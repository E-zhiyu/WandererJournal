package com.wanderer.journal.ui.others.adapters.emotion;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.ViewHolderClosableChipBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmotionTagFilterAdapter extends ListAdapter<EmotionTagEntity, EmotionTagFilterAdapter.EmotionTagFilterViewHolder> {
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
    private final List<Long> checkedEmotionIdSet = new ArrayList<>();  //被选择了的情绪标签的 ID 列表
    private final OnCheckedChangedListener checkedChangedListener;      //选择状态变化监听器

    public static class EmotionTagFilterViewHolder extends RecyclerView.ViewHolder {
        ViewHolderClosableChipBinding binding;
        private boolean isBlocked;

        public EmotionTagFilterViewHolder(@NonNull ViewHolderClosableChipBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.chip.setCheckable(true);            //设置为可点击
            binding.chip.setCloseIconVisible(false);    //隐藏关闭图标

            binding.chip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (isBlocked) {
                    return;
                }

                listener.onCheckChanged(getBindingAdapterPosition(), b);
            });
        }
    }

    public interface OnCheckedChangedListener {
        /**
         * 标签选中状态变更回调
         *
         * @param emotionTag 改变选中状态的情绪标签
         * @param isChecked  是否被选中
         */
        void onCheckChanged(EmotionTagEntity emotionTag, boolean isChecked);
    }

    public interface ViewHolderListener {
        void onCheckChanged(int position, boolean isChecked);
    }

    public EmotionTagFilterAdapter(Set<Long> checkedEmotionIdSet, OnCheckedChangedListener checkedChangedListener) {
        super(ITEM_CALLBACK);
        this.checkedEmotionIdSet.addAll(checkedEmotionIdSet);
        this.checkedChangedListener = checkedChangedListener;
    }

    @NonNull
    @Override
    public EmotionTagFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderClosableChipBinding binding = ViewHolderClosableChipBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new EmotionTagFilterViewHolder(
                binding,
                (position, isChecked) -> {
                    EmotionTagEntity emotionTag = getItem(position);
                    checkedChangedListener.onCheckChanged(emotionTag, isChecked);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EmotionTagFilterViewHolder holder, int position) {
        EmotionTagEntity emotionTag = getItem(position);

        //名称文本
        holder.binding.chip.setText(emotionTag.getName());

        //设置选择状态
        holder.isBlocked = true;
        if (checkedEmotionIdSet.contains(emotionTag.getEmotionId())) {
            holder.binding.chip.setChecked(true);
        }
        holder.isBlocked = false;
    }
}
