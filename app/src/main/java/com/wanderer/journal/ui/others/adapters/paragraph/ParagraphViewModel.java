package com.wanderer.journal.ui.others.adapters.paragraph;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.rxjava3.PagingRx;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphWithEmotion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParagraphViewModel extends ViewModel {
    private final DiaryDatabase db; //数据库实例

    public ParagraphViewModel(@NonNull DiaryDatabase db) {
        this.db = db;
    }

    /**
     * 为不同日期的段落之间插入日期分隔视图
     *
     * @param pagingData 原始段落数据
     * @return 插入分隔视图后的段落数据
     */
    @NonNull
    private PagingData<ParagraphUiModel> transformAndSeparator(PagingData<ParagraphWithEmotion> pagingData) {
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
    public Flowable<PagingData<ParagraphUiModel>> getPagingDataFlow(LocalDate start, LocalDate end, LocalDate targetDate) {
        // 1. 使用 Flowable.fromCallable 将同步的数据库查询和 Pager 初始化打包
        return Flowable.fromCallable(() -> {
                    // 查询目标日期在数据库中的绝对位置
                    int initPosition = 0;
                    if (targetDate != null) {
                        initPosition = db.paragraphDao().getAdjustedPositionSingle(targetDate);
                    }

                    // 配置 PagingConfig
                    PagingConfig pagingConfig = new PagingConfig(
                            10,
                            20,
                            true, // 必须为 true 以支持精准定位
                            20
                    );

                    // 创建 Pager
                    Pager<Integer, ParagraphWithEmotion> pager = new Pager<>(
                            pagingConfig,
                            initPosition, // 动态传入计算出的下标
                            () -> (start != null && end != null)
                                    ? db.paragraphDao().getParagraphPagingSourceInRange(start, end)
                                    : db.paragraphDao().getAllParagraphPagingSource()
                    );

                    return PagingRx.getFlowable(pager).map(this::transformAndSeparator);
                })
                .subscribeOn(Schedulers.io())   //在 IO 线程执行
                .flatMap(pagingDataFlow -> pagingDataFlow)
                .compose(flowable -> PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this)));
    }

    /**
     * 不指定起止日期的获取段落数据方法
     *
     * @return 段落分页数据，支持响应式更新
     */
    public Flowable<PagingData<ParagraphUiModel>> getPagingDataFlow(LocalDate targetDate) {
        return getPagingDataFlow(null, null, targetDate);
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
}
