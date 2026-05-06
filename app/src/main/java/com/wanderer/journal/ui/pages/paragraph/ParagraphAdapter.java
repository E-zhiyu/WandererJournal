package com.wanderer.journal.ui.pages.paragraph;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ViewHolderParagraphBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParagraphAdapter extends PagingDataAdapter<ParagraphEntity, ParagraphAdapter.ParagraphViewHolder> {
    private final OnClickListener listener;
    private final static DiffUtil.ItemCallback<ParagraphEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParagraphEntity oldItem, @NonNull ParagraphEntity newItem) {
            return oldItem.getParagraphId() == newItem.getParagraphId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParagraphEntity oldItem, @NonNull ParagraphEntity newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                    oldItem.getCreateTime().isEqual(newItem.getCreateTime());
        }
    };

    public interface ViewHolderListener {
        void onClicked(ParagraphEntity paragraph, View view);
    }

    public interface OnClickListener {
        /**
         * 段落被点击的回调
         *
         * @param paragraph 被点击的段落
         * @param view      用于显示PopupMenu的视图
         */
        void onClicked(ParagraphEntity paragraph, View view);
    }

    public static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        ViewHolderParagraphBinding binding;
        ParagraphEntity paragraph = null;   //段落实例

        public ParagraphViewHolder(@NonNull ViewHolderParagraphBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置监听器
            binding.getRoot().setOnClickListener(view -> listener.onClicked(paragraph, binding.getRoot()));
        }

        /**
         * 将ViewHolder与数据实例绑定
         *
         * @param paragraph 段落实例
         */
        public void bindItem(ParagraphEntity paragraph) {
            this.paragraph = paragraph;
        }
    }

    /**
     * 段落适配器构造方法
     *
     * @param listener 段落点击监听器
     */
    public ParagraphAdapter(OnClickListener listener) {
        super(ITEM_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParagraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderParagraphBinding binding = ViewHolderParagraphBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ParagraphViewHolder(
                binding,
                listener::onClicked
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ParagraphViewHolder holder, int position) {
        ParagraphEntity paragraph = getItem(position);
        if (paragraph == null) {
            return;
        }

        holder.bindItem(paragraph);

        String content = paragraph.getContent();
        holder.binding.contentText.setText(content);

        LocalDateTime dateTime = paragraph.getCreateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        holder.binding.dateTimeText.setText(dateTime.format(formatter));
    }
}
