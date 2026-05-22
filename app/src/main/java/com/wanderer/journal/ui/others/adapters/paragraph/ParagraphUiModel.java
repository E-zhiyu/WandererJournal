package com.wanderer.journal.ui.others.adapters.paragraph;

import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;

public abstract class ParagraphUiModel {
    // 段落项
    public static final class Item extends ParagraphUiModel {
        public final ParagraphEntityModel model;
        public Item(ParagraphEntityModel p) { this.model = p; }
    }
    // 日期分隔项
    public static final class Separator extends ParagraphUiModel {
        public final String date;
        public Separator(String date) { this.date = date; }
    }
}
