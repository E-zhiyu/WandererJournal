package com.wanderer.journal.data.save.db.entities.composite.ui;

import com.wanderer.journal.data.save.db.entities.composite.union.RoleEntityUnionModel;

public class RoleUiModel {
    //角色项
    public static class Item extends RoleUiModel {
        public final RoleEntityUnionModel model;

        public Item(RoleEntityUnionModel model) {
            this.model = model;
        }
    }

    //关系分隔符
    public static class Separator extends RoleUiModel {
        public final String relationship;

        public Separator(String relationship) {
            this.relationship = relationship;
        }
    }
}
