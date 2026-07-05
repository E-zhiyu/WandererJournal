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
    private final AdapterOnClickListener<LifeNoteEntity> clickListener;
    private final AdapterOnLongClickListener<LifeNoteEntity> longClickListener;

    public static class LifeNoteListViewHolder extends RecyclerView.ViewHolder {
        ViewHolderLifeNoteListBinding binding;

        public LifeNoteListViewHolder(@NonNull ViewHolderLifeNoteListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //绑定触摸监听器
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //绑定点击监听器
            binding.getRoot().setOnClickListener(view -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));

            //绑定长按监听器
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    public LifeNoteListAdapter(
            AdapterOnClickListener<LifeNoteEntity> clickListener,
            AdapterOnLongClickListener<LifeNoteEntity> longClickListener
    ) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        //注册数据变更监听器，用于自动更新圆角
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemChanged(positionStart - 1);           //更新前面的
                notifyItemChanged(positionStart + itemCount);   //更新后面的
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemChanged(positionStart - 1);   //更新前面的
                notifyItemChanged(positionStart);               //更新后面的
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemChanged(fromPosition - 1);    //更新前面的
                notifyItemChanged(fromPosition);                //更新后面的

                notifyItemChanged(toPosition - 1);      //更新前面的
                notifyItemChanged(toPosition + 1);      //更新后面的
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
                    public void onClick(int position, View anchor) {
                        LifeNoteEntity entity = getItem(position);
                        clickListener.onClick(entity, anchor);
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

        //设置圆角
        AppearanceHelper.setRecyclerItemRadius(holder.binding.getRoot(), getItemCount(), holder.getBindingAdapterPosition());
    }
}
