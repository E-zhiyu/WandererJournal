package com.wanderer.journal.ui.pages.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.wanderer.journal.data.save.db.entities.composite.DiaryParagraphCountModel;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class MonthHeaderDecoration extends RecyclerView.ItemDecoration {

    private final List<DiaryParagraphCountModel> dataList;
    private final Paint paint;
    private final int topMargin;

    public MonthHeaderDecoration(List<DiaryParagraphCountModel> dataList, Context context) {
        this.dataList = dataList;
        this.topMargin = dpToPx(16); // 顶部留出 16dp 放文字

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(MaterialColors.getColor( // 文字颜色
                context,
                com.google.android.material.R.attr.colorOutline,
                Color.parseColor("#57606A")
        ));
        paint.setTextSize(dpToPx(9)); // 文字大小 9sp
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        // 如果 RecyclerView 还没设置 paddingTop，我们动态加一个，只加一次
        if (parent.getPaddingTop() != topMargin) {
            // 开启 clipToPadding=false 允许文字画在 padding 区域
            parent.setClipToPadding(false);
            parent.setPadding(parent.getPaddingLeft(), topMargin, parent.getPaddingRight(), parent.getPaddingBottom());
        }

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (position == RecyclerView.NO_POSITION || position >= dataList.size()) continue;

            DiaryParagraphCountModel item = dataList.get(position);
            if (item == null) continue;

            LocalDate date = item.getDiaryDate();

            // 周一 且 该月第一周
            if (date.getDayOfWeek().getValue() == 1 && date.getDayOfMonth() <= 7) {
                String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());

                float x = child.getLeft();
                // Y轴固定在顶部 padding 的区间内（不受方块位置影响）
                float y = topMargin - dpToPx(4);

                c.drawText(monthName, x, y, paint);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }
}