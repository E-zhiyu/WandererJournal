package com.wanderer.journal.ui.pages.main.diary;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.wanderer.journal.data.save.db.entities.DiaryEntity;

public class DiaryDiffCallback extends DiffUtil.ItemCallback<DiaryEntity> {
    @Override
    public boolean areItemsTheSame(@NonNull DiaryEntity oldItem, @NonNull DiaryEntity newItem) {
        return oldItem.getDiaryId() == newItem.getDiaryId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull DiaryEntity oldItem, @NonNull DiaryEntity newItem) {
        //TODO:完成内容是否改变的判断条件
        return false;
    }
}
