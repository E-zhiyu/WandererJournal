package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleUiModel;
import com.wanderer.journal.data.save.db.services.RoleService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class RoleManageViewModel extends ViewModel {
    private final BehaviorProcessor<String> searchKeywordProcessor =
            BehaviorProcessor.createDefault("");    //搜索关键词处理器

    /**
     * 获取角色数据
     *
     * @param db 数据库实例
     * @return 角色数据列表，包含分隔符
     */
    public Flowable<List<RoleUiModel>> getRoleListFlowable(DiaryDatabase db) {
        return searchKeywordProcessor
                .debounce(300, TimeUnit.MILLISECONDS)
                .switchMap(
                        keyword -> RoleService.getAllRoleFlowable(db, keyword)
                );
    }

    /**
     * 执行搜索
     *
     * @param keyword 搜索关键词
     */
    public void executeSearch(String keyword) {
        searchKeywordProcessor.onNext(keyword);
    }
}
