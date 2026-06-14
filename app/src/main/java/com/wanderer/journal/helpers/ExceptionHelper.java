package com.wanderer.journal.helpers;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.helpers.text.TextHelper;

import java.util.Locale;

public class ExceptionHelper {
    public static void showExceptionDialog(Context context, @NonNull Throwable e) {
        String errMessage = e.getMessage();
        new MaterialAlertDialogBuilder(context)
                .setTitle("运行出错")
                .setMessage(errMessage)
                .setPositiveButton("复制错误信息", (dialog, which) -> {
                    copyToClipboard(context, errMessage);
                    Toast.makeText(context, "已将错误信息复制到剪贴板", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)   //无法点击空白区域取消
                .setNegativeButton("关闭", null)
                .show();
    }

    /**
     * 将文本复制到剪贴板
     *
     * @param context 上下文（Activity 或 Application Context）
     * @param text    要复制的文本
     */
    private static void copyToClipboard(@NonNull Context context, String text) {
        //创建 ClipData 对象
        String label = String.format(
                Locale.getDefault(),
                "错误信息(%s)",
                context.getPackageName()
        );
        TextHelper.copyToClipBoard(context, label, text);
    }
}
