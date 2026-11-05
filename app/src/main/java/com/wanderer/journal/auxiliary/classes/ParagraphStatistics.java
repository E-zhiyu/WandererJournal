package com.wanderer.journal.auxiliary.classes;

public class ParagraphStatistics {
    private final int maxLength;    //最大长度
    private final int minLength;    //最小长度
    private final int avgLength;    //平均长度
    private final int totalCount;       //总数量
    private final int weeHoursCount;    //凌晨段落数量（1:00~5:00)
    private final int forenoonCount;    //上午段落数量（5:00~11:00)
    private final int noonCount;        //中午段落数量（11:00~14:00)
    private final int afternoonCount;   //下午段落数量（14:00~18:00)
    private final int nightCount;       //晚上段落数量（18:00~23:00)
    private final int midnightCount;    //半夜段落数量（23:00~1:00)
    private final int timeUnknownCount; //时间未知的段落数量（0:00）

    public ParagraphStatistics(int maxLength, int minLength, int avgLength, int totalCount, int weeHoursCount, int forenoonCount, int noonCount, int afternoonCount, int nightCount, int midnightCount, int timeUnknownCount) {
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.avgLength = avgLength;
        this.totalCount = totalCount;
        this.weeHoursCount = weeHoursCount;
        this.forenoonCount = forenoonCount;
        this.noonCount = noonCount;
        this.afternoonCount = afternoonCount;
        this.nightCount = nightCount;
        this.midnightCount = midnightCount;
        this.timeUnknownCount = timeUnknownCount;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getAvgLength() {
        return avgLength;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getWeeHoursCount() {
        return weeHoursCount;
    }

    public int getForenoonCount() {
        return forenoonCount;
    }

    public int getNoonCount() {
        return noonCount;
    }

    public int getAfternoonCount() {
        return afternoonCount;
    }

    public int getNightCount() {
        return nightCount;
    }

    public int getMidnightCount() {
        return midnightCount;
    }

    public int getTimeUnknownCount() {
        return timeUnknownCount;
    }
}
