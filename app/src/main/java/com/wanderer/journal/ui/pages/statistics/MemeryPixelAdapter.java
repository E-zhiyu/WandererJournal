package com.wanderer.journal.ui.pages.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.entities.composite.DiaryLengthModel;
import com.wanderer.journal.databinding.ViewHolderMemeryPixelBinding;

public class MemeryPixelAdapter extends ListAdapter<DiaryLengthModel, MemeryPixelAdapter.MemeryPixelViewHolder> {
    private final int maxDiaryLength;   //最大日记长度
    private final int avgDiaryLength;   //平均日记长度
    private final OnClickedListener clickedListener;
    private final static DiffUtil.ItemCallback<DiaryLengthModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiaryLengthModel oldItem, @NonNull DiaryLengthModel newItem) {
            return oldItem.getDiaryDate().isEqual(newItem.getDiaryDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiaryLengthModel oldItem, @NonNull DiaryLengthModel newItem) {
            return oldItem.getDiaryLength() == newItem.getDiaryLength();
        }
    };

    public interface OnClickedListener {
        void onClicked(DiaryLengthModel model, View view);
    }

    public interface ViewHolderListener {
        void onClicked(int position, View view);
    }

    /**
     * 记忆像素适配器构造方法
     *
     * @param maxDiaryLength 最大日记字符数量
     */
    public MemeryPixelAdapter(int maxDiaryLength, int avgDiaryLength, OnClickedListener clickedListener) {
        super(ITEM_CALLBACK);
        this.maxDiaryLength = maxDiaryLength;
        this.avgDiaryLength = avgDiaryLength;
        this.clickedListener = clickedListener;
    }

    public static class MemeryPixelViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMemeryPixelBinding binding;

        public MemeryPixelViewHolder(@NonNull ViewHolderMemeryPixelBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(view ->
                    listener.onClicked(getBindingAdapterPosition(), binding.getRoot())
            );
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
        return new MemeryPixelViewHolder(
                binding,
                (position, view) -> {
                    DiaryLengthModel model = getItem(position);
                    clickedListener.onClicked(model, view);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MemeryPixelViewHolder holder, int position) {
        DiaryLengthModel model = getItem(position);

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
