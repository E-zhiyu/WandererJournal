package com.wanderer.journal.ui.others.decoration.sticky;

public interface StickyHeaderAdapter<T> {
    /**
     * 判断指定的位置是否为粘性头部视图
     *
     * @param position 视图的位置
     * @return 是否为粘性头部视图
     */
    boolean isHeader(int position);

    /**
     * 获取对应位置下粘性头部视图的文本
     *
     * @param position 视图位置
     * @return 粘性头部视图的文本
     */
    T getHeaderData(int position);
}
