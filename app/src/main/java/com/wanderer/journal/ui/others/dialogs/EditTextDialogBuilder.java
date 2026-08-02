package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.wanderer.journal.databinding.DialogEdittextBinding;
import com.wanderer.journal.helpers.ImmHelper;

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
        this(context, dialogTitle, hint, "");
    }

    public EditTextDialogBuilder(Context context, String dialogTitle, String hint, String initText) {
        super(context, dialogTitle);
        binding.textLayout.setHint(hint);
        binding.textInput.setText(initText);
    }

    /**
     * 设置输入种类
     *
     * @param type 输入种类，具体参考{@link InputType}中的静态变量
     */
    public EditTextDialogBuilder setInputType(int type) {
        binding.textInput.setInputType(type);
        return this;
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

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        ImmHelper.showImm(binding.textInput);
        return dialog;
    }

    public interface OnClickedListener {
        void onClicked(String inputStr);
    }
}
