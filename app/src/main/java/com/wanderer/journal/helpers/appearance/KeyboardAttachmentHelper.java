package com.wanderer.journal.helpers.appearance;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyboardAttachmentHelper {
    private final View rootView;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;
    private Runnable runnable = null;   // rootView.postDelayed() 执行的 Runnable 对象
    private int previousHeightDiff = 0;

    public interface OnKeyboardHeightChangedListener {
        /**
         * @param currentHeight  当前键盘高度 (px)
         * @param previousHeight 上一次的键盘高度 (px)
         */
        void onHeightChanged(int currentHeight, int previousHeight);
    }

    public KeyboardAttachmentHelper(View rootView) {
        this.rootView = rootView;
    }

    /**
     * 开启全局布局监听（专门应对小窗、分屏或失去焦点时的键盘变动）
     */
    public void startLegacyTracking(OnKeyboardHeightChangedListener listener) {
        if (globalLayoutListener != null) return;

        globalLayoutListener = () -> {
            if (runnable != null) {
                rootView.removeCallbacks(runnable);
            }
            runnable = () -> notifyUi(listener);
            rootView.postDelayed(runnable, 20);
        };

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    /**
     * 注销监听，防止内存泄漏
     */
    public void stopTracking() {
        if (globalLayoutListener != null && rootView != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
            globalLayoutListener = null;
        }
    }

    /**
     * 通知界面更新布局
     *
     * @param listener 布局更新监听器
     */
    private void notifyUi(OnKeyboardHeightChangedListener listener) {
        Rect r = new Rect();
        // 获取当前窗口在屏幕上的实际可视区域
        rootView.getWindowVisibleDisplayFrame(r);

        // 获取屏幕总高度
        int screenHeight = rootView.getRootView().getHeight();

        // 计算键盘（或其他遮挡物）占据的高度
        int heightDiff = screenHeight - r.bottom;

        // 设定一个阈值（比如100dp），排除系统导航栏等微小变化
        int threshold = (int) (100 * rootView.getContext().getResources().getDisplayMetrics().density);

        if (heightDiff < threshold) {
            heightDiff = 0; // 键盘收起
        }

        // 如果高度发生变化，通知回调
        if (heightDiff != previousHeightDiff) {
            if (listener != null) {
                listener.onHeightChanged(heightDiff, previousHeightDiff);
            }
            previousHeightDiff = heightDiff;
        }

        runnable = null;
    }
}
