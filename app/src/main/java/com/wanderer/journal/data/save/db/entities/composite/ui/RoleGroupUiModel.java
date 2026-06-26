package com.wanderer.journal.data.save.db.entities.composite.ui;

import com.wanderer.journal.data.save.db.entities.RoleEntity;

import java.util.List;

public class RoleGroupUiModel {
    public static class Item extends RoleGroupUiModel {
        public List<RoleEntity> roleList;

        public Item(List<RoleEntity> roleList) {
            this.roleList = roleList;
        }
    }

    public static class Separator extends RoleGroupUiModel {
        public String separatorText;

        public Separator(String separatorText) {
            this.separatorText = separatorText;
        }
    }
}
