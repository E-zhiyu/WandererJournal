package com.wanderer.journal.data.save.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Locale;

@Entity(
        tableName = "roles",
        indices = {
                @Index(value = "roleId"),
                @Index(value = "name"),
                @Index(value = "displayName"),
                @Index(value = "relationship"),
                @Index(value = "identity"),
                @Index(value = "impression"),
                @Index(value = "useCount")
        }
)
public class RoleEntity {
    @PrimaryKey(autoGenerate = true)
    private long roleId;            //自增主键
    private String name;            //名称
    @ColumnInfo(defaultValue = "")
    private String displayName;     //显示名称
    private String identity;        //身份描述
    private String impression;      //印象描述
    @ColumnInfo(defaultValue = "2")
    private int relationship;       //关系程度
    @ColumnInfo(defaultValue = "0")
    private int useCount;           //使用次数

    public RoleEntity(String name, String displayName, String identity, String impression, int relationship) {
        this.name = name;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
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

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public String generateDisplayName() {
        if (displayName.isEmpty()) {
            return name;
        } else {
            return String.format(Locale.getDefault(), "%s (%s)", displayName, name);
        }
    }
}
