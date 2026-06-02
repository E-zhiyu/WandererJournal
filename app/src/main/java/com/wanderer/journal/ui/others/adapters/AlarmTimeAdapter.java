package com.wanderer.journal.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.databinding.ViewHolderClosableChipBinding;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AlarmTimeAdapter extends ListAdapter<LocalTime, AlarmTimeAdapter.AlarmTimeViewHolder> {
    private final static DiffUtil.ItemCallback<LocalTime> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull LocalTime oldItem, @NonNull LocalTime newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull LocalTime oldItem, @NonNull LocalTime newItem) {
            return true;
        }
    };
    private final OnClosedListener closedListener;

    public static class AlarmTimeViewHolder extends RecyclerView.ViewHolder {
        ViewHolderClosableChipBinding binding;

        public AlarmTimeViewHolder(@NonNull ViewHolderClosableChipBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置不可选择
            binding.chip.setCheckable(false);

            binding.chip.setOnCloseIconClickListener(view -> listener.onClose(getBindingAdapterPosition()));
        }
    }

    public interface OnClosedListener {
        /**
         * 关闭图标点击回调
         *
         * @param time 点击关闭图标的 Chip 对应的时间字符串
         */
        void onClosed(LocalTime time);
    }

    public interface ViewHolderListener {
        void onClose(int position);
    }

    /**
     * 提醒时间适配器构造方法
     *
     * @param closedListener 关闭图标点击监听器
     */
    public AlarmTimeAdapter(OnClosedListener closedListener) {
        super(ITEM_CALLBACK);
        this.closedListener = closedListener;
    }

    @NonNull
    @Override
    public AlarmTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderClosableChipBinding binding = ViewHolderClosableChipBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AlarmTimeViewHolder(
                binding,
                position -> {
                    LocalTime time = getItem(position);
                    closedListener.onClosed(time);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmTimeViewHolder holder, int position) {
        LocalTime time = getItem(position);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        holder.binding.chip.setText(time.format(formatter));
    }
}
