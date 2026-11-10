package com.wanderer.journal.auxiliary.classes.statistics;

public class ParagraphEmotionTagStatistics {
    private final int maxTagCountInOneParagraph;            //在一个段落中同时出现的最多的标签数量
    private final double avgTagCountInOneParagraph;         //在同一个段落中同时出现的平均的标签数量
    private final int maxEmotionDegreeInOneParagraph;       //最大的段落情绪标签等级之和
    private final double avgEmotionDegreeInOneParagraph;    //平均段落情绪标签等级之和
    private final int strongEmotionDegreeParagraphCount;    //情绪标签等级之和超过了平均数的段落数量


    public ParagraphEmotionTagStatistics(int maxTagCountInOneParagraph, double avgTagCountInOneParagraph, int maxEmotionDegreeInOneParagraph, double avgEmotionDegreeInOneParagraph, int strongEmotionDegreeParagraphCount) {
        this.maxTagCountInOneParagraph = maxTagCountInOneParagraph;
        this.avgTagCountInOneParagraph = avgTagCountInOneParagraph;
        this.maxEmotionDegreeInOneParagraph = maxEmotionDegreeInOneParagraph;
        this.avgEmotionDegreeInOneParagraph = avgEmotionDegreeInOneParagraph;
        this.strongEmotionDegreeParagraphCount = strongEmotionDegreeParagraphCount;
    }

    public int getMaxTagCountInOneParagraph() {
        return maxTagCountInOneParagraph;
    }

    public double getAvgTagCountInOneParagraph() {
        return avgTagCountInOneParagraph;
    }

    public int getMaxEmotionDegreeInOneParagraph() {
        return maxEmotionDegreeInOneParagraph;
    }

    public double getAvgEmotionDegreeInOneParagraph() {
        return avgEmotionDegreeInOneParagraph;
    }

    public int getStrongEmotionDegreeParagraphCount() {
        return strongEmotionDegreeParagraphCount;
    }
}
