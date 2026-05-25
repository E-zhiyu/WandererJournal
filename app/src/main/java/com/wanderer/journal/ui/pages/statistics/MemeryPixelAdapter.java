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
    private final int maxCharacterCount;    //最大日记字符数量
    private final static DiffUtil.ItemCallback<DiaryParagraphCountModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryParagraphCountModel oldItem, @NonNull DiaryParagraphCountModel newItem) {
            return oldItem.getDiaryDate().isEqual(newItem.getDiaryDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryParagraphCountModel oldItem, @NonNull DiaryParagraphCountModel newItem) {
            return oldItem.getParagraphWordCount() == newItem.getParagraphWordCount();
        }
    };

    /**
     * 记忆像素适配器构造方法
     *
     * @param maxCharacterCount 最大日记字符数量
     */
    public MemeryPixelAdapter(int maxCharacterCount) {
        super(ITEM_CALLBACK);
        this.maxCharacterCount = maxCharacterCount;
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

        //根据内容多少分配颜色
        int paragraphCount = model.getParagraphWordCount();
        if (paragraphCount == 0) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_empty);
        } else if (paragraphCount < maxCharacterCount * .25) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_few);
        } else if (paragraphCount < maxCharacterCount * .5) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_moderate);
        } else if (paragraphCount < maxCharacterCount * .75) {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_many);
        } else {
            holder.binding.viewCube.setBackgroundResource(R.drawable.bg_pixel_numerous);
        }
    }
}
