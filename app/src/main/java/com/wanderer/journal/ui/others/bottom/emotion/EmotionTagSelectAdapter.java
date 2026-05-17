package com.wanderer.journal.ui.others.bottom.emotion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.composite.EmotionTagUiModel;
import com.wanderer.journal.databinding.ViewHolderEmotionTagSelectBinding;

public class EmotionTagSelectAdapter extends ListAdapter<EmotionTagUiModel, EmotionTagSelectAdapter.EmotionTagSelectViewHolder> {
    private final OnCheckedChangedListener checkedChangedListener;
    private final static DiffUtil.ItemCallback<EmotionTagUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull EmotionTagUiModel oldItem, @NonNull EmotionTagUiModel newItem) {
            return oldItem.getEmotionTag().getEmotionId() == newItem.getEmotionTag().getEmotionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmotionTagUiModel oldItem, @NonNull EmotionTagUiModel newItem) {
            return oldItem.getEmotionTag().getName().equals(newItem.getEmotionTag().getName()) &&
                    oldItem.isChecked() == newItem.isChecked();
        }
    };

    public interface OnCheckedChangedListener {
        /**
         * 标签选中状态变更回调
         *
         * @param model   情绪标签显示模型
         * @param checked 情绪标签是否被选中
         * @param anchor  浮动滑块绑定的视图
         */
        void onCheckChanged(EmotionTagUiModel model, boolean checked, View anchor);
    }

    public interface ViewHolderListener {
        void onCheckedChanged(int position, boolean isChecked, View view);
    }

    public EmotionTagSelectAdapter(OnCheckedChangedListener checkedChangedListener) {
        super(ITEM_CALLBACK);
        this.checkedChangedListener = checkedChangedListener;
    }

    public static class EmotionTagSelectViewHolder extends RecyclerView.ViewHolder {
        boolean isBlocked = false;                  //监听器是否被屏蔽
        ViewHolderEmotionTagSelectBinding binding;

        public EmotionTagSelectViewHolder(@NonNull ViewHolderEmotionTagSelectBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnCheckedChangeListener((compoundButton, b) ->
                    {
                        if (isBlocked) return;

                        listener.onCheckedChanged(getBindingAdapterPosition(), b, binding.getRoot());
                    }
            );
        }
    }

    @NonNull
    @Override
    public EmotionTagSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderEmotionTagSelectBinding binding = ViewHolderEmotionTagSelectBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new EmotionTagSelectViewHolder(
                binding,
                (position, isChecked, view) -> {
                    EmotionTagUiModel model = getItem(position);
                    checkedChangedListener.onCheckChanged(model, isChecked, binding.getRoot());
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EmotionTagSelectViewHolder holder, int position) {
        EmotionTagUiModel item = getItem(position);
        holder.isBlocked = true;    //临时屏蔽监听器

        //设置选中状态
        holder.binding.getRoot().setChecked(item.isChecked());

        //设置名称
        holder.binding.getRoot().setText(item.getEmotionTag().getName());

        holder.isBlocked = false;   //接触监听器的屏蔽
    }
}
