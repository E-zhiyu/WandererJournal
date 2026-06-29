package com.wanderer.journal.ui.pages.emotion;

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
import com.wanderer.journal.auxiliary.enums.text.EmotionType;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.EmotionListUiModel;
import com.wanderer.journal.databinding.ViewHolderEmotionTagBinding;
import com.wanderer.journal.databinding.ViewHolderRoleRelationshipSeparatorBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderAdapter;

public class EmotionTagAdapter extends ListAdapter<EmotionListUiModel, RecyclerView.ViewHolder>
        implements StickyHeaderAdapter<String> {
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private final OnClickedListener clickedListener;            //点击监听器
    private final OnLongClickedListener longClickedListener;    //长按监听器
    private static final DiffUtil.ItemCallback<EmotionListUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull EmotionListUiModel oldItem, @NonNull EmotionListUiModel newItem) {
            if (oldItem instanceof EmotionListUiModel.Item && newItem instanceof EmotionListUiModel.Item) {
                EmotionListUiModel.Item oldI = (EmotionListUiModel.Item) oldItem;
                EmotionListUiModel.Item newI = (EmotionListUiModel.Item) newItem;
                return oldI.entity.getEmotionId() == newI.entity.getEmotionId();
            } else if (oldItem instanceof EmotionListUiModel.Separator && newItem instanceof EmotionListUiModel.Separator) {
                EmotionListUiModel.Separator oldS = (EmotionListUiModel.Separator) oldItem;
                EmotionListUiModel.Separator newS = (EmotionListUiModel.Separator) newItem;
                return oldS.text.equals(newS.text);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmotionListUiModel oldItem, @NonNull EmotionListUiModel newItem) {
            if (oldItem instanceof EmotionListUiModel.Item && newItem instanceof EmotionListUiModel.Item) {
                EmotionListUiModel.Item oldI = (EmotionListUiModel.Item) oldItem;
                EmotionListUiModel.Item newI = (EmotionListUiModel.Item) newItem;
                return oldI.entity.getType() == newI.entity.getType() &&
                        oldI.entity.getName().equals(newI.entity.getName()) &&
                        oldI.entity.getDescription().equals(newI.entity.getDescription());
            } else
                return oldItem instanceof EmotionListUiModel.Separator && newItem instanceof EmotionListUiModel.Separator;
        }
    };

    public static class EmotionTagViewHolder extends RecyclerView.ViewHolder {
        ViewHolderEmotionTagBinding binding;

        public EmotionTagViewHolder(@NonNull ViewHolderEmotionTagBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClicked(getBindingAdapterPosition()));

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClicked(getBindingAdapterPosition(), view);
                return true;
            });
        }
    }

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderRoleRelationshipSeparatorBinding binding;

        public SeparatorViewHolder(@NonNull ViewHolderRoleRelationshipSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnClickedListener {
        /**
         * 点击监听
         *
         * @param emotionTag 电机的情绪标签
         */
        void onClicked(EmotionTagEntity emotionTag);
    }

    public interface OnLongClickedListener {
        /**
         * 长按监听
         *
         * @param emotionTag 长按的情绪标签
         * @param view       PopupMenu绑定的视图
         */
        void onLongClicked(EmotionTagEntity emotionTag, View view);
    }

    public interface ViewHolderListener {
        void onClicked(int position);

        void onLongClicked(int position, View view);
    }

    /**
     * 情绪标签列表适配器构造方法
     *
     * @param clickedListener 点击监听器
     */
    public EmotionTagAdapter(OnClickedListener clickedListener, OnLongClickedListener longClickedListener) {
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

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemChanged(fromPosition - 1);    //更新前面的
                notifyItemChanged(fromPosition);                //更新后面的

                notifyItemChanged(toPosition - 1);      //更新前面的
                notifyItemChanged(toPosition + 1);      //更新后面的
            }
        });
    }

    @Override
    public boolean isHeader(int position) {
        return getItem(position) instanceof EmotionListUiModel.Separator;
    }

    @Override
    public String getHeaderData(int position, Context context) {
        EmotionListUiModel model = getItem(position);
        if (model instanceof EmotionListUiModel.Separator) {
            return ((EmotionListUiModel.Separator) model).text;
        } else if (model instanceof EmotionListUiModel.Item) {
            int type = ((EmotionListUiModel.Item) model).entity.getType();
            return EmotionType.values()[type].getTitle();
        } else {
            return context.getString(R.string.not_applicable);
        }
    }

    @Override
    public int getItemViewType(int position) {
        EmotionListUiModel item = getItem(position);
        if (item instanceof EmotionListUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderEmotionTagBinding binding = ViewHolderEmotionTagBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new EmotionTagViewHolder(
                    binding,
                    new ViewHolderListener() {
                        @Override
                        public void onClicked(int position) {
                            EmotionListUiModel emotionTag = getItem(position);
                            if (emotionTag instanceof EmotionListUiModel.Item) {
                                clickedListener.onClicked(((EmotionListUiModel.Item) emotionTag).entity);
                            }
                        }

                        @Override
                        public void onLongClicked(int position, View view) {
                            EmotionListUiModel emotionTag = getItem(position);
                            if (emotionTag instanceof EmotionListUiModel.Item) {
                                longClickedListener.onLongClicked(((EmotionListUiModel.Item) emotionTag).entity, view);
                            }
                        }
                    }
            );
        } else {
            ViewHolderRoleRelationshipSeparatorBinding binding = ViewHolderRoleRelationshipSeparatorBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new SeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EmotionListUiModel model = getItem(position);

        if (model instanceof EmotionListUiModel.Item && holder instanceof EmotionTagViewHolder) {
            EmotionTagEntity emotionTag = ((EmotionListUiModel.Item) model).entity;
            EmotionTagViewHolder itemHolder = (EmotionTagViewHolder) holder;

            //标签名称
            itemHolder.binding.nameText.setText(emotionTag.getName());

            //标签描述
            String description = emotionTag.getDescription();
            itemHolder.binding.descriptionText.setText(description.isEmpty() ? "<无描述>" : description);

            //设置圆角
            setRadius(itemHolder.binding.getRoot(), position);
        } else if (model instanceof EmotionListUiModel.Separator && holder instanceof SeparatorViewHolder) {
            String text = ((EmotionListUiModel.Separator) model).text;
            ((SeparatorViewHolder) holder).binding.separatorText.setText(text);
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
        EmotionListUiModel front = getItem(position - 1);
        if (position == getItemCount() - 1) {   //处理最后一个卡片的圆角
            if (front instanceof EmotionListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前一个是分隔视图，判断为单独类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //前一个不是分隔视图，判断为底部类型
            }
        } else {
            EmotionListUiModel behind = getItem(position + 1);

            if (front instanceof EmotionListUiModel.Separator && behind instanceof EmotionListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前后都是分隔视图，判断为单独类型
            } else if (front instanceof EmotionListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.TOP);    //前一个是分隔但后一个不是，判断为顶部类型
            } else if (behind instanceof EmotionListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //后一个是分隔但前一个不是，判断为底部类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.MIDDLE); //前后都不是分隔视图，判断为中间类型
            }
        }
    }
}
