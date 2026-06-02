package com.wanderer.journal.ui.others.adapters.emotion;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.ViewHolderEmotionInAppbarBinding;

public class EmotionTagInAppBarAdapter
        extends ListAdapter<EmotionTagEntity, EmotionTagInAppBarAdapter.ViewHolderEmotionTagInAppBar> {
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
    private final OnClosedListener closedListener;

    public static class ViewHolderEmotionTagInAppBar extends RecyclerView.ViewHolder {
        ViewHolderEmotionInAppbarBinding binding;

        public ViewHolderEmotionTagInAppBar(@NonNull ViewHolderEmotionInAppbarBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            this.binding.chip.setOnCloseIconClickListener(view -> listener.onCloseIconClicked(getBindingAdapterPosition()));
        }
    }

    public interface ViewHolderListener {
        /**
         * 关闭图标点击监听
         *
         * @param position 点击关闭图标的视图的位置
         */
        void onCloseIconClicked(int position);
    }

    public interface OnClosedListener {
        /**
         * 关闭图标点击监听
         *
         * @param emotionTag 点击关闭图标的 Chip 对应的情绪标签
         */
        void onClosed(EmotionTagEntity emotionTag);
    }

    /**
     * 显示在读日记界面顶部的情绪标签 RecyclerView 的适配器
     *
     * @param closedListener 关闭图标点击监听器
     */
    public EmotionTagInAppBarAdapter(OnClosedListener closedListener) {
        super(ITEM_CALLBACK);
        this.closedListener = closedListener;
    }

    @NonNull
    @Override
    public ViewHolderEmotionTagInAppBar onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderEmotionInAppbarBinding binding = ViewHolderEmotionInAppbarBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolderEmotionTagInAppBar(
                binding,
                position -> {
                    EmotionTagEntity emotionTag = getItem(position);
                    closedListener.onClosed(emotionTag);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderEmotionTagInAppBar holder, int position) {
        EmotionTagEntity emotionTag = getItem(position);

        //名称文本
        holder.binding.chip.setText(emotionTag.getName());
    }
}
