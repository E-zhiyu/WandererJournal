package com.wanderer.journal.data.backup.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class RoleAliaPojo {
    private long aliaId;
    private long roleId;
    private String alia;

    public RoleAliaPojo() {
    }

    public long getAliaId() {
        return aliaId;
    }

    public void setAliaId(long aliaId) {
        this.aliaId = aliaId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }
}
