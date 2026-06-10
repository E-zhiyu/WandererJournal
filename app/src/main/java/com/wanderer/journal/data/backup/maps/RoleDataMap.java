package com.wanderer.journal.data.backup.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wanderer.journal.data.backup.pojo.RoleAliaPojo;
import com.wanderer.journal.data.backup.pojo.RolePojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class RoleDataMap {
    private List<RolePojo> roleList;                                //角色列表
    private List<RoleAliaPojo> roleAliaList;

    public List<RolePojo> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<RolePojo> roleList) {
        this.roleList = roleList;
    }

    public List<RoleAliaPojo> getRoleAliaList() {
        return roleAliaList;
    }

    public void setRoleAliaList(List<RoleAliaPojo> roleAliaList) {
        this.roleAliaList = roleAliaList;
    }
}
