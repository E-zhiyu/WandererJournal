package com.wanderer.journal.ui.pages.diary.write;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class LocationViewModel extends ViewModel {
    @Nullable
    private Location location;                      //监听到的当前位置
    private final MutableLiveData<Boolean> isLocationOnLiveData = new MutableLiveData<>(false); //是否开始位置监听
    @Nullable
    private List<LocationListener> listenerList;    //位置监听器列表，用于取消监听

    @Nullable
    public Location getLocation() {
        return location;
    }

    public void setLocation(@Nullable Location location) {
        this.location = location;
    }

    public boolean isLocationOn() {
        return isLocationOnLiveData.getValue() != null && isLocationOnLiveData.getValue();
    }

    public MutableLiveData<Boolean> getIsLocationOnLiveData() {
        return isLocationOnLiveData;
    }

    public void setLocationOn(boolean locationOn) {
        isLocationOnLiveData.setValue(locationOn);
    }

    @Nullable
    public List<LocationListener> getListenerList() {
        return listenerList;
    }

    public void setListenerList(@Nullable List<LocationListener> listenerList) {
        this.listenerList = listenerList;
    }
}
