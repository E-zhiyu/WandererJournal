package com.wanderer.journal.ui.others.method;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class FallbackLinkMovementMethod extends LinkMovementMethod {
    private static FallbackLinkMovementMethod sInstance;

    public static FallbackLinkMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new FallbackLinkMovementMethod();
        }
        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            // 获取点击位置的 ClickableSpan
            ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

            if (links.length != 0) {
                // 如果点到了 ClickableSpan，执行原有的逻辑（消费事件）
                if (action == MotionEvent.ACTION_UP) {
                    links[0].onClick(widget);
                } else {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(links[0]),
                            buffer.getSpanEnd(links[0]));
                }
                return true;
            } else {
                // 如果没点到 ClickableSpan，清除选择状态，并返回 false 允许事件传递给父布局
                Selection.removeSelection(buffer);
                return false;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}
