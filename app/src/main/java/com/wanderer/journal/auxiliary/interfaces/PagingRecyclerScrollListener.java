package com.wanderer.journal.auxiliary.interfaces;

public interface PagingRecyclerScrollListener {
    void onSucceed();
    void onRetry(int failCount);
    void onFailed();
}
