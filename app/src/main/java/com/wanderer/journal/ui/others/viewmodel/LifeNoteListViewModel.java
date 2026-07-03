package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.db.services.LifeNoteService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LifeNoteListViewModel extends ViewModel {
    private final BehaviorProcessor<String> searchKeywordProcessor =
            BehaviorProcessor.createDefault("");    //搜索关键词处理器

    /**
     * 获取人生笔记数据，并支持搜索防抖
     *
     * @param db 数据库实例
     * @return 人生笔记数据列表，支持响应式更新
     */
    public Flowable<List<LifeNoteEntity>> getLifeNoteListFlowable(DiaryDatabase db) {
        return searchKeywordProcessor
                .debounce(50, TimeUnit.MILLISECONDS)
                .switchMap(
                        keyword -> LifeNoteService.getAllLifeNoteFlowable(db, keyword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
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
