package com.wanderer.journal.ui.pages.paragraph;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

public abstract class ParagraphUiModel {
    // 段落项
    public static final class Item extends ParagraphUiModel {
        public final ParagraphEntity paragraph;
        public Item(ParagraphEntity p) { this.paragraph = p; }
    }
    // 日期分隔项
    public static final class Separator extends ParagraphUiModel {
        public final String date;
        public Separator(String date) { this.date = date; }
    }
}
