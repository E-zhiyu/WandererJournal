package com.wanderer.journal.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.databinding.ViewHolderChipTextBinding;

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
    private final String key;                                   //保存搜索历史的关键字
    private final OnClickedListener clickedListener;            //点击监听

    /**
     * 搜索历史适配器构造方法
     *
     * @param key             保存搜索关键词的键，详见{@link SearchHistoryPreference}的静态字符串
     * @param clickedListener 搜索历史 Chip 点击监听器
     */
    public SearchHistoryAdapter(String key, OnClickedListener clickedListener) {
        super(ITEM_CALLBACK);
        this.key = key;
        this.clickedListener = clickedListener;
    }

    public static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public SearchHistoryViewHolder(@NonNull ViewHolderChipTextBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置点击监听
            binding.chip.setOnClickListener(v -> listener.onClicked(getBindingAdapterPosition()));

            //设置长按监听
            binding.chip.setOnLongClickListener(view -> {
                listener.onLongClicked(getBindingAdapterPosition());
                return true;
            });
        }
    }

    public interface OnClickedListener {
        /**
         * 搜索历史记录点击监听
         *
         * @param keyword 点击的关键词
         */
        void onClicked(String keyword);
    }

    public interface ViewHolderListener {
        void onClicked(int position);

        void onLongClicked(int position);
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderChipTextBinding binding = ViewHolderChipTextBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SearchHistoryViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClicked(int position) {
                        String historyKeyWord = getItem(position);
                        clickedListener.onClicked(historyKeyWord);

                        //将点击的关键词放到第一位
                        List<String> historyList = SearchHistoryPreference.addKeyword(
                                historyKeyWord,
                                key,
                                parent.getContext()
                        );
                        submitList(new ArrayList<>(historyList));
                    }

                    @Override
                    public void onLongClicked(int position) {
                        String historyKeyWord = getItem(position);

                        //将点击的关键词放到第一位
                        List<String> historyList = SearchHistoryPreference.removeKeyword(
                                historyKeyWord,
                                key,
                                parent.getContext()
                        );
                        submitList(new ArrayList<>(historyList));
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        String historyKeyWord = getItem(position);
        holder.binding.chip.setText(historyKeyWord);
    }
}
