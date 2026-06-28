package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.ViewModel;

public class DiaryShareViewModel extends ViewModel {
    private final UnPeekLiveData<Integer> clickEvent = new UnPeekLiveData<>();

    public UnPeekLiveData<Integer> getClickEvent() {
        return clickEvent;
    }

    public void setClickEvent(int clickEventCode) {
        this.clickEvent.setValue(clickEventCode);
    }
}
