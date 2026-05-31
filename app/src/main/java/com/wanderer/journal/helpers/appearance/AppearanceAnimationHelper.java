package com.wanderer.journal.helpers.appearance;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.auxiliary.enums.ViewTags;
import com.wanderer.journal.auxiliary.interfaces.PagingRecyclerScrollListener;
import com.wanderer.journal.auxiliary.interfaces.RecyclerViewScrollListener;
import com.wanderer.journal.ui.others.listeners.RecyclerScrollHideShowListener;
import com.wanderer.journal.ui.others.listeners.SpringAnimationOnTouchListener;

/**
 * 视图显示和动画帮助器
 */
public class AppearanceAnimationHelper {
    public static final int SMALL_CARD_RADIUS = 5;     //小卡片圆角大小（dp）
    public static final int MEDIUM_CARD_RADIUS = 20;   //中等卡片圆角大小（dp）

    /**
     * 设置下滑隐藏浮动按钮
     *
     * @param recyclerView 待检测下滑行为的RecyclerView
     * @param btn          需要隐藏的浮动按钮
     */
    public static void setupFloatingBtnBehaviour(@NonNull RecyclerView recyclerView, FloatingActionButton btn) {
        recyclerView.addOnScrollListener(new RecyclerScrollHideShowListener() {
            @Override
            public void onHide() {
                btn.hide();
            }

            @Override
            public void onShow() {
                btn.show();
            }
        });
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画（按下的圆角为8dp）
     *
     * @param view 目标视图 (如 MaterialButton, FAB 等)
     */
    public static void attachMorphAnimation(View view) {
        attachMorphAnimation(view, 0.4f);
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画（每个角分别计算圆角半径）
     *
     * @param view       目标视图 (如 MaterialButton, FAB 等)
     * @param percentage 按下时的圆角半径与初始圆角半径的比例 (单位: dp)
     */
    public static void attachMorphAnimation(View view, float percentage) {
        if (!(view instanceof Shapeable)) {
            throw new IllegalArgumentException("View must implement Shapeable");
        }

        Shapeable shapeableView = (Shapeable) view;
        Vibrator vibrator = (Vibrator) view.getContext()
                .getSystemService(Context.VIBRATOR_SERVICE);

        view.setOnTouchListener(new SpringAnimationOnTouchListener(shapeableView, vibrator, percentage));
    }

    /**
     * 设置视图四个边角大小
     *
     * @param context     上下文
     * @param view        实现了{@link Shapeable}的视图
     * @param topLeft     左上角圆角大小，单位dp
     * @param topRight    右上角圆角大小，单位dp
     * @param bottomLeft  左下角圆角大小，单位dp
     * @param bottomRight 右下角圆角大小，单位dp
     */
    public static void setRadius(
            Context context,
            @NonNull View view,
            float topLeft,
            float topRight,
            float bottomLeft,
            float bottomRight
    ) {
        //如果没有实现Shapeable接口，不执行任何操作
        if (!(view instanceof Shapeable)) {
            return;
        }

        Shapeable shapeable = (Shapeable) view;
        ShapeAppearanceModel model = shapeable.getShapeAppearanceModel();
        shapeable.setShapeAppearanceModel(model.toBuilder()
                .setTopLeftCornerSize(ViewEdgeHelper.dpToPx(context, topLeft))
                .setTopRightCornerSize(ViewEdgeHelper.dpToPx(context, topRight))
                .setBottomLeftCornerSize(ViewEdgeHelper.dpToPx(context, bottomLeft))
                .setBottomRightCornerSize(ViewEdgeHelper.dpToPx(context, bottomRight))
                .build()
        );
    }

    /**
     * 快速设置圆角样式
     *
     * @param view  需要设置圆角的视图
     * @param style 圆角种类
     */
    public static void setRadiusStyle(
            @NonNull View view,
            @NonNull RadiusStyle style
    ) {
        Context context = view.getContext();
        switch (style) {
            case TOP:
                setRadius(context, view, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS);
                break;
            case MIDDLE:
                setRadius(context, view, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS);
                break;
            case BOTTOM:
                setRadius(context, view, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS);
                break;
            case SINGLE:
                setRadius(context, view, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS);
                break;
        }
    }

    /**
     * 根据视图所在位置设置四个圆角的大小
     *
     * @param view     实现了{@link Shapeable}的视图
     * @param listSize 列表大小
     * @param position 当前视图所处的下标
     */
    public static void setRecyclerItemRadius(@NonNull View view, int listSize, int position) {
        //列表长度为1或者列表为空则设置为中等圆角
        if (listSize == 1 || listSize == 0) {
            setRadiusStyle(view, RadiusStyle.SINGLE);
            return;
        }

        //设置新的圆角值
        if (position == 0) {
            setRadiusStyle(view, RadiusStyle.TOP);
        } else if (position == listSize - 1) {
            setRadiusStyle(view, RadiusStyle.BOTTOM);
        } else {
            setRadiusStyle(view, RadiusStyle.MIDDLE);
        }
    }

    /**
     * 播放闪烁动画
     *
     * @param view 需要闪烁的视图
     */
    public static void blink(View view) {
        if (view == null) return;

        // 动态创建一个 GradientDrawable 作为闪烁层
        GradientDrawable blinkDrawable = new GradientDrawable();
        blinkDrawable.setShape(GradientDrawable.RECTANGLE);

        // 设置闪烁的高亮颜色
        int highlightColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorSecondaryContainer);
        blinkDrawable.setColor(highlightColor);

        // 同步复杂的圆角值
        if (view instanceof MaterialCardView) {
            // 如果是 MaterialCardView，它拥有完美的 ShapeAppearanceModel
            MaterialCardView cardView = (MaterialCardView) view;
            ShapeAppearanceModel shapeModel = cardView.getShapeAppearanceModel();

            // 此时，我们需要将 ShapeModel 中的四个角（TopLeft, TopRight, BottomRight, BottomLeft）
            // 转换为 GradientDrawable 需要的 float[] 数组。
            // GradientDrawable.setCornerRadii 需要一个包含 8 个 float 值的数组，
            // 分别对应 [TLx, TLy, TRx, TRy, BRx, BRy, BLx, BLy]

            RectF rect = new RectF(0, 0, view.getWidth(), view.getHeight());
            float topLeft = shapeModel.getTopLeftCornerSize().getCornerSize(rect);
            float topRight = shapeModel.getTopRightCornerSize().getCornerSize(rect);
            float bottomRight = shapeModel.getBottomRightCornerSize().getCornerSize(rect);
            float bottomLeft = shapeModel.getBottomLeftCornerSize().getCornerSize(rect);

            float[] radii = new float[]{
                    topLeft, topLeft,      // Top Left: x, y
                    topRight, topRight,    // Top Right: x, y
                    bottomRight, bottomRight, // Bottom Right: x, y
                    bottomLeft, bottomLeft   // Bottom Left: x, y
            };
            blinkDrawable.setCornerRadii(radii);

        } else {
            // 兜底方案：如果 View 不是 MaterialCardView，尝试获取 View 本身的 Outline
            blinkDrawable.setCornerRadius(0); // 暂时设为 0，防止未知形状
        }

        // 将 Drawable 添加到 Overlay
        blinkDrawable.setAlpha(0); // 初始全透明
        view.getOverlay().add(blinkDrawable);

        // 设置 Bounds (必须，否则不显示)
        // 考虑到 RecyclerView 滚动，最好用 post 确保 View 测量完成
        view.post(() -> blinkDrawable.setBounds(0, 0, view.getWidth(), view.getHeight()));

        // 播放 Alpha 属性动画
        ObjectAnimator animator = ObjectAnimator.ofInt(
                blinkDrawable,
                "alpha",
                0,   // 全透明
                100         //不设置为255，为了避免完全遮罩内容
        );

        animator.setDuration(400); // 闪亮过程
        animator.setRepeatCount(1); // 呼吸 1 次
        animator.setRepeatMode(ObjectAnimator.REVERSE); // 倒序回弹

        // 清理工作
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 必须移除，防止叠加和内存泄漏
                view.getOverlay().remove(blinkDrawable);
            }
        });

        animator.start();
    }

    /**
     * 将 RecyclerView平滑滚动到指定位置
     *
     * @param recyclerView        需要滚动的 RecyclerView
     * @param layoutManager       RecyclerView 的布局管理器
     * @param targetPosition      需要滚动到的目标下标
     * @param distanceThresholder 最大平滑滚动的距离，超出该距离先闪现到附近再平滑滚动
     * @param listener            滚动结果监听器
     */
    public static void scrollRecycler(
            @NonNull RecyclerView recyclerView,
            LinearLayoutManager layoutManager,
            int targetPosition,
            int distanceThresholder,
            RecyclerViewScrollListener listener
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
        int firstVisiblePos = layoutManager.findFirstVisibleItemPosition();
        int lastVisiblePos = layoutManager.findLastVisibleItemPosition();
        if (firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION) {
            //处理没有可见视图的情况
            if (listener != null) {
                listener.onFailed("没有可见视图");
            }
            return;
        } else if (targetPosition >= firstVisiblePos && targetPosition <= lastVisiblePos) {
            //处理不需要滚动的情况
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
            if (viewHolder != null) {
                AppearanceAnimationHelper.blink(viewHolder.itemView);
            }
            if (listener != null) {
                listener.onSucceed();
            }
            return;
        }

        //根据距离远近采用不同的滚动方式
        int distance = Math.abs(targetPosition - firstVisiblePos);
        if (distance > distanceThresholder) {
            //然后再平滑滚动
            recyclerView.post(() -> {
                Object tag = recyclerView.getTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT());
                if (tag instanceof RecyclerView.OnScrollListener) {
                    recyclerView.removeOnScrollListener((RecyclerView.OnScrollListener) tag);
                    recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                }
                //添加滚动监听器并平滑滚动
                RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        // 当滚动完全停止 (IDLE) 时再闪烁
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
                            if (viewHolder != null) {
                                AppearanceAnimationHelper.blink(viewHolder.itemView);
                            } else {
                                Log.w(LogTags.APPEARANCE_ANIMATION_HELPER.n(), "待闪烁的ViewHolder为null");
                            }

                            //移除滚动监听器防止用户滚动时触发闪烁
                            recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                            recyclerView.removeOnScrollListener(this);

                            recyclerView.post(() -> layoutManager.scrollToPositionWithOffset(
                                    targetPosition,
                                    0
                            ));
                        }
                    }
                };
                recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), scrollListener);
                recyclerView.addOnScrollListener(scrollListener);
                recyclerView.smoothScrollToPosition(targetPosition);
                Log.d(LogTags.APPEARANCE_ANIMATION_HELPER.n(), "平滑滚动目标：" + targetPosition);

                //瞬间滚动到附近
                recyclerView.postDelayed(() -> {
                            int momentPosition = targetPosition > firstVisiblePos ?
                                    targetPosition - distanceThresholder :
                                    targetPosition + distanceThresholder;
                            layoutManager.scrollToPositionWithOffset(momentPosition, 0);
                            Log.d(LogTags.APPEARANCE_ANIMATION_HELPER.n(), "滚动到附近：" + momentPosition);
                            },
                        300
                );
            });
        } else {
            Object tag = recyclerView.getTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT());
            if (tag instanceof RecyclerView.OnScrollListener) {
                recyclerView.removeOnScrollListener((RecyclerView.OnScrollListener) tag);
                recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
            }

            //添加滚动监听器并平滑滚动
            RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    // 当滚动完全停止 (IDLE) 时再闪烁
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
                        if (viewHolder != null) {
                            AppearanceAnimationHelper.blink(viewHolder.itemView);
                        }

                        //移除滚动监听器防止用户滚动时触发闪烁
                        recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                        recyclerView.removeOnScrollListener(this);

                        recyclerView.post(() -> layoutManager.scrollToPositionWithOffset(
                                targetPosition,
                                0
                        ));
                    }
                }
            };
            recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), scrollListener);
            recyclerView.addOnScrollListener(scrollListener);
            recyclerView.smoothScrollToPosition(targetPosition);
        }

        if (listener != null) {
            listener.onSucceed();
        }
    }

    /**
     * 滚动带有{@link PagingDataAdapter}类型适配器的{@link RecyclerView}
     *
     * @param recyclerView     需要滚动的 RecyclerView
     * @param layoutManager    RecyclerView 的布局管理器
     * @param adapter          RecyclerView 的适配器
     * @param targetPosition   需要滚动到的位置
     * @param maxRetryCount    最大重试次数
     * @param retryDelayMillis 重试时间间隔（毫秒）
     * @param listener         滚动状态监听器
     */
    public static void scrollPagingRecycler(
            RecyclerView recyclerView,
            LinearLayoutManager layoutManager,
            PagingDataAdapter<?, ?> adapter,
            int targetPosition,
            int maxRetryCount,
            int retryDelayMillis,
            PagingRecyclerScrollListener listener
    ) {
        if (layoutManager == null) {
            return;
        }

        recyclerView.post(() -> {
                    Object o;
                    try {
                        o = adapter.peek(targetPosition);
                    } catch (IndexOutOfBoundsException e) {
                        o = null;
                    }
                    if (o != null) {
                        // 已真实加载
                        scrollRecycler(
                                recyclerView,
                                layoutManager,
                                targetPosition,
                                10,
                                new RecyclerViewScrollListener() {
                                    @Override
                                    public void onSucceed() {
                                        listener.onSucceed();
                                    }

                                    @Override
                                    public void onFailed(String errMessage) {
                                        listener.onFailed();
                                    }
                                }
                        );
                    } else {
                        // 未加载成功，继续等待
                        recyclerView.postDelayed(new Runnable() {
                            private int failCount = 0;
                            private boolean scrollBottomOrTop = true;   //true:下次滚动到底部，false:下次滚动到顶部
                            private int retryTopCount = 0;      //重试滚动到顶部次数
                            private int retryBottomCount = 0;   //重试滚动到底部次数

                            @Override
                            public void run() {
                                //获取目标位置的数据实例，判断是否滚动成功
                                Object object;
                                try {
                                    object = adapter.peek(targetPosition);
                                } catch (IndexOutOfBoundsException e) {
                                    object = null;
                                }

                                //计算下次重试的滚动位置
                                int nextRetryPosition;
                                if (scrollBottomOrTop) {
                                    int bottom = adapter.getItemCount() - 1;
                                    nextRetryPosition = bottom - (bottom - targetPosition) * retryBottomCount / maxRetryCount;
                                    retryBottomCount++;
                                } else {
                                    nextRetryPosition = targetPosition * retryTopCount / maxRetryCount;
                                    retryTopCount++;
                                }

                                if (object != null) {
                                    scrollRecycler(
                                            recyclerView,
                                            layoutManager,
                                            targetPosition,
                                            10,
                                            new RecyclerViewScrollListener() {
                                                @Override
                                                public void onSucceed() {
                                                    listener.onSucceed();
                                                }

                                                @Override
                                                public void onFailed(String errMessage) {
                                                    listener.onFailed();
                                                }
                                            }
                                    );
                                } else if (failCount < maxRetryCount) {
                                    failCount++;
                                    listener.onRetry(failCount);

                                    //跳转到边界以触发加载
                                    layoutManager.scrollToPositionWithOffset(nextRetryPosition, 0);
                                    scrollBottomOrTop = !scrollBottomOrTop;

                                    recyclerView.postDelayed(this, retryDelayMillis);
                                } else {
                                    listener.onFailed();
                                }
                            }
                        }, 50);
                    }
                }
        );
    }
}
