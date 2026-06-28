package com.wanderer.journal.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.entities.RoleEntity;

import java.util.List;
import java.util.Map;

public class RoleSelectViewModel extends ViewModel {
    //存放超复杂的复合类型数据（不用考虑序列化）
    private final MutableLiveData<Map<Integer, List<RoleEntity>>> groupedRoleMap = new MutableLiveData<>();

    //存放点击事件通信机制（代替传统 Listener）
    private final UnPeekLiveData<RoleEntity> selectedRoleEvent = new UnPeekLiveData<>();    //新的观察者不需要观察旧的点击事件

    public void setSelectedRole(RoleEntity role) {
        selectedRoleEvent.setValue(role);
    }

    public void setGroupedMap(Map<Integer, List<RoleEntity>> map) {
        this.groupedRoleMap.setValue(map);
    }

    public MutableLiveData<Map<Integer, List<RoleEntity>>> getGroupedMap() {
        return groupedRoleMap;
    }

    public UnPeekLiveData<RoleEntity> getSelectedRoleEvent() {
        return selectedRoleEvent;
    }
}
