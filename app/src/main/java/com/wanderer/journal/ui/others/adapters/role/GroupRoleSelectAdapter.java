package com.wanderer.journal.ui.others.adapters.role;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleGroupUiModel;
import com.wanderer.journal.databinding.ViewHolderGroupRoleItemBinding;
import com.wanderer.journal.databinding.ViewHolderGroupRoleSeparatorBinding;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GroupRoleSelectAdapter extends ListAdapter<RoleGroupUiModel, RecyclerView.ViewHolder> {
    private static final DiffUtil.ItemCallback<RoleGroupUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RoleGroupUiModel oldItem, @NonNull RoleGroupUiModel newItem) {
            if (oldItem instanceof RoleGroupUiModel.Item && newItem instanceof RoleGroupUiModel.Item) {
                RoleGroupUiModel.Item oldI = (RoleGroupUiModel.Item) oldItem;
                RoleGroupUiModel.Item newI = (RoleGroupUiModel.Item) newItem;
                List<Long> oldItemRoleIdList = oldI.roleList.stream()
                        .map(RoleEntity::getRoleId)
                        .collect(Collectors.toList());
                List<Long> newItemRoleIdList = newI.roleList.stream()
                        .map(RoleEntity::getRoleId)
                        .collect(Collectors.toList());
                return oldItemRoleIdList.equals(newItemRoleIdList);
            } else if (oldItem instanceof RoleGroupUiModel.Separator && newItem instanceof RoleGroupUiModel.Separator) {
                RoleGroupUiModel.Separator oldS = (RoleGroupUiModel.Separator) oldItem;
                RoleGroupUiModel.Separator newS = (RoleGroupUiModel.Separator) newItem;
                return oldS.separatorText.equals(newS.separatorText);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoleGroupUiModel oldItem, @NonNull RoleGroupUiModel newItem) {
            if (oldItem instanceof RoleGroupUiModel.Item && newItem instanceof RoleGroupUiModel.Item) {
                return true;
            } else
                return oldItem instanceof RoleGroupUiModel.Separator && newItem instanceof RoleGroupUiModel.Separator;
        }
    };
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private final OnClickListener clickListener;

    public interface OnClickListener {
        void onClick(RoleEntity role);
    }

    public static class GroupRoleItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderGroupRoleItemBinding binding;

        public GroupRoleItemViewHolder(@NonNull ViewHolderGroupRoleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 刷新 ChipGroup 中的角色 Chip
         *
         * @param roleList 角色列表
         * @param listener 角色点击后触发的监听器
         */
        public void refreshRoleChip(@NonNull List<RoleEntity> roleList, OnClickListener listener) {
            //删除之前的视图
            binding.chipGroup.removeAllViews();

            //添加新的视图
            for (RoleEntity role : roleList) {
                //实例化 Chip
                Chip roleChip = new Chip(binding.getRoot().getContext());
                roleChip.setCheckable(false);

                //设置显示名称
                if (role.getDisplayName().isEmpty()) {
                    roleChip.setText(role.getName());
                } else {
                    String finalDisplay = String.format(
                            Locale.getDefault(),
                            "%s (%s)",
                            role.getDisplayName(),
                            role.getName()
                    );
                    roleChip.setText(finalDisplay);
                }

                //添加到视图
                binding.chipGroup.addView(roleChip);

                //绑定点击监听器
                roleChip.setOnClickListener(view -> listener.onClick(role));
            }
        }
    }

    public static class GroupRoleSeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderGroupRoleSeparatorBinding binding;

        public GroupRoleSeparatorViewHolder(@NonNull ViewHolderGroupRoleSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public GroupRoleSelectAdapter(OnClickListener clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        RoleGroupUiModel item = getItem(position);
        if (item instanceof RoleGroupUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderGroupRoleItemBinding binding = ViewHolderGroupRoleItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new GroupRoleItemViewHolder(binding);
        } else {
            ViewHolderGroupRoleSeparatorBinding binding = ViewHolderGroupRoleSeparatorBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new GroupRoleSeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RoleGroupUiModel dataItem = getItem(position);
        if (dataItem instanceof RoleGroupUiModel.Item && holder instanceof GroupRoleItemViewHolder) {
            RoleGroupUiModel.Item item = (RoleGroupUiModel.Item) dataItem;
            GroupRoleItemViewHolder itemHolder = (GroupRoleItemViewHolder) holder;

            itemHolder.refreshRoleChip(item.roleList, clickListener);
        } else if (dataItem instanceof RoleGroupUiModel.Separator && holder instanceof GroupRoleSeparatorViewHolder) {
            RoleGroupUiModel.Separator separator = (RoleGroupUiModel.Separator) dataItem;
            GroupRoleSeparatorViewHolder separatorHolder = (GroupRoleSeparatorViewHolder) holder;

            separatorHolder.binding.text.setText(separator.separatorText);
        }
    }
}
