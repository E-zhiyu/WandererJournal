package com.wanderer.journal.data.save.db.entities.composite.ui;

import com.wanderer.journal.data.save.db.entities.composite.union.ParagraphUnionModel;

import java.time.LocalDate;

public abstract class ParagraphUiModel {
    // 段落项
    public static final class Item extends ParagraphUiModel {
        public final ParagraphUnionModel model;

        public Item(ParagraphUnionModel p) {
            this.model = p;
        }
    }

    // 日期分隔项
    public static final class Separator extends ParagraphUiModel {
        public final LocalDate date;

        public Separator(LocalDate date) {
            this.date = date;
        }
    }
}
