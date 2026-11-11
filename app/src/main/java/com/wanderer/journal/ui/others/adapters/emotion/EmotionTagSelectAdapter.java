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
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.ViewHolderClosableChipBinding;

import java.util.HashSet;
import java.util.Set;

public class EmotionTagSelectAdapter extends ListAdapter<EmotionTagEntity, EmotionTagSelectAdapter.ItemViewHolder> {
    private final AdapterOnClickListener<EmotionTagEntity> onClickedListener;     //点击监听
    private final AdapterOnChipCloseListener<EmotionTagEntity> onCloseListener;   //关闭图标点击监听
    private final Set<Long> initCheckedIdSet = new HashSet<>();                             //初始化选中的标签的编号集合
    private final static DiffUtil.ItemCallback<EmotionTagEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull EmotionTagEntity oldItem, @NonNull EmotionTagEntity newItem) {
            return oldItem.getEmotionId() == newItem.getEmotionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmotionTagEntity oldItem, @NonNull EmotionTagEntity newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    public interface ChipListener {
        void onClicked(int position, View view);

        void onClosed(int position);
    }

    /**
     * 情绪标签选择适配器构造方法
     *
     * @param initCheckedSet    用于初始化选择的情绪标签编号集合
     * @param onClickedListener 点击监听
     * @param onCloseListener   关闭按钮点击监听
     */
    public EmotionTagSelectAdapter(
            Set<Long> initCheckedSet,
            AdapterOnClickListener<EmotionTagEntity> onClickedListener,
            AdapterOnChipCloseListener<EmotionTagEntity> onCloseListener
    ) {
        super(ITEM_CALLBACK);
        this.initCheckedIdSet.addAll(initCheckedSet);
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
        EmotionTagEntity emotionTag = getItem(position);

        //设置选中状态
        holder.binding.chip.setChecked(initCheckedIdSet.contains(emotionTag.getEmotionId()));

        //设置名称
        holder.binding.chip.setText(emotionTag.getName());
    }
}
