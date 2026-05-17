package com.wanderer.journal.ui.others.adapters.paragraph;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParagraphViewModel extends ViewModel {
    // 使用 Processor 或 Subject 来接收外部触发的初始化信号（目标日期）
    private final BehaviorProcessor<LocalDate> targetDateProcessor = BehaviorProcessor.create();

    private final Flowable<PagingData<ParagraphUiModel>> pagingDataFlow;

    public ParagraphViewModel(@NonNull DiaryDatabase db, @Nullable LocalDate start, @Nullable LocalDate end) {
        this.pagingDataFlow = PagingRx.cachedIn(
                targetDateProcessor
                        .distinctUntilChanged() // 防止重复触发
                        .flatMapSingle(date -> {
                            // 1. 异步获取初始位置
                            return db.paragraphDao().getAdjustedPositionSingle(date)
                                    .subscribeOn(Schedulers.io());
                        })
                        .flatMap(initPosition -> {
                            // 2. 拿到位置后，构建 Pager 流
                            PagingConfig pagingConfig = new PagingConfig(
                                    10,
                                    20,
                                    true,
                                    30,
                                    60
                            );

                            Pager<Integer, ParagraphWithEmotion> pager = new Pager<>(
                                    pagingConfig,
                                    initPosition, // 动态传入计算出的下标
                                    () -> (start != null && end != null)
                                            ? db.paragraphDao().getParagraphPagingSourceInRange(start, end)
                                            : db.paragraphDao().getAllParagraphPagingSource()
                            );

                            return PagingRx.getFlowable(pager);
                        })
                        .map(this::transformAndSeparator),  // 3. 变换与插入分隔符
                ViewModelKt.getViewModelScope(this)
        );
    }

    // 暴露给外部的启动接口
    public void scrollToDate(LocalDate date) {
        targetDateProcessor.onNext(date);
    }

    // 将复杂的转换逻辑提取出来
    @NonNull
    private PagingData<ParagraphUiModel> transformAndSeparator(PagingData<ParagraphWithEmotion> pagingData) {
        Executor executor = Runnable::run; // 转换通常在主线程或 Paging 内部线程同步执行

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

    public Flowable<PagingData<ParagraphUiModel>> getPagingDataFlow() {
        return pagingDataFlow;
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
