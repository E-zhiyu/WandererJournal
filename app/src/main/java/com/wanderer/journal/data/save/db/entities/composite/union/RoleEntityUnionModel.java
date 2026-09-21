package com.wanderer.journal.data.save.db.entities.composite.union;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;

import java.util.List;

public class RoleEntityUnionModel {
    @Embedded
    private RoleEntity role;
    @Relation(
            entity = RoleAliaEntity.class,
            parentColumn = "roleId",
            entityColumn = "roleId"
    )
    private List<RoleAliaEntity> roleAliaList;

    public RoleEntityUnionModel(RoleEntity role, List<RoleAliaEntity> roleAliaList) {
        this.role = role;
        this.roleAliaList = roleAliaList;
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public List<RoleAliaEntity> getRoleAliaList() {
        return roleAliaList;
    }

    public void setRoleAliaList(List<RoleAliaEntity> roleAliaList) {
        this.roleAliaList = roleAliaList;
    }
}
