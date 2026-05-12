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

import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;

import io.reactivex.rxjava3.core.Flowable;

public class ParagraphViewModel extends ViewModel {
    private final Flowable<PagingData<ParagraphUiModel>> pagingDataFlow;

    /**
     * 段落 ViewModel 构造方法
     *
     * @param dao   段落表查询器
     * @param start 开始日期，传递 null 不限制日期范围
     * @param end   结束日期（不包含），传递 null 不限制日期范围
     */
    public ParagraphViewModel(@NonNull ParagraphDao dao, @Nullable LocalDate start, @Nullable LocalDate end) {
        PagingConfig pagingConfig = new PagingConfig(20, 10, false, 20);

        Pager<Integer, ParagraphEntity> pager = new Pager<>(
                pagingConfig,
                () -> (start != null && end != null)
                        ? dao.getParagraphPagingSourceInRange(start, end)
                        : dao.getAllParagraphPagingSource()
        );

        // 获取原始流并进行变换
        Flowable<PagingData<ParagraphUiModel>> transformedFlow = PagingRx.getFlowable(pager)
                .map(pagingData -> {
                    // 1. 将 ParagraphEntity 映射为 ParagraphUiModel.Item
                    Executor executor = Runnable::run;

                    PagingData<ParagraphUiModel.Item> itemPagingData = PagingDataTransforms.map(
                            pagingData,
                            executor, // 第二个参数：执行器
                            ParagraphUiModel.Item::new // 第三个参数：转换逻辑
                    );

                    // 2. 插入分隔符
                    return PagingDataTransforms.insertSeparators(
                            itemPagingData,
                            executor,
                            (before, after) -> {
                                // 列表末尾不需要分隔符
                                if (after == null) return null;

                                // 列表头部：插入第一个日期分隔符
                                if (before == null) {
                                    return new ParagraphUiModel.Separator(formatDate(after.paragraph.getCreateTime()));
                                }

                                // 相邻项日期不同：插入新的日期分隔符
                                if (!isSameDay(before.paragraph.getCreateTime(), after.paragraph.getCreateTime())) {
                                    return new ParagraphUiModel.Separator(formatDate(after.paragraph.getCreateTime()));
                                }

                                return null; // 同一天，不插入
                            });
                });

        // 缓存数据
        pagingDataFlow = PagingRx.cachedIn(transformedFlow, ViewModelKt.getViewModelScope(this));
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
