package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.wanderer.journal.databinding.DialogEdittextBinding;

public class EditTextDialogBuilder
        extends CustomDialogBuilderBase<
        DialogEdittextBinding,
        EditTextDialogBuilder.OnClickedListener,
        DialogInterface.OnClickListener
        > {
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
    protected DialogEdittextBinding getCustomView() {
        return DialogEdittextBinding.inflate(
                LayoutInflater.from(builder.getContext())
        );
    }

    @Override
    public CustomDialogBuilderBase<DialogEdittextBinding, OnClickedListener, DialogInterface.OnClickListener> setPositiveButton(
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
    public CustomDialogBuilderBase<DialogEdittextBinding, OnClickedListener, DialogInterface.OnClickListener> setNegativeButton(String btnTitle, DialogInterface.OnClickListener callback) {
        builder.setNegativeButton(btnTitle, callback);
        return this;
    }

    public interface OnClickedListener {
        void onClicked(String inputStr);
    }
}
