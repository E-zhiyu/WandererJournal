package com.wanderer.journal.ui.pages.main.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.composite.DiaryWithSummary;
import com.wanderer.journal.databinding.ViewHolderDiaryBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DiaryAdapter extends ListAdapter<DiaryWithSummary, DiaryAdapter.ViewHolderDiary> {
    private final OnClickedListener clickListener;              //点击监听
    private final OnLongClickedListener longClickedListener;    //长按监听
    private static final DiffUtil.ItemCallback<DiaryWithSummary> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {

        @Override
        public boolean areItemsTheSame(@NonNull DiaryWithSummary oldItem, @NonNull DiaryWithSummary newItem) {
            return oldItem.getDiary().getDiaryId() == newItem.getDiary().getDiaryId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryWithSummary oldItem, @NonNull DiaryWithSummary newItem) {
            return oldItem.equals(newItem);
        }
    };

    /**
     * 日记列表适配器构造方法
     *
     * @param clickListener ViewHolder点击监听器
     */
    public DiaryAdapter(OnClickedListener clickListener, OnLongClickedListener longClickedListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickedListener = longClickedListener;
    }

    public interface OnClickedListener {
        /**
         * 点击监听
         *
         * @param diary 被点击的视图对应的日记实例
         */
        void onClicked(DiaryEntity diary);
    }

    public interface OnLongClickedListener {
        /**
         * 长按监听
         *
         * @param diary 长按的视图对应的日记实例
         * @param view  用于显示PopupMenu的视图
         */
        void onLongClicked(DiaryEntity diary, View view);
    }

    public interface ViewHolderListener {
        /**
         * ViewHolder点击监听
         *
         * @param position 点击的ViewHolder的下标
         */
        void onClicked(int position);

        /**
         * 长按监听
         *
         * @param position 长按的ViewHolder下标
         * @param view     用于显示PopupMenu的视图
         */
        void onLongClicked(int position, View view);
    }

    public static class ViewHolderDiary extends RecyclerView.ViewHolder {
        ViewHolderDiaryBinding binding;

        public ViewHolderDiary(@NonNull ViewHolderDiaryBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener
                    .onClicked(getBindingAdapterPosition())
            );

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                        listener.onLongClicked(getBindingAdapterPosition(), binding.getRoot());
                        return true;
                    }
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
                new ViewHolderListener() {
                    @Override
                    public void onClicked(int position) {
                        DiaryEntity diary = getItem(position).getDiary();
                        clickListener.onClicked(diary);
                    }

                    @Override
                    public void onLongClicked(int position, View view) {
                        DiaryEntity diary = getItem(position).getDiary();
                        longClickedListener.onLongClicked(diary, view);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDiary holder, int position) {
        DiaryWithSummary diaryWithSummary = getItem(position);

        //日期
        LocalDate date = diaryWithSummary.getDiary().getDiaryDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        holder.binding.dateText.setText(date.format(formatter));

        //片段摘要
        String paragraphFragment = diaryWithSummary.getParagraphFragment();
        holder.binding.contentPreviewText.setText(paragraphFragment);

        //片段数量
        int paragraphCount = diaryWithSummary.getParagraphCount();
        holder.binding.paragraphCountText.setText(String.valueOf(paragraphCount));

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.binding.getRoot(), getItemCount(), position);
    }
}
