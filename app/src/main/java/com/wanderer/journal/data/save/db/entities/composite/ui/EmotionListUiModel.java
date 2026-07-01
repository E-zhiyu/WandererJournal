package com.wanderer.journal.data.save.db.entities.composite.ui;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;

public class EmotionListUiModel {
    public static class Item extends EmotionListUiModel {
        public final EmotionTagEntity entity;

        public Item(EmotionTagEntity entity) {
            this.entity = entity;
        }
    }

    public static class Separator extends EmotionListUiModel {
        public final String text;

        public Separator(String text) {
            this.text = text;
        }
    }
}
