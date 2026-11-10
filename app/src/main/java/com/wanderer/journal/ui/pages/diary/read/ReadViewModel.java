package com.wanderer.journal.ui.pages.diary.read;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.rxjava3.PagingRx;

import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.entities.composite.union.ParagraphUnionModel;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.data.save.db.services.ParagraphService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReadViewModel extends ViewModel {
    private final MutableLiveData<FilterConditions> filterConditionsLiveData = new MutableLiveData<>();  //提醒宿主更新 UI 的 LiveData
    private List<Long> filteredParagraphIdList; //过滤的段落的编号
    private final BehaviorProcessor<FilterConditions> filterConditionsProcessor =
            BehaviorProcessor.createDefault(new FilterConditions());
    public final Set<Long> contentDisplayIdSet = new HashSet<>();  //标记为显示内容的段落编号
    private long lastAuthTimeMillis = 0;

    public static class FilterConditions {
        public boolean isHiddenShown;                   //是否显示隐藏段落
        public boolean isAndMode;                       //多词搜索是否为“与”模式
        @NonNull
        public String keyword;                          //搜索关键词（包含空格）
        public boolean filterMedia;                     //是否需要带有媒体
        public boolean filterLocation;                  //是否需要带有位置
        @NonNull
        public final Map<Long, Integer> emotionWhiteList;    //情绪标签白名单（k:标签编号; v:情绪等级）

        /**
         * 不设置过滤条件的构造方法
         */
        public FilterConditions() {
            isHiddenShown = false;
            isAndMode = false;
            keyword = "";
            filterMedia = false;
            filterLocation = false;
            emotionWhiteList = new HashMap<>();
        }

        /**
         * 判断是否有段落过滤条件
         *
         * @return 段落过滤条件
         */
        public boolean hasFilter() {
            return !keyword.isEmpty() ||
                    filterMedia ||
                    filterLocation ||
                    !emotionWhiteList.isEmpty();
        }
    }

    public List<Long> getFilteredParagraphIdList() {
        return filteredParagraphIdList;
    }

    /**
     * 判断是否未通过身份验证
     *
     * @return 是否未通过身份验证
     */
    public boolean isNotAuthed() {
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - lastAuthTimeMillis > 1000 * 60 * 5;     //一次授权的有效时间为5分钟
    }

    /**
     * 设置是否通过身份验证
     *
     * @param isAuthed 是否通过身份验证
     */
    public void setIsAuthed(boolean isAuthed) {
        lastAuthTimeMillis = isAuthed ? System.currentTimeMillis() : 0;
    }

    /**
     * 获取有效的搜索关键词列表
     *
     * @param rawSearchStr 原始搜索字符串
     * @return 分离的搜索关键词列表
     */
    public List<String> getValidKeywordList(@NonNull String rawSearchStr) {
        String[] words = rawSearchStr.split("\\s+"); // 按空格拆分
        return Arrays.stream(words)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public MutableLiveData<FilterConditions> getFilterConditionsLiveData() {
        return filterConditionsLiveData;
    }

    /**
     * 为不同日期的段落之间插入日期分隔视图
     *
     * @param pagingData 原始段落数据
     * @return 插入分隔视图后的段落数据
     */
    @NonNull
    private PagingData<ParagraphUiModel> transformAndSeparator(PagingData<ParagraphUnionModel> pagingData) {
        Executor executor = Runnable::run;

        PagingData<ParagraphUiModel.Item> itemPagingData = PagingDataTransforms.map(
                pagingData, executor, ParagraphUiModel.Item::new);

        return PagingDataTransforms.insertSeparators(
                itemPagingData, executor, (before, after) -> {
                    if (after == null) return null;

                    LocalDate afterDate = after.model.getParagraph().getCreateTime().toLocalDate();
                    if (before == null) {
                        return new ParagraphUiModel.Separator(afterDate);
                    }
                    LocalDate beforeDate = before.model.getParagraph().getCreateTime().toLocalDate();
                    if (!beforeDate.isEqual(afterDate)) {
                        return new ParagraphUiModel.Separator(afterDate);
                    }
                    return null;
                });
    }

    /**
     * 获取段落数据
     *
     * @param initPosition 初始跳转到的位置
     * @param db           数据库实例
     * @return 段落分页数据，支持响应式更新
     */
    public Flowable<PagingData<ParagraphUiModel>> getParagraphPagingDataFlow(int initPosition, DiaryDb db) {
        return filterConditionsProcessor.debounce(50, TimeUnit.MILLISECONDS)
                .distinctUntilChanged(filterConditions -> filterConditions.isHiddenShown)
                .switchMap(filterConditions -> {
                    // 配置 PagingConfig
                    PagingConfig pagingConfig = new PagingConfig(
                            10,
                            20,
                            true, // 必须为 true 以支持精准定位
                            8
                    );

                    // 创建 Pager
                    Pager<Integer, ParagraphUnionModel> pager = new Pager<>(
                            pagingConfig,
                            initPosition,
                            () -> db.paragraphDao().getAllParagraphPagingSource(filterConditions.isHiddenShown)
                    );

                    return PagingRx.getFlowable(pager).map(this::transformAndSeparator);
                })
                .subscribeOn(Schedulers.io())   //在 IO 线程执行
                .compose(flowable -> PagingRx.cachedIn(
                        flowable,
                        ViewModelKt.getViewModelScope(this)
                ));
    }

    /**
     * 获取符合过滤条件的段落的编号
     *
     * @param db 数据库实例
     * @return 从数据库中获取符合过滤条件的段落编号
     */
    public Flowable<List<Long>> getFilteredParagraphIds(DiaryDb db) {
        return filterConditionsProcessor.debounce(50, TimeUnit.MILLISECONDS)
                .switchMap(filterConditions -> {
                    filterConditionsLiveData.postValue(filterConditions);

                    //判断是否没有过滤选项
                    if (!filterConditions.hasFilter()) {
                        List<Long> emptyResult = new ArrayList<>();
                        filteredParagraphIdList = emptyResult;
                        return Flowable.just(emptyResult);
                    }

                    // 直接返回数据库查询的 Flowable，数据变化时会自动发射新结果
                    return ParagraphService.getSearchMatchedParagraphPositionsFlowableInternal(
                                    getValidKeywordList(filterConditions.keyword),
                                    filterConditions.emotionWhiteList,
                                    filterConditions.filterMedia,
                                    filterConditions.filterLocation,
                                    filterConditions.isHiddenShown,
                                    filterConditions.isAndMode,
                                    db
                            )
                            .flatMap(idList -> {
                                filteredParagraphIdList = idList;
                                return Flowable.just(idList);
                            });
                });
    }

    /**
     * 更新过滤条件
     *
     * @param conditions 过滤条件实体
     */
    public void setFilterConditions(@Nullable FilterConditions conditions) {
        filterConditionsProcessor.onNext(conditions == null ? new FilterConditions() : conditions);
    }

    /**
     * 获取当前的过滤条件
     *
     * @return 当前的过滤条件
     */
    public FilterConditions getFilterConditions() {
        FilterConditions conditions = filterConditionsProcessor.getValue();
        return Objects.requireNonNullElseGet(conditions, FilterConditions::new);
    }
}
