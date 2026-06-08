package com.wanderer.journal.ui.others.decoration.sticky;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.databinding.ViewHolderDateSeparatorBinding;

public class StickyHeaderItemDecoration extends RecyclerView.ItemDecoration {
    private final StickyHeaderAdapter adapter;
    private ViewHolderDateSeparatorBinding separatorBinding;
    private String lastTitle = "";  //上次显示的日期文本

    public StickyHeaderItemDecoration(StickyHeaderAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);

        if (parent.getChildCount() <= 0) return;

        // 1. 获取屏幕上可见第一个 View 的 Position
        View firstChild = parent.getChildAt(0);
        int firstVisiblePosition = parent.getChildAdapterPosition(firstChild);
        if (firstVisiblePosition == RecyclerView.NO_POSITION) return;

        // 2. 创建或更新头部 View
        String title = adapter.getHeaderTitle(firstVisiblePosition);
        if (title.isEmpty()) return;

        ensureHeaderView(parent, title);

        // 3. 计算粘性头部的 Y 轴坐标（处理“推开”效果）
        int headerTop = getHeaderTop(parent, firstChild, firstVisiblePosition);

        // 4. 绘制头部
        canvas.save();
        canvas.translate(0, headerTop);
        separatorBinding.getRoot().draw(canvas);
        canvas.restore();
    }

    /**
     * 确保 HeaderView 被正确创建和测量大小
     */
    private void ensureHeaderView(RecyclerView parent, String title) {
        if (separatorBinding == null) {
            // 实例化你自定义的日期分隔符布局（请替换为你自己的 layout id 和 view id）
            separatorBinding = ViewHolderDateSeparatorBinding.inflate(
                    LayoutInflater.from(parent.getContext())
            );
        }

        //两次文本不一样时重新渲染并测量布局
        if (!title.equals(lastTitle)) {
            lastTitle = title;
            separatorBinding.dateText.setText(title);

            // 必须手动测量和布局，因为它是脱离 RecyclerView 渲染树独立绘制的
            int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            separatorBinding.getRoot().measure(widthSpec, heightSpec);
            separatorBinding.getRoot().layout(
                    0,
                    0,
                    separatorBinding.getRoot().getMeasuredWidth(),
                    separatorBinding.getRoot().getMeasuredHeight()
            );
        }

    }

    /**
     * 计算 Top 坐标，实现下一个 Header 顶起当前 Header 的动画效果
     */
    private int getHeaderTop(@NonNull RecyclerView parent, View firstChild, int firstVisiblePosition) {
        int headerHeight = separatorBinding.getRoot().getHeight();
        int maxTop = 0;

        // 遍历当前屏幕上可见的 View，寻找下一个分隔符
        for (int i = 1; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (position != RecyclerView.NO_POSITION && adapter.isHeader(position)) {
                // 如果下一个分隔符快要碰到顶部固定的 Header 了
                if (child.getTop() < headerHeight) {
                    // 返回一个负值，随着滚动，把当前的 Header 往上推
                    return child.getTop() - headerHeight;
                }
                break;
            }
        }
        return maxTop;
    }
}
