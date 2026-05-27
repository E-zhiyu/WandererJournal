package com.wanderer.journal.ui.others.adapters.paragraph;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.rxjava3.PagingRx;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParagraphViewModel extends ViewModel {
    private final MutableLiveData<List<Integer>> matchedPositions = new MutableLiveData<>(new ArrayList<>());   //匹配搜索的位置列表
    private final MutableLiveData<Integer> currentMatchIndex = new MutableLiveData<>(-1);   //当前所在的匹配搜索位置的下标

    public LiveData<Integer> getCurrentMatchIndex() {
        return currentMatchIndex;
    }

    public LiveData<List<Integer>> getMatchedPositions() {
        return matchedPositions;
    }

    /**
     * 为不同日期的段落之间插入日期分隔视图
     *
     * @param pagingData 原始段落数据
     * @return 插入分隔视图后的段落数据
     */
    @NonNull
    private PagingData<ParagraphUiModel> transformAndSeparator(PagingData<ParagraphEntityModel> pagingData) {
        Executor executor = Runnable::run;

        PagingData<ParagraphUiModel.Item> itemPagingData = PagingDataTransforms.map(
                pagingData, executor, ParagraphUiModel.Item::new);

        return PagingDataTransforms.insertSeparators(
                itemPagingData, executor, (before, after) -> {
                    if (after == null) return null;
                    if (before == null || !isSameDay(before.model.getParagraph().getCreateTime(), after.model.getParagraph().getCreateTime())) {
                        return new ParagraphUiModel.Separator(formatDate(after.model.getParagraph().getCreateTime()));
                    }
                    return null;
                });
    }

    /**
     * 获取由 PagingData转换得到的 Flowable 数据
     *
     * @param start 段落起始日期
     * @param end   段落结束日期（不包含）
     * @return 段落数据，支持响应式更新
     */
    public Flowable<PagingData<ParagraphUiModel>> getPagingDataFlow(
            @NonNull LocalDate start,
            @NonNull LocalDate end,
            DiaryDatabase db
    ) {
        return Flowable.fromCallable(() -> {
                    // 配置 PagingConfig
                    PagingConfig pagingConfig = new PagingConfig(
                            10,
                            20,
                            true, // 必须为 true 以支持精准定位
                            8
                    );

                    // 创建 Pager
                    Pager<Integer, ParagraphEntityModel> pager = new Pager<>(
                            pagingConfig,
                            0, // 从最开始加载
                            () -> db.paragraphDao().getParagraphPagingSourceInRange(start, end)
                    );

                    return PagingRx.getFlowable(pager).map(this::transformAndSeparator);
                })
                .subscribeOn(Schedulers.io())   //在 IO 线程执行
                .flatMap(pagingDataFlow -> pagingDataFlow)
                .compose(flowable -> PagingRx.cachedIn(
                        flowable,
                        ViewModelKt.getViewModelScope(this)
                ));
    }

    /**
     * 不指定起止日期的获取段落数据方法
     *
     * @return 段落分页数据，支持响应式更新
     */
    public Flowable<PagingData<ParagraphUiModel>> getPagingDataFlow(int initPosition, DiaryDatabase db) {
        return Flowable.fromCallable(() -> {
                    // 配置 PagingConfig
                    PagingConfig pagingConfig = new PagingConfig(
                            10,
                            20,
                            true, // 必须为 true 以支持精准定位
                            8
                    );

                    // 创建 Pager
                    Pager<Integer, ParagraphEntityModel> pager = new Pager<>(
                            pagingConfig,
                            initPosition, // 动态传入计算出的下标
                            () -> db.paragraphDao().getAllParagraphPagingSource()
                    );

                    return PagingRx.getFlowable(pager).map(this::transformAndSeparator);
                })
                .subscribeOn(Schedulers.io())   //在 IO 线程执行
                .flatMap(pagingDataFlow -> pagingDataFlow)
                .compose(flowable -> PagingRx.cachedIn(
                        flowable,
                        ViewModelKt.getViewModelScope(this)
                ));
    }

    /**
     * 将{@link LocalDateTime}转换为yyyy-MM-dd日期字符串
     *
     * @param datetime 需要转换的时间类
     * @return 转换得到的日期字符串
     */
    @NonNull
    private String formatDate(@NonNull LocalDateTime datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");
        return datetime.format(formatter);
    }

    /**
     * 判断两个时间是否在同一天
     *
     * @param t1 时间实例
     * @param t2 时间实例
     * @return 是否在同一天
     */
    private boolean isSameDay(@NonNull LocalDateTime t1, @NonNull LocalDateTime t2) {
        LocalDate d1 = t1.toLocalDate();
        LocalDate d2 = t2.toLocalDate();
        return d1.equals(d2);
    }

    /**
     * 清空搜索
     */
    private void clearSearch() {
        matchedPositions.postValue(new ArrayList<>());
        currentMatchIndex.postValue(-1);
    }

    /**
     * 执行搜索逻辑
     *
     * @param keyword 搜索关键词
     * @return 从数据库中获取符合搜索条件的下标
     */
    public Single<List<Integer>> executeSearch(String keyword, DiaryDatabase db) {
        if (keyword == null || keyword.isEmpty()) {
            clearSearch();
            return Single.just(new ArrayList<>());
        }

        return Single.defer(() -> {
            //转义防止 SQL 注入
            String safeKeyword = keyword.replace("/", "//")
                    .replace("%", "/%")
                    .replace("_", "/_");

            //在数据库中查询并返回结果
            ParagraphDao paragraphDao = db.paragraphDao();
            List<Integer> positionList = paragraphDao.getSearchMatchedParagraphPositions(safeKeyword);
            matchedPositions.postValue(positionList);
            if (!positionList.isEmpty()) {
                currentMatchIndex.postValue(positionList.size() - 1);
            } else {
                currentMatchIndex.postValue(-1);
            }
            return Single.just(positionList);
        });
    }

    // 点击“向下”按钮
    public void jumpToNext() {
        List<Integer> positions = matchedPositions.getValue();
        Integer currentIndex = currentMatchIndex.getValue();
        if (positions != null && !positions.isEmpty() && currentIndex != null) {
            int nextIndex = currentIndex == -1 ?
                    0 :
                    (currentIndex + 1) % positions.size(); // 循环滚动
            currentMatchIndex.setValue(nextIndex);
        }
    }

    // 点击“向上”按钮
    public void jumpToPrevious() {
        List<Integer> positions = matchedPositions.getValue();
        Integer currentIndex = currentMatchIndex.getValue();
        if (positions != null && !positions.isEmpty() && currentIndex != null) {
            int prevIndex = currentIndex == -1 ?
                    positions.size() - 1 :
                    (currentIndex - 1 + positions.size()) % positions.size();
            currentMatchIndex.setValue(prevIndex);
        }
    }
}
