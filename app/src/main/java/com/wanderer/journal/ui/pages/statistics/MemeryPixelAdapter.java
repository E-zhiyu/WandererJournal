package com.wanderer.journal.ui.pages.statistics;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.entities.composite.DiaryParagraphCountModel;
import com.wanderer.journal.databinding.ViewHolderMemeryPixelBinding;

public class MemeryPixelAdapter extends ListAdapter<DiaryParagraphCountModel, MemeryPixelAdapter.MemeryPixelViewHolder> {
    private final int maxDiaryLength;   //最大日记长度
    private final int avgDiaryLength;   //平均日记长度
    private final static DiffUtil.ItemCallback<DiaryParagraphCountModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryParagraphCountModel oldItem, @NonNull DiaryParagraphCountModel newItem) {
            return oldItem.getDiaryDate().isEqual(newItem.getDiaryDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryParagraphCountModel oldItem, @NonNull DiaryParagraphCountModel newItem) {
            return oldItem.getDiaryLength() == newItem.getDiaryLength();
        }
    };

    /**
     * 记忆像素适配器构造方法
     *
     * @param maxDiaryLength 最大日记字符数量
     */
    public MemeryPixelAdapter(int maxDiaryLength, int avgDiaryLength) {
        super(ITEM_CALLBACK);
        this.maxDiaryLength = maxDiaryLength;
        this.avgDiaryLength = avgDiaryLength;
    }

    public static class MemeryPixelViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMemeryPixelBinding binding;

        public MemeryPixelViewHolder(@NonNull ViewHolderMemeryPixelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public MemeryPixelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderMemeryPixelBinding binding = ViewHolderMemeryPixelBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MemeryPixelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemeryPixelViewHolder holder, int position) {
        DiaryParagraphCountModel model = getItem(position);

        //判断是否为 null 占位符
        if (model == null) {
            return;
        }

        //生成等级分隔符
        int lv1 = avgDiaryLength / 2;
        int lv3 = (avgDiaryLength + maxDiaryLength) / 2;

        //根据内容多少分配颜色
        int diaryLength = model.getDiaryLength();
        if (diaryLength == 0) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_empty);
        } else if (diaryLength < lv1) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_few);
        } else if (diaryLength < avgDiaryLength) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_moderate);
        } else if (diaryLength < lv3) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_many);
        } else {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_numerous);
        }
    }
}
