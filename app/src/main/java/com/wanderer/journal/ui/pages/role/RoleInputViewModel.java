package com.wanderer.journal.ui.pages.role;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.entities.composite.union.RoleEntityUnionModel;

public class RoleInputViewModel extends ViewModel {
    private final MutableLiveData<RoleEntityUnionModel> roleModelLiveData = new MutableLiveData<>();

    public MutableLiveData<RoleEntityUnionModel> getRoleModelLiveData() {
        return roleModelLiveData;
    }

    @Nullable
    public RoleEntityUnionModel getRoleModel() {
        return roleModelLiveData.getValue();
    }

    public void setRoleModel(RoleEntityUnionModel model) {
        roleModelLiveData.setValue(model);
    }
}
