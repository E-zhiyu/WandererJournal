package com.wanderer.journal.ui.others.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.ui.others.adapters.MultiChoiceDialogAdapter;

import java.util.List;

/**
 * 可禁用选项的多选对话框
 */
public class MultiChoiceDialog extends CustomDialogBase {
    private final List<ChoiceItem> itemList;            //选项列表
    private final OnConfirmedListener confirmListener;  //确认按钮点击监听
    private MultiChoiceDialogAdapter adapter;           //多选列表适配器

    public static class ChoiceItem {
        private final boolean initStat;   //初始状态
        private final String title;       //显示名称
        private final boolean enabled;    //是否启用

        public ChoiceItem(boolean initStat, String title, boolean enabled) {
            this.initStat = initStat;
            this.title = title;
            this.enabled = enabled;
        }

        public boolean getInitStat() {
            return initStat;
        }

        public String getTitle() {
            return title;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public interface OnConfirmedListener {
        /**
         * 确认按钮点击回调
         *
         * @param checkedStatList 选项选择状态
         */
        void onConfirmed(List<Boolean> checkedStatList);
    }

    /**
     * 允许禁用选项的构造方法
     *
     * @param context         上下文
     * @param dialogTitle     对话框标题
     * @param itemList        选项列表
     * @param confirmListener 确认按钮点击监听
     */
    public MultiChoiceDialog(
            Context context,
            String dialogTitle,
            List<ChoiceItem> itemList,
            OnConfirmedListener confirmListener
    ) {
        super(context, dialogTitle);
        this.itemList = itemList;
        this.confirmListener = confirmListener;
    }

    @Override
    protected View getView() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_multichoice, null);

        //多选选项列表
        RecyclerView itemListView = view.findViewById(R.id.item_recycler);
        adapter = new MultiChoiceDialogAdapter(itemList);
        itemListView.setAdapter(adapter);

        return view;
    }

    public void buildDialog(
            @Nullable OnCancelListener cancelListener,
            boolean isCancelable
    ) {
        View dialogView = getView();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(dialogTitle)
                .setView(dialogView)
                .setCancelable(isCancelable)
                .setPositiveButton("确定", (dialog1, which) ->
                        confirmListener.onConfirmed(adapter.getCheckedStatList())
                );


        if (cancelListener != null) {
            builder.setNegativeButton("取消", (dialog1, which) -> cancelListener.onCancel());
        }

        dialog = builder.create();
    }
}
