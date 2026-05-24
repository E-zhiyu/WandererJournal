package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 自定义对话框构造器基类
 *
 * @param <P> 确认按钮点击回调类型
 * @param <N> 取消按钮点击回调类型
 */
public abstract class CustomDialogBuilderBase<P, N> {
    protected final MaterialAlertDialogBuilder builder; //对话框构造器

    /**
     * 自定义对话框构造器基类构造方法
     *
     * @param context     上下文
     * @param dialogTitle 对话框标题
     */
    public CustomDialogBuilderBase(@NonNull Context context, String dialogTitle) {
        builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(dialogTitle);

        //设置自定义视图
        View customView = getView();
        builder.setView(customView);
    }

    /**
     * 获取对话框视图
     *
     * @return 通过LayoutInflater获取的对话框视图
     */
    abstract protected View getView();

    /**
     * 设置确认按钮
     *
     * @param btnTitle 按钮的标题
     * @param callback 点击回调
     * @return 设置了确认按钮的对话框构造器
     */
    abstract public CustomDialogBuilderBase<P, N> setPositiveButton(String btnTitle, P callback);

    /**
     * 设置取消按钮
     *
     * @param btnTitle 按钮标题
     * @param callback 取消按钮的监听器
     * @return 设置了取消按钮的对话框构造器
     */
    abstract public CustomDialogBuilderBase<P, N> setNegativeButton(String btnTitle, N callback);

    /**
     * 显示对话框
     *
     * @return 显示的对话框
     */
    public AlertDialog show() {
        return builder.show();
    }

    /**
     * 创建对话框实例但不显示
     *
     * @return 对话框实例
     */
    public AlertDialog create() {
        return builder.create();
    }
}
