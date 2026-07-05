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

        //设置圆角
        AppearanceHelper.setRecyclerItemRadius(holder.binding.getRoot(), getItemCount(), holder.getBindingAdapterPosition());
    }
}
