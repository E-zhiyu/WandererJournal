package com.wanderer.journal.data.save.db.entities.composite.ui;

import com.wanderer.journal.data.save.db.entities.composite.union.UsedEmotionTagUnionModel;

public class EmotionListUiModel {
    public static class Item extends EmotionListUiModel {
        public final UsedEmotionTagUnionModel model;

        public Item(UsedEmotionTagUnionModel model) {
            this.model = model;
        }
    }

    public static class Separator extends EmotionListUiModel {
        public final String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
