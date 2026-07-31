package com.wanderer.journal.ui.others.adapters.role;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.AdapterOnLongClickListener;
import com.wanderer.journal.auxiliary.interfaces.adapter.ViewHolderListener;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.databinding.ViewHolderChipTextBinding;

public class CommonRoleSelectAdapter extends ListAdapter<RoleEntity, CommonRoleSelectAdapter.RoleSelectViewHolder> {
    private final static DiffUtil.ItemCallback<RoleEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RoleEntity oldItem, @NonNull RoleEntity newItem) {
            return oldItem.getRoleId() == newItem.getRoleId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoleEntity oldItem, @NonNull RoleEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDisplayName().equals(newItem.getDisplayName());
        }
    };
    private final AdapterOnClickListener<RoleEntity> clickListener;
    private final AdapterOnLongClickListener<RoleEntity> longClickListener;

    public static class RoleSelectViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public RoleSelectViewHolder(@NonNull ViewHolderChipTextBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.chip.setCheckable(false);

            //设置点击监听
            binding.chip.setOnClickListener(view -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));

            //设置长按监听
            binding.chip.setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    public CommonRoleSelectAdapter(
            AdapterOnClickListener<RoleEntity> clickListener,
            AdapterOnLongClickListener<RoleEntity> longClickListener
    ) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public RoleSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderChipTextBinding binding = ViewHolderChipTextBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RoleSelectViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int position, View anchor) {
                        RoleEntity role = getItem(position);
                        clickListener.onClick(role, anchor);
                    }

                    @Override
                    public void onLongClick(int position, View anchor) {
                        RoleEntity role = getItem(position);
                        longClickListener.onLongClick(role, anchor);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RoleSelectViewHolder holder, int position) {
        RoleEntity role = getItem(position);
        holder.binding.chip.setText(role.generateDisplayName());
    }
}
