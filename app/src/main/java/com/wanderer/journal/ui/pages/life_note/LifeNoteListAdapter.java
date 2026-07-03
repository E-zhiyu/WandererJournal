package com.wanderer.journal.ui.pages.life_note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.databinding.ViewHolderLifeNoteListBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;

import java.time.format.DateTimeFormatter;

public class LifeNoteListAdapter extends ListAdapter<LifeNoteEntity, LifeNoteListAdapter.LifeNoteListViewHolder> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DiffUtil.ItemCallback<LifeNoteEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull LifeNoteEntity oldItem, @NonNull LifeNoteEntity newItem) {
            return oldItem.getNoteId() == newItem.getNoteId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull LifeNoteEntity oldItem, @NonNull LifeNoteEntity newItem) {
            return oldItem.getInsight().equals(newItem.getInsight()) &&
                    oldItem.getElaboration().equals(newItem.getElaboration()) &&
                    oldItem.getDateTime().isEqual(newItem.getDateTime());
        }
    };
    private final OnClickListener clickListener;
    private final OnLongClickListener longClickListener;

    public interface ViewHolderListener {
        void onClick(int position);

        void onLongClick(int position, View view);
    }

    public interface OnClickListener {
        void onClick(LifeNoteEntity entity);
    }

    public interface OnLongClickListener {
        void onLongClick(LifeNoteEntity entity, View view);
    }

    public static class LifeNoteListViewHolder extends RecyclerView.ViewHolder {
        ViewHolderLifeNoteListBinding binding;

        public LifeNoteListViewHolder(@NonNull ViewHolderLifeNoteListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //绑定触摸监听器
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //绑定点击监听器
            binding.getRoot().setOnClickListener(view -> listener.onClick(getBindingAdapterPosition()));

            //绑定长按监听器
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    public LifeNoteListAdapter(OnClickListener clickListener, OnLongClickListener longClickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        //注册数据变更监听器，用于自动更新圆角
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                // 如果在顶部插入了数据，通知原先的第一项（现在的第 itemCount 项）更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(itemCount);
                }

                // 如果在末尾追加了数据，通知原先的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(positionStart - 1);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                // 如果在顶部删除了数据，通知现在的第一项更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(0);
                }

                // 如果在末尾删除了数据，通知现在的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(getItemCount() - 1);
                }
            }
        });
    }

    @NonNull
    @Override
    public LifeNoteListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderLifeNoteListBinding binding = ViewHolderLifeNoteListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new LifeNoteListViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int position) {
                        LifeNoteEntity entity = getItem(position);
                        clickListener.onClick(entity);
                    }

                    @Override
                    public void onLongClick(int position, View view) {
                        LifeNoteEntity entity = getItem(position);
                        longClickListener.onLongClick(entity, view);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LifeNoteListViewHolder holder, int position) {
        LifeNoteEntity entity = getItem(position);

        //洞见
        holder.binding.insightText.setText(entity.getInsight());

        //阐述
        holder.binding.elaborationText.setText(entity.getElaboration().isEmpty() ? "<无阐述>" : entity.getElaboration());

        //时间
        holder.binding.dateTimeText.setText(entity.getDateTime().format(DATE_TIME_FORMATTER));
    }
}
