package com.wanderer.journal.ui.pages.emotion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.ViewHolderEmotionTagBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

public class EmotionTagAdapter extends ListAdapter<EmotionTagEntity, EmotionTagAdapter.EmotionTagViewHolder> {
    private final OnClickedListener clickedListener;            //点击监听器
    private final OnLongClickedListener longClickedListener;    //长按监听器
    private static final DiffUtil.ItemCallback<EmotionTagEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull EmotionTagEntity oldItem, @NonNull EmotionTagEntity newItem) {
            return oldItem.getEmotionId() == newItem.getEmotionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmotionTagEntity oldItem, @NonNull EmotionTagEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription());
        }
    };

    public static class EmotionTagViewHolder extends RecyclerView.ViewHolder {
        ViewHolderEmotionTagBinding binding;

        public EmotionTagViewHolder(@NonNull ViewHolderEmotionTagBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClicked(getBindingAdapterPosition()));

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClicked(getBindingAdapterPosition(), view);
                return true;
            });
        }
    }

    public interface OnClickedListener {
        /**
         * 点击监听
         *
         * @param emotionTag 电机的情绪标签
         */
        void onClicked(EmotionTagEntity emotionTag);
    }

    public interface OnLongClickedListener {
        /**
         * 长按监听
         *
         * @param emotionTag 长按的情绪标签
         * @param view       PopupMenu绑定的视图
         */
        void onLongClicked(EmotionTagEntity emotionTag, View view);
    }

    public interface ViewHolderListener {
        void onClicked(int position);

        void onLongClicked(int position, View view);
    }

    /**
     * 情绪标签列表适配器构造方法
     *
     * @param clickedListener 点击监听器
     */
    public EmotionTagAdapter(OnClickedListener clickedListener, OnLongClickedListener longClickedListener) {
        super(ITEM_CALLBACK);
        this.clickedListener = clickedListener;
        this.longClickedListener = longClickedListener;

        //注册数据变更监听器，用于自动更新圆角
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                // 如果在顶部插入了数据，通知原先的第一项（现在的第 itemCount 项）更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(itemCount);
                }

                // 如果在末尾追加了数据，通知原先的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(positionStart - 1);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                // 如果在顶部删除了数据，通知现在的第一项更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(0);
                }

                // 如果在末尾删除了数据，通知现在的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(getItemCount() - 1);
                }
            }
        });
    }

    @NonNull
    @Override
    public EmotionTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderEmotionTagBinding binding = ViewHolderEmotionTagBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new EmotionTagViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClicked(int position) {
                        EmotionTagEntity emotionTag = getItem(position);
                        clickedListener.onClicked(emotionTag);
                    }

                    @Override
                    public void onLongClicked(int position, View view) {
                        EmotionTagEntity emotionTag = getItem(position);
                        longClickedListener.onLongClicked(emotionTag, view);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EmotionTagViewHolder holder, int position) {
        EmotionTagEntity emotionTag = getItem(position);

        //标签名称
        holder.binding.nameText.setText(emotionTag.getName());

        //标签描述
        holder.binding.descriptionText.setText(emotionTag.getDescription());

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.binding.getRoot(), getItemCount(), position);
    }
}
