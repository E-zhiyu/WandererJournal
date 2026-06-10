package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.wanderer.journal.databinding.DialogProgressBinding;

import java.util.Locale;

public class ProgressDialogBuilder
        extends CustomDialogBuilderBase<DialogInterface.OnClickListener, DialogInterface.OnClickListener> {
    private LinearProgressIndicator progressIndicator;  //进度条
    private MaterialTextView progressText;              //进度文本
    private MaterialTextView subTitleText;              //底部副标题文本
    private final String originSubTitle;                //对话框底部副标题

    public ProgressDialogBuilder(Context context, String dialogTitle, @Nullable String originSubTitle) {
        super(context, dialogTitle);
        this.originSubTitle = originSubTitle == null ? "" : originSubTitle;
    }

    @Override
    protected View getCustomView() {
        //创建自定义布局
        DialogProgressBinding binding = DialogProgressBinding.inflate(
                LayoutInflater.from(builder.getContext())
        );

        progressIndicator = binding.progressIndicator;
        progressText = binding.progressText;
        subTitleText = binding.subTitleText;

        //默认为不确定进度模式
        setIndeterminate(true);

        return binding.getRoot();
    }

    @Override
    public CustomDialogBuilderBase<DialogInterface.OnClickListener, DialogInterface.OnClickListener> setPositiveButton(String btnTitle, DialogInterface.OnClickListener callback) {
        builder.setPositiveButton(btnTitle, callback);
        return this;
    }

    @Override
    public CustomDialogBuilderBase<DialogInterface.OnClickListener, DialogInterface.OnClickListener> setNegativeButton(String btnTitle, DialogInterface.OnClickListener callback) {
        builder.setNegativeButton(btnTitle, callback);
        return this;
    }

    @Override
    public AlertDialog show() {
        subTitleText.setText(originSubTitle);
        return super.show();
    }

    /**
     * 更新进度
     *
     * @param current  当前数量
     * @param total    总数量
     * @param subTitle 对话框底部副标题
     */
    public void updateProgress(int current, int total, String subTitle) {
        if (progressIndicator != null) {
            //设置进度（0-100）
            int progressPercent = (int) ((current / (float) total) * 100);
            progressIndicator.setProgressCompat(progressPercent, true);

            //更新文本
            progressText.setText(String.format(Locale.getDefault(),
                    "%d/%d (%d%%)", current, total, progressPercent));

            //更新副标题
            subTitleText.setText(subTitle != null ? subTitle : this.originSubTitle);
        }
    }

    /**
     * 设置为不确定模式（用于初始阶段）
     *
     * @param indeterminate 是否为不确定模式
     */
    public void setIndeterminate(boolean indeterminate) {
        //如果当前状态与目标状态相同，则不执行任何操作
        if (progressIndicator.isIndeterminate() == indeterminate) {
            return;
        }

        //修改状态
        if (progressIndicator != null) {
            progressIndicator.setIndeterminate(indeterminate);

            if (indeterminate) {
                progressText.setVisibility(View.GONE);
            } else {
                progressText.setVisibility(View.VISIBLE);
            }
        }
    }
}
