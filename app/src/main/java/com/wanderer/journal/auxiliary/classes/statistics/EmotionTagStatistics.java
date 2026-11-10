package com.wanderer.journal.auxiliary.classes.statistics;

import com.wanderer.journal.data.save.db.entities.composite.union.UsedEmotionTagUnionModel;

import java.util.List;

public class EmotionTagStatistics {
    private final List<UsedEmotionTagUnionModel> tagUseCountModelList;
    private final ParagraphEmotionTagStatistics paragraphEmotionTagStatistics;

    public EmotionTagStatistics(List<UsedEmotionTagUnionModel> tagUseCountModelList, ParagraphEmotionTagStatistics paragraphEmotionTagStatistics) {
        this.tagUseCountModelList = tagUseCountModelList;
        this.paragraphEmotionTagStatistics = paragraphEmotionTagStatistics;
    }

    public List<UsedEmotionTagUnionModel> getTagUseCountModelList() {
        return tagUseCountModelList;
    }

    public ParagraphEmotionTagStatistics getParagraphEmotionTagStatistics() {
        return paragraphEmotionTagStatistics;
    }
}
