package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class LifeNoteListViewModel extends ViewModel {
    private final BehaviorProcessor<String> searchKeywordProcessor =
            BehaviorProcessor.createDefault("");    //搜索关键词处理器


}
