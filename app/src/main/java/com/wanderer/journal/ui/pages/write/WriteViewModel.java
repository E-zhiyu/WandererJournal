package com.wanderer.journal.ui.pages.write;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.time.LocalDate;

import io.reactivex.rxjava3.core.Flowable;

public class WriteViewModel extends ViewModel {
    private final Flowable<PagingData<ParagraphEntity>> pagingDataFlow;

    public WriteViewModel(@NonNull ParagraphDao dao) {
        // 1. 配置分页参数
        PagingConfig pagingConfig = new PagingConfig(
                20,      // 每页加载数量
                10,      // 预取距离（滑到倒数第10条时开始加载下一页）
                false,   // 是否启用占位符
                20       // 初始加载数量
        );

        // 2. 创建 Pager 并转换为 Flowable
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);
        Pager<Integer, ParagraphEntity> pager = new Pager<>(
                pagingConfig,
                () -> dao.getParagraphPagingSourceInRange(start, end)
        );

        // 3. 将其暴露给 View 层
        pagingDataFlow = PagingRx.getFlowable(pager);

        // 关联 ViewModel 的生命周期，自动缓存数据
        PagingRx.cachedIn(pagingDataFlow, ViewModelKt.getViewModelScope(this));
    }

    public Flowable<PagingData<ParagraphEntity>> getPagingDataFlow() {
        return pagingDataFlow;
    }
}
