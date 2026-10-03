package com.wanderer.journal.helpers.appearance;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.auxiliary.enums.unique.LogTags;
import com.wanderer.journal.auxiliary.enums.unique.ViewTags;
import com.wanderer.journal.ui.others.scroller.CustomOffsetSmoothScroller;

import java.lang.ref.WeakReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class ScrollHelper {
    public interface PagingRecyclerScrollListener {
        void onSucceed(int successPosition);

        void onRetry(int count);

        void onFailed();
    }

    public interface RecyclerViewScrollListener {
        void onSucceed();

        void onFailed(String errMessage);
    }

    /**
     * 内部滚动任务，管理 LoadState 监听器和轮询降级
     */
    private static class PagingScrollTask implements Runnable {
        private final WeakReference<RecyclerView> recyclerViewRef;
        private final WeakReference<LinearLayoutManager> layoutManagerRef;
        private final WeakReference<PagingDataAdapter<?, ?>> adapterRef;
        private final int targetPosition;
        private final int offset;
        private final int maxRetryCount;
        private final int retryDelayMillis;
        private final PagingRecyclerScrollListener listener;
        private final Runnable pageUpdatedTask = this::checkAndScroll;  //触发 pageUpdatedListener 后执行的代码
        private Function0<Unit> pageUpdatedListener;
        private int retryCount = 0;
        private boolean isFinished = false;

        /**
         * 滚动到未加载的分页列表的任务
         *
         * @param recyclerView     列表视图
         * @param layoutManager    线性布局管理器
         * @param adapter          分页加载适配器
         * @param targetPosition   目标下标
         * @param offset           偏移量 (px)
         * @param maxRetryCount    重定向的最大重试次数
         * @param retryDelayMillis 两次重试的间隔时间
         * @param listener         滚动结果监听器
         */
        PagingScrollTask(
                RecyclerView recyclerView,
                LinearLayoutManager layoutManager,
                PagingDataAdapter<?, ?> adapter,
                int targetPosition,
                int offset,
                int maxRetryCount,
                int retryDelayMillis,
                PagingRecyclerScrollListener listener
        ) {
            this.recyclerViewRef = new WeakReference<>(recyclerView);
            this.layoutManagerRef = new WeakReference<>(layoutManager);
            this.adapterRef = new WeakReference<>(adapter);
            this.targetPosition = targetPosition;
            this.offset = offset;
            this.maxRetryCount = maxRetryCount;
            this.retryDelayMillis = retryDelayMillis;
            this.listener = listener;
        }

        /**
         * 开始滚动
         */
        public void start() {
            PagingDataAdapter<?, ?> adapter = adapterRef.get();
            RecyclerView recyclerView = recyclerViewRef.get();
            if (adapter == null || recyclerView == null) {
                listener.onFailed();
                return;
            }

            // 监听 Paging3 的加载状态变化
            pageUpdatedListener = () -> {
                recyclerView.removeCallbacks(pageUpdatedTask);
                recyclerView.postDelayed(pageUpdatedTask, 200);
                return Unit.INSTANCE;
            };
            adapter.addOnPagesUpdatedListener(pageUpdatedListener);

            // 首次向目标方向推动一次滚动（触发该方向的 Page 加载）
            triggerFetchTowardsTarget();

            // 启动定时检查作为兜底保护
            recyclerView.postDelayed(this, retryDelayMillis);
        }

        @Override
        public void run() {
            if (isFinished) return;

            RecyclerView recyclerView = recyclerViewRef.get();
            PagingDataAdapter<?, ?> adapter = adapterRef.get();

            if (recyclerView == null || adapter == null) {
                cleanup();
                listener.onFailed();
                return;
            }

            if (checkAndScroll()) {
                return;
            }

            if (retryCount < maxRetryCount) {
                retryCount++;
                listener.onRetry(retryCount);

                //重新尝试向目标方向引导
                triggerFetchTowardsTarget();

                recyclerView.postDelayed(this, retryDelayMillis);
            } else {
                cleanup();
                listener.onFailed();
            }
        }

        /**
         * 滚动检查是否滚动到目标位置
         *
         * @return 滚动后是否到达目标位置
         */
        private boolean checkAndScroll() {
            if (isFinished) return true;

            PagingDataAdapter<?, ?> adapter = adapterRef.get();
            RecyclerView recyclerView = recyclerViewRef.get();
            LinearLayoutManager layoutManager = layoutManagerRef.get();

            if (adapter != null && recyclerView != null && layoutManager != null) {
                if (isPositionLoaded(adapter, targetPosition)) {
                    cleanup();
                    scrollRecycler(recyclerView, layoutManager, targetPosition, 10, offset, new RecyclerViewScrollListener() {
                        @Override
                        public void onSucceed() {
                            listener.onSucceed(targetPosition);
                        }

                        @Override
                        public void onFailed(String errMessage) {
                            listener.onFailed();
                        }
                    });
                    return true;
                }
            }
            return false;
        }

        /**
         * 向目标方向逼近，触发 Paging3 加载目标区域的 Page
         */
        private void triggerFetchTowardsTarget() {
            LinearLayoutManager layoutManager = layoutManagerRef.get();
            PagingDataAdapter<?, ?> adapter = adapterRef.get();
            if (layoutManager == null || adapter == null) return;

            int itemCount = adapter.getItemCount();
            if (itemCount == 0) return;

            // 如果 targetPosition 在可视区域下方，跳到当前已知最靠下的位置以触发向下加载
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            if (targetPosition > lastVisible) {
                layoutManager.scrollToPosition(itemCount - 1);
            }
            // 如果 targetPosition 在可视区域上方，跳到当前已知最靠上的位置
            else if (targetPosition < firstVisible) {
                layoutManager.scrollToPosition(0);
            }
        }

        /**
         * 清理（页面加载监听器等）
         */
        private void cleanup() {
            isFinished = true;
            PagingDataAdapter<?, ?> adapter = adapterRef.get();
            RecyclerView recyclerView = recyclerViewRef.get();

            if (adapter != null && pageUpdatedListener != null) {
                adapter.removeOnPagesUpdatedListener(pageUpdatedListener);
            }
            if (recyclerView != null) {
                recyclerView.removeCallbacks(this);
            }
        }
    }

    /**
     * 将 RecyclerView平滑滚动到指定位置
     *
     * @param recyclerView        需要滚动的 RecyclerView
     * @param layoutManager       RecyclerView 的布局管理器
     * @param targetPosition      需要滚动到的目标下标
     * @param distanceThresholder 最大平滑滚动的距离，超出该距离先闪现到附近再平滑滚动
     * @param offset              滚动结束后目标视图与 RecyclerView顶部的距离(px)
     * @param listener            滚动结果监听器
     */
    public static void scrollRecycler(
            @NonNull RecyclerView recyclerView,
            LinearLayoutManager layoutManager,
            int targetPosition,
            int distanceThresholder,
            int offset,
            @Nullable RecyclerViewScrollListener listener
    ) {
        //判断布局管理器
        if (layoutManager == null) {
            if (listener != null) {
                listener.onFailed("布局管理器为空");
            }
            return;
        }

        //判断位置是否有效
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null) {
            if (listener != null) {
                listener.onFailed("无法获取适配器");
            }
            return;
        } else if (targetPosition < 0 || targetPosition >= adapter.getItemCount()) {
            if (listener != null) {
                listener.onFailed("目标位置无效");
            }
            return;
        }

        //获取可见位置并比较
        int firstCompletePos = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastCompletePos = layoutManager.findLastCompletelyVisibleItemPosition();
        int firstVisiblePos = firstCompletePos == RecyclerView.NO_POSITION ?
                layoutManager.findFirstVisibleItemPosition() :
                firstCompletePos;
        int lastVisiblePos = lastCompletePos == RecyclerView.NO_POSITION ?
                layoutManager.findLastVisibleItemPosition() :
                lastCompletePos;
        if (firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION) {
            //处理没有可见视图的情况
            if (listener != null) {
                listener.onFailed("没有可见视图");
            }
            return;
        }

        //根据距离远近采用不同的滚动方式
        int distance = Math.abs(targetPosition - firstVisiblePos);
        if (distance > distanceThresholder) {
            //然后再平滑滚动
            recyclerView.post(() -> {
                //移除旧的滚动监听器
                Object tag = recyclerView.getTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT());
                if (tag instanceof RecyclerView.OnScrollListener) {
                    recyclerView.removeOnScrollListener((RecyclerView.OnScrollListener) tag);
                    recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                }

                //添加滚动监听器
                RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        // 当滚动完全停止 (IDLE) 时再闪烁
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            //闪烁视图以提醒用户
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
                            if (viewHolder != null) {
                                AnimationHelper.blink(viewHolder.itemView);
                            } else {
                                Log.w(LogTags.SCROLL_HELPER.n(), "待闪烁的ViewHolder为null");
                            }

                            //移除滚动监听器防止用户滚动时触发闪烁
                            recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                            recyclerView.removeOnScrollListener(this);
                        }
                    }
                };
                recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), scrollListener);
                recyclerView.addOnScrollListener(scrollListener);

                //开始平滑滚动
                CustomOffsetSmoothScroller scroller = new CustomOffsetSmoothScroller(
                        recyclerView.getContext(),
                        offset
                );
                scroller.setTargetPosition(targetPosition);
                layoutManager.startSmoothScroll(scroller);

                //一定时间后瞬间滚动到附近以缩短行程
                recyclerView.post(() -> {
                    int momentPosition = targetPosition > firstVisiblePos ?
                            targetPosition - distanceThresholder :
                            targetPosition + distanceThresholder;
                    layoutManager.scrollToPositionWithOffset(momentPosition, 0);
                });
            });
        } else {
            //移除旧的滚动监听器
            Object tag = recyclerView.getTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT());
            if (tag instanceof RecyclerView.OnScrollListener) {
                recyclerView.removeOnScrollListener((RecyclerView.OnScrollListener) tag);
                recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
            }

            //添加滚动监听器
            RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    // 当滚动完全停止 (IDLE) 时再闪烁
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        //闪烁视图以提醒用户
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
                        if (viewHolder != null) {
                            AnimationHelper.blink(viewHolder.itemView);
                        }

                        //移除滚动监听器防止用户滚动时触发闪烁
                        recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                        recyclerView.removeOnScrollListener(this);
                    }
                }
            };
            recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), scrollListener);
            recyclerView.addOnScrollListener(scrollListener);

            //开始平滑滚动
            CustomOffsetSmoothScroller scroller = new CustomOffsetSmoothScroller(
                    recyclerView.getContext(),
                    offset
            );
            scroller.setTargetPosition(targetPosition);
            layoutManager.startSmoothScroll(scroller);
        }

        if (listener != null) {
            listener.onSucceed();
        }
    }

    /**
     * 滚动带有{@link PagingDataAdapter}类型适配器的{@link RecyclerView}
     *
     * @param recyclerView   需要滚动的 RecyclerView
     * @param layoutManager  RecyclerView 的布局管理器
     * @param adapter        RecyclerView 的适配器
     * @param targetPosition 需要滚动到的位置
     * @param offset         滚动结束后目标视图到 RecyclerView 顶部的距离
     * @param maxRetryCount  最大重试次数
     * @param listener       滚动状态监听器
     */
    public static void scrollPagingRecycler(
            @NonNull RecyclerView recyclerView,
            @NonNull LinearLayoutManager layoutManager,
            @NonNull PagingDataAdapter<?, ?> adapter,
            int targetPosition,
            int offset,
            int maxRetryCount,
            @NonNull PagingRecyclerScrollListener listener
    ) {
        //处理无内容的情况
        if (adapter.getItemCount() == 0) {
            listener.onFailed();
            return;
        }

        //处理越界情况
        if (targetPosition < 0) {
            targetPosition = 0;
        } else if (targetPosition >= adapter.getItemCount()) {
            targetPosition = adapter.getItemCount() - 1;
        }

        int secureTarget = targetPosition;
        recyclerView.post(() -> {
            // 1. 如果目标位置的数据已经加载，直接精确滚动
            if (isPositionLoaded(adapter, secureTarget)) {
                scrollRecycler(recyclerView, layoutManager, secureTarget, 10, offset, new RecyclerViewScrollListener() {
                    @Override
                    public void onSucceed() {
                        listener.onSucceed(secureTarget);
                    }

                    @Override
                    public void onFailed(String errMessage) {
                        listener.onFailed();
                    }
                });
                return;
            }

            // 2. 数据尚未加载，启动 LoadState 监听 + 引导式滚动机制
            PagingScrollTask task = new PagingScrollTask(
                    recyclerView,
                    layoutManager,
                    adapter,
                    secureTarget,
                    offset,
                    maxRetryCount,
                    3000,
                    listener
            );
            task.start();
        });
    }

    /**
     * 判断指定位置的数据是否真正加载（排除超出范围和未加载的占位符）
     */
    private static boolean isPositionLoaded(PagingDataAdapter<?, ?> adapter, int position) {
        if (position < 0 || position >= adapter.getItemCount()) {
            return false;
        }
        try {
            return adapter.peek(position) != null;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
}
