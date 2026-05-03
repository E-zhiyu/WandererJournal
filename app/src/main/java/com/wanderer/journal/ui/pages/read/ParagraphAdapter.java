package com.wanderer.journal.ui.pages.read;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ViewHolderParagraphBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParagraphAdapter extends PagingDataAdapter<ParagraphEntity, ParagraphAdapter.ParagraphViewHolder> {
    private final static DiffUtil.ItemCallback<ParagraphEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParagraphEntity oldItem, @NonNull ParagraphEntity newItem) {
            return oldItem.getParagraphId() == newItem.getParagraphId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParagraphEntity oldItem, @NonNull ParagraphEntity newItem) {
            return oldItem.getContent().equals(newItem.getContent());
        }
    };

    public interface ViewHolderListener {
        void onClicked();
    }

    public static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        ViewHolderParagraphBinding binding;

        public ParagraphViewHolder(@NonNull ViewHolderParagraphBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置监听器
            binding.getRoot().setOnClickListener(view -> listener.onClicked());
        }
    }

    public ParagraphAdapter() {
        super(ITEM_CALLBACK);
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
                () -> {
                    //TODO:处理回调
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ParagraphViewHolder holder, int position) {
        ParagraphEntity paragraph = getItem(position);
        if (paragraph == null) {
            return;
        }

        String content = paragraph.getContent();
        holder.binding.contentText.setText(content);

        LocalDateTime dateTime = paragraph.getCreateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        holder.binding.dateTimeText.setText(dateTime.format(formatter));
    }
}
