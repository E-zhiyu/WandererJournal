package com.wanderer.journal.ui.pages.life_note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnLongClickListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.ViewHolderListener;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;
import com.wanderer.journal.databinding.ViewHolderLifeNoteHistoryListBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;

import java.time.format.DateTimeFormatter;

public class LifeNoteHistoryListAdapter extends ListAdapter<LifeNoteHistoryEntity, LifeNoteHistoryListAdapter.HistoryViewHolder> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final static DiffUtil.ItemCallback<LifeNoteHistoryEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull LifeNoteHistoryEntity oldItem, @NonNull LifeNoteHistoryEntity newItem) {
            return oldItem.getHistoryId() == newItem.getHistoryId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull LifeNoteHistoryEntity oldItem, @NonNull LifeNoteHistoryEntity newItem) {
            return oldItem.getInsight().equals(newItem.getInsight()) &&
                    oldItem.getElaboration().equals(newItem.getElaboration()) &&
                    oldItem.getUpdateDateTime().isEqual(newItem.getUpdateDateTime());
        }
    };
    private final AdapterOnClickListener<LifeNoteHistoryEntity> clickListener;
    private final AdapterOnLongClickListener<LifeNoteHistoryEntity> longClickListener;

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ViewHolderLifeNoteHistoryListBinding binding;

        public HistoryViewHolder(@NonNull ViewHolderLifeNoteHistoryListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    public LifeNoteHistoryListAdapter(
            AdapterOnClickListener<LifeNoteHistoryEntity> clickListener,
            AdapterOnLongClickListener<LifeNoteHistoryEntity> longClickListener
    ) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderLifeNoteHistoryListBinding binding = ViewHolderLifeNoteHistoryListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HistoryViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int pos, View anchor) {
                        LifeNoteHistoryEntity entity = getItem(pos);
                        clickListener.onClick(entity, anchor);
                    }

                    @Override
                    public void onLongClick(int pos, View anchor) {
                        LifeNoteHistoryEntity entity = getItem(pos);
                        longClickListener.onLongClick(entity, anchor);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        LifeNoteHistoryEntity entity = getItem(position);

        //洞见
        holder.binding.insightText.setText(entity.getInsight());

        //阐述
        holder.binding.elaborationText.setText(entity.getElaboration().isEmpty() ? "<无阐述>" : entity.getElaboration());

        //时间
        holder.binding.dateTimeText.setText(entity.getUpdateDateTime().format(DATE_TIME_FORMATTER));
    }
}
