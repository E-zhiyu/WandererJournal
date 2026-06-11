package com.wanderer.journal.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.databinding.ViewHolderChipTextBinding;

public class RoleSelectAdapter extends ListAdapter<RoleEntity, RoleSelectAdapter.RoleSelectViewHolder> {
    private final static DiffUtil.ItemCallback<RoleEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RoleEntity oldItem, @NonNull RoleEntity newItem) {
            return oldItem.getRoleId() == newItem.getRoleId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoleEntity oldItem, @NonNull RoleEntity newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };
    private final OnClickListener clickListener;    //点击监听

    public interface ViewHolderListener {
        void onClick(int position);
    }

    public interface OnClickListener {
        void onClick(RoleEntity role);
    }

    public static class RoleSelectViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public RoleSelectViewHolder(@NonNull ViewHolderChipTextBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.chip.setCheckable(false);

            binding.chip.setOnClickListener(view -> listener.onClick(getBindingAdapterPosition()));
        }
    }

    public RoleSelectAdapter(OnClickListener clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
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
                position -> {
                    RoleEntity role = getItem(position);
                    clickListener.onClick(role);
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RoleSelectViewHolder holder, int position) {
        RoleEntity role = getItem(position);
        holder.binding.chip.setText(role.getName());
    }
}
