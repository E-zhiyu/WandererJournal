package com.wanderer.journal.auxiliary.classes;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class FilterConditions {
    public boolean isHiddenShown;                   //是否显示隐藏段落
    public boolean isAndMode;                       //多词搜索是否为“与”模式
    @NonNull
    public String keyword;                          //搜索关键词（包含空格）
    public boolean filterMedia;                     //是否需要带有媒体
    public boolean filterLocation;                  //是否需要带有位置
    @NonNull
    public final Map<Long, Integer> emotionWhiteList;    //情绪标签白名单（k:标签编号; v:情绪等级）

    /**
     * 不设置过滤条件的构造方法
     */
    public FilterConditions() {
        isHiddenShown = false;
        isAndMode = false;
        keyword = "";
        filterMedia = false;
        filterLocation = false;
        emotionWhiteList = new HashMap<>();
    }

    /**
     * 判断是否有段落过滤条件
     *
     * @return 段落过滤条件
     */
    public boolean hasFilter() {
        return !keyword.isEmpty() ||
                filterMedia ||
                filterLocation ||
                !emotionWhiteList.isEmpty();
    }
}
