package com.wanderer.journal.ui.others.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.databinding.ViewHolderMultichoiceItemBinding;
import com.wanderer.journal.ui.others.dialogs.MultiChoiceDialog;

import java.util.List;
import java.util.stream.Collectors;

public class MultiChoiceDialogAdapter extends RecyclerView.Adapter<MultiChoiceDialogAdapter.MultiChoiceItemViewHolder> {
    private final List<MultiChoiceDialog.ChoiceItem> itemList;  //选项列表
    private final List<Boolean> checkedStatList;                //选项选择状态列表，与实际 UI 应保持一致

    public static class MultiChoiceItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMultichoiceItemBinding binding;

        public MultiChoiceItemViewHolder(@NonNull ViewHolderMultichoiceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 不设置是否启用可选项的构造方法
     *
     * @param itemList 选项列表
     */
    public MultiChoiceDialogAdapter(@NonNull List<MultiChoiceDialog.ChoiceItem> itemList) {
        this.itemList = itemList;
        this.checkedStatList = itemList.stream()
                .map(MultiChoiceDialog.ChoiceItem::getInitStat)
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public MultiChoiceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderMultichoiceItemBinding binding = ViewHolderMultichoiceItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MultiChoiceItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiChoiceItemViewHolder holder, int position) {
        MultiChoiceDialog.ChoiceItem item = itemList.get(position);
        boolean itemEnabled = item.isEnabled();
        boolean stat = item.getInitStat();
        String itemTitle = item.getTitle();

        //设置选项的可用性
        holder.binding.checkedText.setText(itemTitle);
        if (!itemEnabled) {
            //禁用选项
            holder.binding.checkedText.setEnabled(false);
            holder.binding.checkedText.setChecked(false);

            //图标也变成灰色
            TextViewCompat.setCompoundDrawableTintList(holder.binding.checkedText, ColorStateList.valueOf(Color.GRAY));
        } else {
            holder.binding.checkedText.setChecked(stat);
        }

        //绑定复选框的选择监听器
        holder.binding.checkedText.setOnClickListener(
                view -> {
                    holder.binding.checkedText.toggle();    //更新 UI
                    checkedStatList.set(position, holder.binding.checkedText.isChecked());  //更新选中状态
                }
        );
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public List<Boolean> getCheckedStatList() {
        return checkedStatList;
    }
}
