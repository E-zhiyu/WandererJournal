package com.wanderer.journal.ui.pages.diary.write;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.rxjava3.PagingRx;

import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.data.save.db.entities.composite.union.ParagraphUnionModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WriteViewModel extends ViewModel {
    private final BehaviorProcessor<LocalDate> dateProcessor =
            BehaviorProcessor.createDefault(LocalDate.now());       //日期的处理器
    private final BehaviorProcessor<Boolean> showHiddenParagraphProcessor =
            BehaviorProcessor.createDefault(false);     //是否显示隐藏的段落
    private long lastAuthTimeMillis = 0;
    public final Set<Long> contentDisplayIdSet = new HashSet<>();  //标记为显示内容的段落编号

    private static class Query {
        final LocalDate date;
        final boolean isHiddenShown;

        private Query(LocalDate date, boolean isHiddenShown) {
            this.date = date;
            this.isHiddenShown = isHiddenShown;
        }
    }

    /**
     * 更新日期，以刷新日记段落列表内容
     *
     * @param date 更新后的日期
     */
    public void updateDate(LocalDate date) {
        dateProcessor.onNext(date);
    }

    /**
     * 获取隐藏的段落是否显示
     *
     * @return 是否显示了隐藏的段落
     */
    public boolean isHiddenParagraphShown() {
        return showHiddenParagraphProcessor.getValue() != null && showHiddenParagraphProcessor.getValue();
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
     * 获取由 PagingData转换得到的 Flowable 数据
     *
     * @param db 数据库实例
     * @return 段落数据，支持响应式更新
     */
    public Flowable<PagingData<ParagraphUiModel>> getPagingDataFlow(DiaryDb db) {
        return Flowable.combineLatest(
                        dateProcessor,
                        showHiddenParagraphProcessor,
                        Query::new
                )
                .switchMap(query -> {
                    // 配置 PagingConfig
                    PagingConfig pagingConfig = new PagingConfig(
                            10,
                            20,
                            true,
                            8
                    );

                    // 创建 Pager
                    Pager<Integer, ParagraphUnionModel> pager = new Pager<>(
                            pagingConfig,
                            null, // 从最开始加载
                            () -> db.paragraphDao().getParagraphPagingSourceInDateRange(
                                    query.date,
                                    query.date.plusDays(1),
                                    query.isHiddenShown ? 1 : 0
                            )
                    );

                    return PagingRx.getFlowable(pager).map(this::transformAndSeparator);
                })
                .subscribeOn(Schedulers.io())
                .compose(flowable -> PagingRx.cachedIn(
                        flowable,
                        ViewModelKt.getViewModelScope(this)
                ));
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

                    if (before == null || !isSameDay(before.model.getParagraph().getCreateTime(), after.model.getParagraph().getCreateTime())) {
                        return new ParagraphUiModel.Separator(
                                after.model.getParagraph().getCreateTime().toLocalDate()
                        );
                    }
                    return null;
                });
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
}
