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
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.auxiliary.enums.text.RoleRelationship;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleUiModel;
import com.wanderer.journal.databinding.ViewHolderRoleBinding;
import com.wanderer.journal.databinding.ViewHolderRoleRelationshipSeparatorBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderAdapter;

import java.util.List;
import java.util.Locale;

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
                        oldModel.getRole().getDisplayName().equals(newModel.getRole().getDisplayName()) &&
                        oldModel.getRoleAliaList().equals(newModel.getRoleAliaList()) &&
                        oldModel.getRole().getIdentity().equals(newModel.getRole().getIdentity());
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

        //注册数据变更监听器，用于自动更新圆角
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemChanged(positionStart - 1);           //更新前面的
                notifyItemChanged(positionStart + itemCount);   //更新后面的
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemChanged(positionStart - 1);   //更新前面的
                notifyItemChanged(positionStart);               //更新后面的
            }
        });
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

            //显示名称 + 名称
            String name = itemModel.model.getRole().getName();
            String displayName = itemModel.model.getRole().getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                itemHolder.binding.nameText.setText(name);
            } else {
                String finalDisplay = String.format(
                        Locale.getDefault(),
                        "%s (%s)",
                        displayName, name
                );
                itemHolder.binding.nameText.setText(finalDisplay);
            }

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
            itemHolder.binding.aliasText.setText(aliasBuilder.toString().isEmpty() ? "<无别名>" : aliasBuilder.toString());

            //身份描述
            String identity = itemModel.model.getRole().getIdentity();
            itemHolder.binding.identityText.setText(identity.isEmpty() ? "<未指明身份>" : identity);

            //设置圆角
            setRadius(itemHolder.binding.getRoot(), position);
        }
    }

    /**
     * 设置圆角
     *
     * @param view     需要设置圆角的视图
     * @param position 该视图所处的位置
     */
    private void setRadius(View view, int position) {
        if (position == 0) {    //第0个不参与圆角设置，因为它是日期分隔视图
            return;
        }

        //不需要考虑当前是分隔视图的情况，因为不是Shapable不会执行任何操作
        RoleUiModel front = getItem(position - 1);
        if (position == getItemCount() - 1) {   //处理最后一个卡片的圆角
            if (front instanceof RoleUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前一个是分隔视图，判断为单独类型
            } else {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //前一个不是分隔视图，判断为底部类型
            }
        } else {
            RoleUiModel behind = getItem(position + 1);

            if (front instanceof RoleUiModel.Separator && behind instanceof RoleUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前后都是分隔视图，判断为单独类型
            } else if (front instanceof RoleUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.TOP);    //前一个是分隔但后一个不是，判断为顶部类型
            } else if (behind instanceof RoleUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //后一个是分隔但前一个不是，判断为底部类型
            } else {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.MIDDLE); //前后都不是分隔视图，判断为中间类型
            }
        }
    }
}
