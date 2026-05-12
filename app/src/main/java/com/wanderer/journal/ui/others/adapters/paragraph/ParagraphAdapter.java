package com.wanderer.journal.ui.others.adapters.paragraph;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ViewHolderDateSeparatorBinding;
import com.wanderer.journal.databinding.ViewHolderParagraphBinding;
import com.wanderer.journal.enums.RadiusStyle;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParagraphAdapter extends PagingDataAdapter<ParagraphUiModel, RecyclerView.ViewHolder> {
    private final static DiffUtil.ItemCallback<ParagraphUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParagraphUiModel oldItem, @NonNull ParagraphUiModel newItem) {
            if (oldItem instanceof ParagraphUiModel.Item && newItem instanceof ParagraphUiModel.Item) {
                ParagraphEntity oldParagraph = ((ParagraphUiModel.Item) oldItem).paragraph;
                ParagraphEntity newParagraph = ((ParagraphUiModel.Item) newItem).paragraph;
                return oldParagraph.getParagraphId() == newParagraph.getParagraphId();
            } else if (oldItem instanceof ParagraphUiModel.Separator && newItem instanceof ParagraphUiModel.Separator) {
                String oldDateStr = ((ParagraphUiModel.Separator) oldItem).date;
                String newDateStr = ((ParagraphUiModel.Separator) newItem).date;
                return oldDateStr.equals(newDateStr);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParagraphUiModel oldItem, @NonNull ParagraphUiModel newItem) {
            if (oldItem instanceof ParagraphUiModel.Item && newItem instanceof ParagraphUiModel.Item) {
                ParagraphEntity oldParagraph = ((ParagraphUiModel.Item) oldItem).paragraph;
                ParagraphEntity newParagraph = ((ParagraphUiModel.Item) newItem).paragraph;
                return oldParagraph.getContent().equals(newParagraph.getContent()) &&
                        oldParagraph.getCreateTime().isEqual(newParagraph.getCreateTime());
            } else {
                return true;
            }
        }
    };
    private final static int TYPE_ITEM = 1;         //段落内容ViewHolder种类
    private final static int TYPE_SEPARATOR = 0;    //分隔ViewHolder种类
    private final OnClickListener listener;

    public interface ViewHolderListener {
        void onClicked(ParagraphEntity paragraph, View view);
    }

    public interface OnClickListener {
        /**
         * 段落被点击的回调
         *
         * @param paragraph 被点击的段落
         * @param view      用于显示PopupMenu的视图
         */
        void onClicked(ParagraphEntity paragraph, View view);
    }

    public static class DateSeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderDateSeparatorBinding binding;

        public DateSeparatorViewHolder(@NonNull ViewHolderDateSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        ViewHolderParagraphBinding binding;
        ParagraphEntity paragraph = null;   //段落实例

        public ParagraphViewHolder(@NonNull ViewHolderParagraphBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置监听器
            binding.getRoot().setOnClickListener(view -> {
                if (paragraph == null) {
                    return;
                }

                listener.onClicked(paragraph, binding.getRoot());
            });
        }

        /**
         * 将ViewHolder与数据实例绑定
         *
         * @param paragraph 段落实例
         */
        public void bindItem(ParagraphEntity paragraph) {
            this.paragraph = paragraph;
        }
    }

    /**
     * 段落适配器构造方法
     *
     * @param listener 段落点击监听器
     */
    public ParagraphAdapter(OnClickListener listener) {
        super(ITEM_CALLBACK);
        this.listener = listener;

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
    public int getItemViewType(int position) {
        ParagraphUiModel item = getItem(position);
        if (item instanceof ParagraphUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderParagraphBinding binding = ViewHolderParagraphBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new ParagraphViewHolder(
                    binding,
                    listener::onClicked
            );
        } else {
            ViewHolderDateSeparatorBinding binding = ViewHolderDateSeparatorBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new DateSeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ParagraphUiModel uiModel = getItem(position);
        if (uiModel == null) {
            if (holder instanceof ParagraphViewHolder) {
                ParagraphViewHolder itemHolder = (ParagraphViewHolder) holder;
                itemHolder.binding.contentText.setText("正在加载段落内容……");
                itemHolder.binding.dateTimeText.setText("未知");
            }
            return;
        }

        if (holder instanceof ParagraphViewHolder && uiModel instanceof ParagraphUiModel.Item) {
            ParagraphEntity paragraph = ((ParagraphUiModel.Item) uiModel).paragraph;
            ParagraphViewHolder itemHolder = (ParagraphViewHolder) holder;

            itemHolder.bindItem(paragraph);

            String content = paragraph.getContent();
            itemHolder.binding.contentText.setText(content);

            LocalDateTime dateTime = paragraph.getCreateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            itemHolder.binding.dateTimeText.setText(dateTime.format(formatter));

            //设置圆角
            setRadius(itemHolder.binding.getRoot(), position);
        } else if (holder instanceof DateSeparatorViewHolder && uiModel instanceof ParagraphUiModel.Separator) {
            DateSeparatorViewHolder separatorViewHolder = (DateSeparatorViewHolder) holder;

            String dateStr = ((ParagraphUiModel.Separator) uiModel).date;
            separatorViewHolder.binding.dateText.setText(dateStr);
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
        ParagraphUiModel front = getItem(position - 1);
        if (position == getItemCount() - 1) {   //处理最后一个卡片的圆角
            if (front instanceof ParagraphUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前一个是分隔视图，判断为单独类型
            } else {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //前一个不是分隔视图，判断为底部类型
            }
        } else {
            ParagraphUiModel behind = getItem(position + 1);

            if (front instanceof ParagraphUiModel.Separator && behind instanceof ParagraphUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前后都是分隔视图，判断为单独类型
            } else if (front instanceof ParagraphUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.TOP);    //前一个是分隔但后一个不是，判断为顶部类型
            } else if (behind instanceof ParagraphUiModel.Separator) {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //后一个是分隔但前一个不是，判断为底部类型
            } else {
                AppearanceAnimationHelper.setRadiusStyle(view, RadiusStyle.MIDDLE); //前后都不是分隔视图，判断为中间类型
            }
        }
    }
}
