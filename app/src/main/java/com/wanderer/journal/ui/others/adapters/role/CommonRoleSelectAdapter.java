package com.wanderer.journal.ui.others.adapters.role;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

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
    private final OnClickListener clickListener;    //点击监听
    private final OnLongClickListener longClickListener;    //长按监听

    public interface ViewHolderListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public interface OnClickListener {
        void onClick(RoleEntity role);
    }

    public interface OnLongClickListener {
        void onLongClick(RoleEntity role);
    }

    public static class RoleSelectViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public RoleSelectViewHolder(@NonNull ViewHolderChipTextBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.chip.setCheckable(false);

            //设置点击监听
            binding.chip.setOnClickListener(view -> listener.onClick(getBindingAdapterPosition()));

            //设置长按监听
            binding.chip.setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition());
                return true;
            });
        }
    }

    public CommonRoleSelectAdapter(OnClickListener clickListener, OnLongClickListener longClickListener) {
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
                    public void onClick(int position) {
                        RoleEntity role = getItem(position);
                        clickListener.onClick(role);
                    }

                    @Override
                    public void onLongClick(int position) {
                        RoleEntity role = getItem(position);
                        longClickListener.onLongClick(role);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RoleSelectViewHolder holder, int position) {
        RoleEntity role = getItem(position);
        String displayName = role.getDisplayName();
        if (displayName.isEmpty()) {
            holder.binding.chip.setText(role.getName());
        } else {
            String finalDisplay = role.generateDisplayName();
            holder.binding.chip.setText(finalDisplay);
        }
    }
}
