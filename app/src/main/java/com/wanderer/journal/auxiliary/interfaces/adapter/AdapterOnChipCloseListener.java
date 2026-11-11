package com.wanderer.journal.auxiliary.interfaces.adapter;

public interface AdapterOnChipCloseListener<T> {
    /**
     * Chip 的 closeIcon 点击回调
     *
     * @param model 数据原型
     */
    void onClose(T model);
}
