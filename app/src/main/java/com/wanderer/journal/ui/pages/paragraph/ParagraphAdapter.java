package com.wanderer.journal.ui.pages.paragraph;

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
            } else {
                return true;
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
                return false;
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
                // 如果在顶部插入了数据，通知原先的第一项（现在的第 itemCount 项）更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(itemCount);
                }

                // 如果在末尾追加了数据，通知原先的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(positionStart - 1);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                // 如果在顶部删除了数据，通知现在的第一项更新圆角
                if (positionStart == 0 && getItemCount() > itemCount) {
                    notifyItemChanged(0);
                }

                // 如果在末尾删除了数据，通知现在的最后一项更新圆角
                if (positionStart > 0) {
                    notifyItemChanged(getItemCount() - 1);
                }
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
        //TODO:完成圆角自动设置的功能
    }
}
