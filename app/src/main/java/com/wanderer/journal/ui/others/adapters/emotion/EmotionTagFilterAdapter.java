package com.wanderer.journal.ui.others.adapters.emotion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnChipCloseListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.ViewHolderClosableChipBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmotionTagFilterAdapter extends ListAdapter<EmotionTagEntity, EmotionTagFilterAdapter.ItemViewHolder> {
    private static final DiffUtil.ItemCallback<EmotionTagEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull EmotionTagEntity oldItem, @NonNull EmotionTagEntity newItem) {
            return oldItem.getEmotionId() == newItem.getEmotionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmotionTagEntity oldItem, @NonNull EmotionTagEntity newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };
    private final List<Long> checkedEmotionIdSet = new ArrayList<>();   //被选择了的情绪标签的 ID 列表
    private final AdapterOnClickListener<EmotionTagEntity> clickListener;      //点击监听
    private final AdapterOnChipCloseListener<EmotionTagEntity> closeListener;    //关闭图标点击监听

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderClosableChipBinding binding;

        public ItemViewHolder(@NonNull ViewHolderClosableChipBinding binding, ChipListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置点击监听
            binding.chip.setOnClickListener(view -> {
                listener.onClick(getBindingAdapterPosition(), binding.chip);
                binding.chip.setChecked(true);
            });

            //设置关闭图标监听
            binding.chip.setOnCloseIconClickListener(view -> {
                listener.onClose(getBindingAdapterPosition());
                binding.chip.setChecked(false);
            });
        }
    }

    public interface ChipListener {
        void onClick(int position, View view);

        void onClose(int position);
    }

    /**
     * @param checkedEmotionIdSet 初始选中的情绪标签
     * @param clickListener       情绪标签点击监听
     * @param closeListener       情绪标签关闭图标点击监听
     */
    public EmotionTagFilterAdapter(Set<Long> checkedEmotionIdSet, AdapterOnClickListener<EmotionTagEntity> clickListener, AdapterOnChipCloseListener<EmotionTagEntity> closeListener) {
        super(ITEM_CALLBACK);
        this.checkedEmotionIdSet.addAll(checkedEmotionIdSet);
        this.clickListener = clickListener;
        this.closeListener = closeListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderClosableChipBinding binding = ViewHolderClosableChipBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ItemViewHolder(
                binding,
                new ChipListener() {
                    @Override
                    public void onClick(int position, View view) {
                        EmotionTagEntity emotionTag = getItem(position);
                        clickListener.onClick(emotionTag, view);
                    }

                    @Override
                    public void onClose(int position) {
                        closeListener.onClose(getItem(position));
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        EmotionTagEntity emotionTag = getItem(position);

        //名称文本
        holder.binding.chip.setText(emotionTag.getName());

        //设置选择状态
        if (checkedEmotionIdSet.contains(emotionTag.getEmotionId())) {
            holder.binding.chip.setChecked(true);
        }
    }
}
