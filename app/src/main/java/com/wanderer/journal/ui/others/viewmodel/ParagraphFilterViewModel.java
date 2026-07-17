package com.wanderer.journal.ui.others.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.rxjava3.PagingRx;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.data.save.db.services.ParagraphService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParagraphFilterViewModel extends ViewModel {
    private final MutableLiveData<Void> filterUpdatedLiveData = new MutableLiveData<>();    //提醒宿主更新 UI 的 LiveData
    private final BehaviorProcessor<String> searchKeywordProcessor =
            BehaviorProcessor.createDefault("");    //搜索关键词处理器（包含空格）
    private final BehaviorProcessor<Boolean> filterUpdateProcessor =
            BehaviorProcessor.createDefault(true);
    private final BehaviorProcessor<Boolean> keywordModeProcessor =
            BehaviorProcessor.createDefault(true);  //多词搜索是否为“与”模式处理器
    private boolean filterMedia = false;
    private final Set<Long> checkedEmotionIdSet = new HashSet<>();

    private static class FilterQuery {
        final boolean isAndMode;    //多词搜索是否为“与”模式
        final String keyword;       //搜索关键词（包含空格）

        public FilterQuery(boolean isAndMode, String keyword) {
            this.isAndMode = isAndMode;
            this.keyword = keyword;
        }
    }

    public Boolean getFilterMedia() {
        return filterMedia;
    }

    public Set<Long> getCheckedEmotionIdSet() {
        return checkedEmotionIdSet;
    }

    public void setFilterMedia(boolean filterMedia) {
        this.filterMedia = filterMedia;
    }

    public boolean isAndMode() {
        Boolean isAndMode = keywordModeProcessor.getValue();
        return isAndMode == null || isAndMode;
    }

    public List<String> getValidKeywordList() {
        String[] words = searchKeywordProcessor.getValue().split("\\s+"); // 按空格拆分
        return Arrays.stream(words)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public MutableLiveData<Void> getFilterUpdatedLiveData() {
        return filterUpdatedLiveData;
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
                        return new ParagraphUiModel.Separator(
                                after.model.getParagraph().getCreateTime().toLocalDate()
                        );
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
                            null, // 从最开始加载
                            () -> db.paragraphDao().getParagraphPagingSourceByDate(start, end)
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
                            initPosition,
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
     * 判断两个时间是否在同一天
     *
     * @param t1 时间实例
     * @param t2 时间实例
     * @return 是否在同一天
     */
    private boolean isSameDay(@NonNull LocalDateTime t1, @NonNull LocalDateTime t2) {
        LocalDate d1 = t1.toLocalDate();
        LocalDate d2 = t2.toLocalDate();
        return d1.isEqual(d2);
    }

    /**
     * 获取符合过滤条件的段落的位置
     *
     * @param db 数据库实例
     * @return 从数据库中获取符合过滤条件的段落下标
     */
    public Flowable<List<Integer>> getFilteredParagraphPosition(DiaryDatabase db) {
        return Flowable.combineLatest(
                        searchKeywordProcessor,
                        filterUpdateProcessor,
                        keywordModeProcessor.debounce(50, TimeUnit.MILLISECONDS),
                        (keyword, b, isAndMode) -> new FilterQuery(isAndMode, keyword)
                )
                .switchMap(filterQuery -> {
                    //判断是否没有过滤选项
                    if (isNoFilter()) {
                        return Flowable.just(new ArrayList<>());
                    }

                    // 直接返回数据库查询的 Flowable，数据变化时会自动发射新结果
                    return ParagraphService.getSearchMatchedParagraphPositionsFlowableInternal(
                            getValidKeywordList(),
                            checkedEmotionIdSet,
                            filterMedia,
                            db,
                            filterQuery.isAndMode
                    );
                });
    }

    /**
     * 执行一次搜索
     *
     * @param keyword 搜索文本
     */
    public void executeSearch(String keyword) {
        searchKeywordProcessor.onNext(keyword);
        filterUpdatedLiveData.setValue(null);
    }

    /**
     * 提醒过滤条件已更新
     */
    public void notifyFilterUpdated() {
        filterUpdateProcessor.onNext(true);
        filterUpdatedLiveData.setValue(null);
    }

    /**
     * 切换多词搜索模式
     */
    public void toggleKeywordMode() {
        keywordModeProcessor.onNext(!isAndMode());
        filterUpdatedLiveData.setValue(null);
    }

    /**
     * 判断是否有过滤条件
     *
     * @return 是否有过滤条件
     */
    public boolean isNoFilter() {
        String searchText = searchKeywordProcessor.getValue();
        return checkedEmotionIdSet.isEmpty() &&
                !filterMedia &&
                (searchText == null || searchText.isEmpty());
    }

    /**
     * 清空过滤条件
     */
    public void clearFilter() {
        filterMedia = false;
        checkedEmotionIdSet.clear();
        searchKeywordProcessor.onNext("");

        filterUpdatedLiveData.setValue(null);
    }
}
