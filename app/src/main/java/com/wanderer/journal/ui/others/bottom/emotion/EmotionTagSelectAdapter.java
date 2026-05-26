package com.wanderer.journal.ui.others.bottom.emotion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.entities.composite.EmotionTagUiModel;
import com.wanderer.journal.databinding.ViewHolderEmotionTagSelectBinding;

public class EmotionTagSelectAdapter extends ListAdapter<EmotionTagUiModel, EmotionTagSelectAdapter.EmotionTagSelectViewHolder> {
    private final OnClickedListener onClickedListener;  //点击监听
    private final OnCloseListener onCloseListener;      //关闭图标点击监听
    private final static DiffUtil.ItemCallback<EmotionTagUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull EmotionTagUiModel oldItem, @NonNull EmotionTagUiModel newItem) {
            return oldItem.getEmotionTag().getEmotionId() == newItem.getEmotionTag().getEmotionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmotionTagUiModel oldItem, @NonNull EmotionTagUiModel newItem) {
            return oldItem.getEmotionTag().getName().equals(newItem.getEmotionTag().getName());
        }
    };

    public interface OnClickedListener {
        /**
         * 标签点击回调
         *
         * @param model  情绪标签显示模型
         * @param anchor 浮动滑块绑定的视图
         */
        void onClicked(EmotionTagUiModel model, View anchor);
    }

    public interface OnCloseListener {
        /**
         * Chip 的 closeIcon 点击回调
         *
         * @param model 数据原型
         */
        void onClosed(EmotionTagUiModel model);
    }

    public interface ViewHolderListener {
        void onClicked(int position, View view);

        void onClosed(int position);
    }

    /**
     * 情绪标签选择适配器构造方法
     *
     * @param onClickedListener 点击监听
     * @param onCloseListener   关闭按钮点击监听
     */
    public EmotionTagSelectAdapter(OnClickedListener onClickedListener, OnCloseListener onCloseListener) {
        super(ITEM_CALLBACK);
        this.onClickedListener = onClickedListener;
        this.onCloseListener = onCloseListener;
    }

    public static class EmotionTagSelectViewHolder extends RecyclerView.ViewHolder {
        ViewHolderEmotionTagSelectBinding binding;

        public EmotionTagSelectViewHolder(@NonNull ViewHolderEmotionTagSelectBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置点击监听
            binding.chip.setOnClickListener(view -> {
                listener.onClicked(getBindingAdapterPosition(), binding.chip);
                binding.chip.setChecked(true);
            });

            //设置关闭图标监听
            binding.chip.setOnCloseIconClickListener(view -> {
                listener.onClosed(getBindingAdapterPosition());
                binding.chip.setChecked(false);
            });
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
                new ViewHolderListener() {
                    @Override
                    public void onClicked(int position, View view) {
                        EmotionTagUiModel model = getItem(position);
                        onClickedListener.onClicked(model, view);
                    }

                    @Override
                    public void onClosed(int position) {
                        EmotionTagUiModel model = getItem(position);
                        onCloseListener.onClosed(model);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EmotionTagSelectViewHolder holder, int position) {
        EmotionTagUiModel item = getItem(position);
        if (item == null) {
            holder.binding.chip.setChecked(false);
            holder.binding.chip.setCheckable(false);            //不可选择
            holder.binding.chip.setCloseIconVisible(false);     //隐藏关闭按钮
            holder.binding.chip.setCheckedIconVisible(true);    //显示左侧选择按钮
            holder.binding.chip.setCheckedIconResource(R.drawable.outline_add_24);  //左侧改为添加图标
            holder.binding.chip.setText("添加情绪标签");
        } else {
            //设置选中状态
            holder.binding.chip.setChecked(item.isChecked());

            //设置名称
            holder.binding.chip.setText(item.getEmotionTag().getName());
        }
    }
}
