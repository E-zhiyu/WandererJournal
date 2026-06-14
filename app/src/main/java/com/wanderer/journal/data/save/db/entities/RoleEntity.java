package com.wanderer.journal.data.save.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "roles",
        indices = {
                @Index(value = "roleId"),
                @Index(value = "name"),
                @Index(value = "relationship"),
                @Index(value = "identity"),
                @Index(value = "impression")
        }
)
public class RoleEntity {
    @PrimaryKey(autoGenerate = true)
    private long roleId;            //自增主键
    private String name;            //名称
    private String identity;        //身份描述
    private String impression;      //印象描述
    @ColumnInfo(defaultValue = "2")
    private int relationship;       //关系程度

    public RoleEntity(String name, String identity, String impression, int relationship) {
        this.name = name;
        this.identity = identity;
        this.impression = impression;
        this.relationship = relationship;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }
}
