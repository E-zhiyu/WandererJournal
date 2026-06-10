package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.wanderer.journal.databinding.DialogEdittextBinding;

public class EditTextDialogBuilder
        extends CustomDialogBuilderBase<EditTextDialogBuilder.OnClickedListener, DialogInterface.OnClickListener> {
    private DialogEdittextBinding binding;

    /**
     * 文本框对话框构造方法
     *
     * @param context     上下文
     * @param dialogTitle 对话框标题
     * @param hint        输入框的 hint 提示
     */
    public EditTextDialogBuilder(@NonNull Context context, String dialogTitle, String hint) {
        super(context, dialogTitle);
        binding.textLayout.setHint(hint);
    }

    @Override
    protected View getCustomView() {
        binding = DialogEdittextBinding.inflate(
                LayoutInflater.from(builder.getContext())
        );

        return binding.getRoot();
    }

    @Override
    public CustomDialogBuilderBase<OnClickedListener, DialogInterface.OnClickListener> setPositiveButton(
            String btnTitle,
            OnClickedListener callback
    ) {
        if (callback != null) {
            builder.setPositiveButton(btnTitle, (dialogInterface, i) ->
                    callback.onClicked(String.valueOf(binding.textInput.getText()))
            );
        } else {
            builder.setPositiveButton(btnTitle, null);
        }

        return this;
    }

    @Override
    public CustomDialogBuilderBase<OnClickedListener, DialogInterface.OnClickListener> setNegativeButton(String btnTitle, DialogInterface.OnClickListener callback) {
        builder.setNegativeButton(btnTitle, callback);
        return this;
    }

    public interface OnClickedListener {
        void onClicked(String inputStr);
    }
}
