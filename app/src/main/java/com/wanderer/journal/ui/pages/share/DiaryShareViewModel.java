package com.wanderer.journal.ui.pages.share;

import androidx.lifecycle.ViewModel;

import com.wanderer.journal.ui.others.viewmodel.UnPeekLiveData;

public class DiaryShareViewModel extends ViewModel {
    private final UnPeekLiveData<Integer> clickEvent = new UnPeekLiveData<>();

    public UnPeekLiveData<Integer> getClickEvent() {
        return clickEvent;
    }

    public void setClickEvent(int clickEventCode) {
        this.clickEvent.setValue(clickEventCode);
    }
}
