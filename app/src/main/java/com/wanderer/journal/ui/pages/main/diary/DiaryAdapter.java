package com.wanderer.journal.ui.pages.main.diary;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.databinding.ViewHolderDiaryBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DiaryAdapter extends ListAdapter<DiaryEntity, DiaryAdapter.ViewHolderDiary> {
    private final OnClickListener listener;
    private static final DiffUtil.ItemCallback<DiaryEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryEntity oldItem, @NonNull DiaryEntity newItem) {
            return oldItem.getDiaryId() == newItem.getDiaryId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryEntity oldItem, @NonNull DiaryEntity newItem) {
            //TODO:完成内容是否改变的判断条件
            return false;
        }
    };

    /**
     * 日记列表适配器构造方法
     *
     * @param listener ViewHolder点击监听器
     */
    public DiaryAdapter(OnClickListener listener) {
        super(ITEM_CALLBACK);
        this.listener = listener;
    }

    public interface OnClickListener {
        /**
         * 点击监听
         *
         * @param diary 被点击的视图对应的日记实例
         */
        void onClicked(DiaryEntity diary);
    }

    public interface ViewHolderListener {
        /**
         * ViewHolder点击监听
         *
         * @param position 点击的ViewHolder的下标
         */
        void onClicked(int position);
    }

    public static class ViewHolderDiary extends RecyclerView.ViewHolder {
        ViewHolderDiaryBinding binding;

        public ViewHolderDiary(@NonNull ViewHolderDiaryBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener
                    .onClicked(getBindingAdapterPosition())
            );
        }
    }

    @NonNull
    @Override
    public ViewHolderDiary onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderDiaryBinding binding = ViewHolderDiaryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolderDiary(
                binding,
                position -> {
                    DiaryEntity diary = getItem(position);
                    listener.onClicked(diary);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDiary holder, int position) {
        DiaryEntity diary = getItem(position);

        LocalDate date = diary.getDiaryDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        holder.binding.dateText.setText(date.format(formatter));

        //TODO:补上段落预览以及段落数量显示

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.binding.getRoot(), getItemCount(), position);
    }
}
