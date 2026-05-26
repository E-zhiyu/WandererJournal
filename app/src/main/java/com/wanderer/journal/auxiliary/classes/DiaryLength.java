package com.wanderer.journal.auxiliary.classes;

public class DiaryLength {
    private int max;    //最大长度
    private int avg;    //平均长度

    public DiaryLength(int max, int avg) {
        this.max = max;
        this.avg = avg;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }
}
