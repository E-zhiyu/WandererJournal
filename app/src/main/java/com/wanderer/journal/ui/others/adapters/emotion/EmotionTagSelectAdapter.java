package com.wanderer.journal.ui.others.adapters.emotion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnChipCloseListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.wanderer.journal.data.save.db.entities.composite.ui.EmotionTagUiModel;
import com.wanderer.journal.databinding.ViewHolderClosableChipBinding;

public class EmotionTagSelectAdapter extends ListAdapter<EmotionTagUiModel, EmotionTagSelectAdapter.ItemViewHolder> {
    private final AdapterOnClickListener<EmotionTagUiModel> onClickedListener;      //点击监听
    private final AdapterOnChipCloseListener<EmotionTagUiModel> onCloseListener;    //关闭图标点击监听
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

    public interface ChipListener {
        void onClicked(int position, View view);

        void onClosed(int position);
    }

    /**
     * 情绪标签选择适配器构造方法
     *
     * @param onClickedListener 点击监听
     * @param onCloseListener   关闭按钮点击监听
     */
    public EmotionTagSelectAdapter(AdapterOnClickListener<EmotionTagUiModel> onClickedListener, AdapterOnChipCloseListener<EmotionTagUiModel> onCloseListener) {
        super(ITEM_CALLBACK);
        this.onClickedListener = onClickedListener;
        this.onCloseListener = onCloseListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderClosableChipBinding binding;

        public ItemViewHolder(@NonNull ViewHolderClosableChipBinding binding, ChipListener listener) {
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
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderClosableChipBinding binding = ViewHolderClosableChipBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ItemViewHolder(
                binding,
                new ChipListener() {
                    @Override
                    public void onClicked(int position, View view) {
                        onClickedListener.onClick(getItem(position), view);
                    }

                    @Override
                    public void onClosed(int position) {
                        onCloseListener.onClose(getItem(position));
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        EmotionTagUiModel item = getItem(position);
        //设置选中状态
        holder.binding.chip.setChecked(item.isChecked());

        //设置名称
        holder.binding.chip.setText(item.getEmotionTag().getName());
    }
}
