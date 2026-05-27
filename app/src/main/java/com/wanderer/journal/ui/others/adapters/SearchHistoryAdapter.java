package com.wanderer.journal.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.databinding.ViewHolderSearchHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends ListAdapter<String, SearchHistoryAdapter.SearchHistoryViewHolder> {
    private final static DiffUtil.ItemCallback<String> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return true;
        }
    };
    private final String key;                   //保存搜索历史的关键字
    private final OnClickerListener listener;   //视图的点击监听

    /**
     * 搜索历史适配器构造方法
     *
     * @param key      保存搜索关键词的键，详见{@link SearchHistoryPreference}的静态字符串
     * @param listener 搜索历史Chip点击监听器
     */
    public SearchHistoryAdapter(String key, OnClickerListener listener) {
        super(ITEM_CALLBACK);
        this.key = key;
        this.listener = listener;
    }

    public static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSearchHistoryBinding binding;

        public SearchHistoryViewHolder(@NonNull ViewHolderSearchHistoryBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.titleChip.setOnClickListener(v -> listener.onClicked(getBindingAdapterPosition()));
        }
    }

    public interface OnClickerListener {
        /**
         * 搜索历史记录点击监听
         *
         * @param keyword 点击的关键词
         */
        void onClicked(String keyword);
    }

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         */
        void onClicked(int position);
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderSearchHistoryBinding binding = ViewHolderSearchHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SearchHistoryViewHolder(binding, position -> {
            String historyKeyWord = getItem(position);
            listener.onClicked(historyKeyWord);

            //将点击的关键词放到第一位
            List<String> historyList = SearchHistoryPreference.addKeyword(
                    historyKeyWord,
                    key,
                    parent.getContext()
            );
            submitList(new ArrayList<>(historyList));
        });
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        String historyKeyWord = getItem(position);
        holder.binding.titleChip.setText(historyKeyWord);
    }
}
