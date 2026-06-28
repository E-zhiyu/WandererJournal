package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.ViewModel;

public class MediaAddOptionViewModel extends ViewModel {
    private final UnPeekLiveData<Integer> clickEvent = new UnPeekLiveData<>();

    public UnPeekLiveData<Integer> getClickEvent() {
        return clickEvent;
    }

    public void setClickEvent(int eventCode) {
        this.clickEvent.setValue(eventCode);
    }
}
