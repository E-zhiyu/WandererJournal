package com.wanderer.journal.auxiliary.classes.statistics;

import com.wanderer.journal.data.save.db.entities.composite.union.EmotionTagUseCountUnionModel;

import java.util.List;

public class EmotionTagStatistics {
    private final List<EmotionTagUseCountUnionModel> tagUseCountModelList;
    private final int maxTagCountInOneParagraph;
    private final double avgTagCountInOneParagraph;

    public EmotionTagStatistics(List<EmotionTagUseCountUnionModel> tagUseCountModelList, int maxTagCountInOneParagraph, double avgTagCountInOneParagraph) {
        this.tagUseCountModelList = tagUseCountModelList;
        this.maxTagCountInOneParagraph = maxTagCountInOneParagraph;
        this.avgTagCountInOneParagraph = avgTagCountInOneParagraph;
    }

    public List<EmotionTagUseCountUnionModel> getTagUseCountModelList() {
        return tagUseCountModelList;
    }

    public int getMaxTagCountInOneParagraph() {
        return maxTagCountInOneParagraph;
    }

    public double getAvgTagCountInOneParagraph() {
        return avgTagCountInOneParagraph;
    }
}
