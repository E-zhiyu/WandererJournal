package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.databinding.DialogMultichoiceBinding;
import com.wanderer.journal.ui.others.adapters.MultiChoiceDialogAdapter;

import java.util.List;

public class MultiChoiceDialogBuilder
        extends CustomDialogBuilderBase<MultiChoiceDialogBuilder.OnConfirmedListener, MultiChoiceDialogBuilder.OnConfirmedListener> {
    private final MultiChoiceDialogAdapter adapter; //多选列表适配器
    private RecyclerView itemListRecycler;          //选项列表视图

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
     * 自定义对话框构造器基类构造方法
     *
     * @param context     上下文
     * @param dialogTitle 对话框标题
     */
    public MultiChoiceDialogBuilder(Context context,
                                    String dialogTitle,
                                    List<ChoiceItem> itemList
    ) {
        super(context, dialogTitle);

        //实例化多选列表的适配器
        adapter = new MultiChoiceDialogAdapter(itemList);
        itemListRecycler.setAdapter(adapter);
    }

    @Override
    protected View getView() {
        DialogMultichoiceBinding binding = DialogMultichoiceBinding.inflate(
                LayoutInflater.from(builder.getContext())
        );

        //获取多选选项列表
        itemListRecycler = binding.itemRecycler;

        return binding.getRoot();
    }

    @Override
    public CustomDialogBuilderBase<OnConfirmedListener, OnConfirmedListener> setPositiveButton(String btnTitle, OnConfirmedListener callback) {
        if (callback != null) {
            builder.setPositiveButton(btnTitle, (dialogInterface, i) ->
                    callback.onConfirmed(adapter.getCheckedStatList())
            );
        } else {
            builder.setPositiveButton(btnTitle, null);
        }
        return this;
    }

    @Override
    public CustomDialogBuilderBase<OnConfirmedListener, OnConfirmedListener> setNegativeButton(String btnTitle, OnConfirmedListener callback) {
        if (callback != null) {
            builder.setNegativeButton(btnTitle, (dialogInterface, i) ->
                    callback.onConfirmed(adapter.getCheckedStatList())
            );
        } else {
            builder.setNegativeButton(btnTitle, null);
        }
        return this;
    }
}
