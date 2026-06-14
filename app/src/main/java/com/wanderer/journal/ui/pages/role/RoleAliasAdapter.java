package com.wanderer.journal.ui.pages.role;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.databinding.ViewHolderChipTextBinding;

import java.util.ArrayList;
import java.util.List;

public class RoleAliasAdapter extends ListAdapter<String, RoleAliasAdapter.AliaViewHolder> {
    private static final DiffUtil.ItemCallback<String> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return true;
        }
    };

    public interface ViewHolderListener {
        void onCloseIconClicked(int position);
    }

    public static class AliaViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public AliaViewHolder(@NonNull ViewHolderChipTextBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //启用关闭图标
            binding.chip.setCloseIconVisible(true);

            //设置关闭图标点击监听
            binding.chip.setOnCloseIconClickListener(view -> listener.onCloseIconClicked(getBindingAdapterPosition()));
        }
    }

    public RoleAliasAdapter() {
        super(ITEM_CALLBACK);
    }

    @NonNull
    @Override
    public AliaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderChipTextBinding binding = ViewHolderChipTextBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AliaViewHolder(
                binding,
                position -> {
                    List<String> newList = new ArrayList<>(getCurrentList());
                    newList.remove(position);
                    submitList(newList);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AliaViewHolder holder, int position) {
        String alia = getItem(position);
        holder.binding.chip.setText(alia);
    }
}
