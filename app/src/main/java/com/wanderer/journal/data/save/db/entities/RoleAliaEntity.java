package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "roleAlias",
        foreignKeys = @ForeignKey(
                entity = RoleEntity.class,
                parentColumns = "roleId",
                childColumns = "roleId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "roleId"),
                @Index(value = "alia")
        }
)
public class RoleAliaEntity {
    @PrimaryKey(autoGenerate = true)
    private long aliaId;    //别名 ID
    private long roleId;    //角色 ID
    private String alia;    //称呼

    public RoleAliaEntity(String alia, long roleId) {
        this.alia = alia;
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getAliaId() {
        return aliaId;
    }

    public void setAliaId(long aliaId) {
        this.aliaId = aliaId;
    }

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }
}
