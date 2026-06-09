package com.wanderer.journal.ui.pages.role;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.dropdown.RoleRelationship;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.RoleUiModel;
import com.wanderer.journal.databinding.ViewHolderRoleBinding;
import com.wanderer.journal.databinding.ViewHolderRoleRelationshipSeparatorBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderAdapter;

import java.util.List;

public class RoleAdapter extends ListAdapter<RoleUiModel, RecyclerView.ViewHolder>
        implements StickyHeaderAdapter<String> {
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private static final DiffUtil.ItemCallback<RoleUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RoleUiModel oldItem, @NonNull RoleUiModel newItem) {
            if (oldItem instanceof RoleUiModel.Item && newItem instanceof RoleUiModel.Item) {
                RoleEntityModel oldModel = ((RoleUiModel.Item) oldItem).model;
                RoleEntityModel newModel = ((RoleUiModel.Item) newItem).model;
                return oldModel.getRole().getRoleId() == newModel.getRole().getRoleId();
            } else if (oldItem instanceof RoleUiModel.Separator && newItem instanceof RoleUiModel.Separator) {
                String oldSeparator = ((RoleUiModel.Separator) oldItem).relationship;
                String newSeparator = ((RoleUiModel.Separator) newItem).relationship;
                return oldSeparator.equals(newSeparator);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoleUiModel oldItem, @NonNull RoleUiModel newItem) {
            if (oldItem instanceof RoleUiModel.Item && newItem instanceof RoleUiModel.Item) {
                RoleEntityModel oldModel = ((RoleUiModel.Item) oldItem).model;
                RoleEntityModel newModel = ((RoleUiModel.Item) newItem).model;
                return oldModel.getRole().getName().equals(newModel.getRole().getName()) &&
                        oldModel.getRoleAliaList().equals(newModel.getRoleAliaList());
            } else
                return oldItem instanceof RoleUiModel.Separator && newItem instanceof RoleUiModel.Separator;
        }
    };

    private final OnClickedListener clickedListener;            //单击监听
    private final OnLongClickedListener longClickedListener;    //长按监听

    public interface ViewHolderListener {
        void onClicked(int position);

        void onLongClicked(int position, View anchor);
    }

    public interface OnClickedListener {
        void onClicked(RoleUiModel model);
    }

    public interface OnLongClickedListener {
        void onLongClicked(RoleUiModel model, View anchor);
    }

    public static class RelationshipSeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderRoleRelationshipSeparatorBinding binding;

        public RelationshipSeparatorViewHolder(@NonNull ViewHolderRoleRelationshipSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class RoleViewHolder extends RecyclerView.ViewHolder {
        ViewHolderRoleBinding binding;

        public RoleViewHolder(@NonNull ViewHolderRoleBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听器
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClicked(getBindingAdapterPosition()));

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClicked(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    public RoleAdapter(
            OnClickedListener clickedListener,
            OnLongClickedListener longClickedListener
    ) {
        super(ITEM_CALLBACK);
        this.clickedListener = clickedListener;
        this.longClickedListener = longClickedListener;
    }

    @Override
    public boolean isHeader(int position) {
        return getItem(position) instanceof RoleUiModel.Separator;
    }

    @Override
    public String getHeaderData(int position, Context context) {
        RoleUiModel model = getItem(position);
        if (model instanceof RoleUiModel.Separator) {
            return ((RoleUiModel.Separator) model).relationship;
        } else if (model instanceof RoleUiModel.Item) {
            int relationship = ((RoleUiModel.Item) model).model.getRole().getRelationship();
            return RoleRelationship.values()[relationship].getTitle();
        } else {
            return context.getString(R.string.not_applicable);
        }
    }

    @Override
    public int getItemViewType(int position) {
        RoleUiModel item = getItem(position);
        if (item instanceof RoleUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderRoleBinding binding = ViewHolderRoleBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new RoleViewHolder(
                    binding,
                    new ViewHolderListener() {
                        @Override
                        public void onClicked(int position) {
                            RoleUiModel model = getItem(position);
                            clickedListener.onClicked(model);
                        }

                        @Override
                        public void onLongClicked(int position, View anchor) {
                            RoleUiModel model = getItem(position);
                            longClickedListener.onLongClicked(model, anchor);
                        }
                    }
            );
        } else {
            ViewHolderRoleRelationshipSeparatorBinding binding = ViewHolderRoleRelationshipSeparatorBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new RelationshipSeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RoleUiModel model = getItem(position);
        if (model instanceof RoleUiModel.Separator && holder instanceof RelationshipSeparatorViewHolder) {
            String relationship = ((RoleUiModel.Separator) model).relationship;
            ((RelationshipSeparatorViewHolder) holder).binding.separatorText.setText(relationship);
        } else if (model instanceof RoleUiModel.Item && holder instanceof RoleViewHolder) {
            RoleUiModel.Item itemModel = (RoleUiModel.Item) model;
            RoleViewHolder itemHolder = (RoleViewHolder) holder;

            //名称
            String name = itemModel.model.getRole().getName();
            itemHolder.binding.nameText.setText(name);

            //别名
            StringBuilder aliasBuilder = new StringBuilder();
            List<RoleAliaEntity> aliaList = itemModel.model.getRoleAliaList();
            int i = 0;
            for (RoleAliaEntity alia : aliaList) {
                aliasBuilder.append(alia.getAlia().trim());
                if (i < aliaList.size() - 1) {
                    aliasBuilder.append("，");
                }
                i++;
            }
            itemHolder.binding.aliasText.setText(aliasBuilder.toString());

            //身份描述
            itemHolder.binding.identityText.setText(itemModel.model.getRole().getIdentity());
        }
    }
}
