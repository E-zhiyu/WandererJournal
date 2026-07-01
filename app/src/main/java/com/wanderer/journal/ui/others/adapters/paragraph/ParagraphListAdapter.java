package com.wanderer.journal.ui.others.adapters.paragraph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.text.RoleRefTextRule;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.auxiliary.interfaces.OnRoleClickListener;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.CrossRefWithEmotion;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.databinding.ViewHolderSeparatorTextChipBinding;
import com.wanderer.journal.databinding.ViewHolderParagraphBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderAdapter;
import com.wanderer.journal.ui.others.method.FallbackLinkMovementMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParagraphListAdapter extends ListAdapter<ParagraphUiModel, RecyclerView.ViewHolder>
        implements StickyHeaderAdapter<String> {
    private final static DiffUtil.ItemCallback<ParagraphUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParagraphUiModel oldItem, @NonNull ParagraphUiModel newItem) {
            if (oldItem instanceof ParagraphUiModel.Item && newItem instanceof ParagraphUiModel.Item) {
                ParagraphEntity oldParagraph = ((ParagraphUiModel.Item) oldItem).model.getParagraph();
                ParagraphEntity newParagraph = ((ParagraphUiModel.Item) newItem).model.getParagraph();
                return oldParagraph.getParagraphId() == newParagraph.getParagraphId();
            } else if (oldItem instanceof ParagraphUiModel.Separator && newItem instanceof ParagraphUiModel.Separator) {
                LocalDate oldDateStr = ((ParagraphUiModel.Separator) oldItem).date;
                LocalDate newDateStr = ((ParagraphUiModel.Separator) newItem).date;
                return oldDateStr.isEqual(newDateStr);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParagraphUiModel oldItem, @NonNull ParagraphUiModel newItem) {
            if (oldItem instanceof ParagraphUiModel.Item && newItem instanceof ParagraphUiModel.Item) {
                ParagraphEntity oldParagraph = ((ParagraphUiModel.Item) oldItem).model.getParagraph();
                ParagraphEntity newParagraph = ((ParagraphUiModel.Item) newItem).model.getParagraph();
                List<CrossRefWithEmotion> oldEmotionList = ((ParagraphUiModel.Item) oldItem).model.getEmotionList();
                List<CrossRefWithEmotion> newEmotionList = ((ParagraphUiModel.Item) newItem).model.getEmotionList();
                List<MediaEntity> oldMediaList = ((ParagraphUiModel.Item) oldItem).model.getMediaList();
                List<MediaEntity> newMediaList = ((ParagraphUiModel.Item) newItem).model.getMediaList();
                return oldParagraph.getContent().equals(newParagraph.getContent()) &&
                        oldParagraph.getCreateTime().isEqual(newParagraph.getCreateTime()) &&
                        oldEmotionList.equals(newEmotionList) &&
                        oldMediaList.equals(newMediaList);
            } else {
                return true;
            }
        }
    };
    private final static int TYPE_ITEM = 1;         //段落内容ViewHolder种类
    private final static int TYPE_SEPARATOR = 0;    //分隔ViewHolder种类
    private final ParagraphListAdapter.OnMediaClickedListener mediaClickedListener;     //媒体点击监听
    private final OnRoleClickListener roleClickListener;                                //角色富文本点击监听
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");

    @Override
    public boolean isHeader(int position) {
        ParagraphUiModel model = getItem(position);
        return model instanceof ParagraphUiModel.Separator;
    }

    @Override
    public String getHeaderData(int position, Context context) {
        ParagraphUiModel model = getItem(position);
        if (model instanceof ParagraphUiModel.Separator) {
            return ((ParagraphUiModel.Separator) model).date.format(formatter);
        } else if (model instanceof ParagraphUiModel.Item) {
            return ((ParagraphUiModel.Item) model).model.getParagraph().getCreateTime().format(formatter);
        } else {
            return context.getString(R.string.not_applicable);
        }
    }

    public interface OnMediaClickedListener {
        /**
         * 媒体视图点击监听
         *
         * @param position  被点击的媒体所在的位置
         * @param mediaView 被点击的媒体视图
         * @param mediaList 同一个段落的媒体列表
         */
        void onClicked(int position, View mediaView, List<MediaEntity> mediaList);
    }

    public static class DateSeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSeparatorTextChipBinding binding;

        public DateSeparatorViewHolder(@NonNull ViewHolderSeparatorTextChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        ViewHolderParagraphBinding binding;

        public ParagraphViewHolder(@NonNull ViewHolderParagraphBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 段落适配器构造方法
     *
     * @param mediaClickedListener 媒体预览图点击监听
     */
    public ParagraphListAdapter(
            ParagraphListAdapter.OnMediaClickedListener mediaClickedListener,
            OnRoleClickListener roleClickListener
    ) {
        super(ITEM_CALLBACK);
        this.mediaClickedListener = mediaClickedListener;
        this.roleClickListener = roleClickListener;

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
            return new ParagraphListAdapter.ParagraphViewHolder(binding);
        } else {
            ViewHolderSeparatorTextChipBinding binding = ViewHolderSeparatorTextChipBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new ParagraphListAdapter.DateSeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ParagraphUiModel uiModel = getItem(position);

        if (holder instanceof ParagraphListAdapter.ParagraphViewHolder && uiModel instanceof ParagraphUiModel.Item) {
            ParagraphEntityModel dataModel = ((ParagraphUiModel.Item) uiModel).model;
            ParagraphEntity paragraph = dataModel.getParagraph();
            ParagraphListAdapter.ParagraphViewHolder itemHolder = (ParagraphListAdapter.ParagraphViewHolder) holder;
            Context context = itemHolder.binding.getRoot().getContext();

            //媒体列表
            List<MediaEntity> mediaList = dataModel.getMediaList();
            if (mediaList != null && !mediaList.isEmpty()) {
                //动态动态调整网格列数：1张图显示1列，2张图2列，3张及以上显示3列
                int spanCount = Math.min(mediaList.size(), 3);
                int size = itemHolder.binding.getRoot().getWidth() / spanCount;
                GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
                itemHolder.binding.mediaRecycler.setLayoutManager(layoutManager);

                //绑定数据
                ParagraphInnerMediaAdapter mediaAdapter = new ParagraphInnerMediaAdapter(
                        size,
                        spanCount,
                        (mediaPosition, view) -> mediaClickedListener.onClicked(mediaPosition, view, mediaList)
                );
                itemHolder.binding.mediaRecycler.setAdapter(mediaAdapter);
                mediaAdapter.submitList(mediaList);

                //显示列表
                itemHolder.binding.mediaRecycler.setVisibility(View.VISIBLE);
            } else {
                itemHolder.binding.mediaRecycler.setVisibility(View.GONE);
            }

            //内容文本视图设置部分属性
            itemHolder.binding.contentText.setMovementMethod(FallbackLinkMovementMethod.getInstance());
            itemHolder.binding.contentText.setFocusable(false);     //防止消费触摸监听
            itemHolder.binding.contentText.setClickable(false);     //防止消费点击监听
            itemHolder.binding.contentText.setLongClickable(false); //防止消费长按监听
            itemHolder.binding.contentText.setHighlightColor(Color.TRANSPARENT);

            //内容文本填充富文本
            String rawContent = paragraph.getContent();
            CharSequence richText = TextHelper.hierarchicFromString(context, null, rawContent, new RoleRefTextRule() {
                @Override
                public void onClick(String clickData) {
                    try {
                        long roleId = Long.parseLong(clickData);
                        roleClickListener.onRoleClicked(roleId);
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            itemHolder.binding.contentText.setText(richText);

            //情绪标签
            List<CrossRefWithEmotion> emotionList = dataModel.getEmotionList();
            if (emotionList.isEmpty()) {
                itemHolder.binding.emotionChipGroup.setVisibility(View.GONE);
            } else {
                itemHolder.binding.emotionChipGroup.removeAllViews();   //先清空所有情绪标签
                for (CrossRefWithEmotion emotion : emotionList) {
                    String title = emotion.generateDisplayText();

                    //添加 Chip 到视图中
                    Chip emotionChip = getEmotionChip(context, title);
                    itemHolder.binding.emotionChipGroup.addView(emotionChip);
                }

                itemHolder.binding.emotionChipGroup.setVisibility(View.VISIBLE);
            }

            //时间
            LocalDateTime dateTime = paragraph.getCreateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            itemHolder.binding.dateTimeText.setText(dateTime.format(formatter));

            //设置圆角
            setRadius(itemHolder.binding.getRoot(), position);
        } else if (holder instanceof ParagraphListAdapter.DateSeparatorViewHolder && uiModel instanceof ParagraphUiModel.Separator) {
            ParagraphListAdapter.DateSeparatorViewHolder separatorViewHolder = (ParagraphListAdapter.DateSeparatorViewHolder) holder;

            //分隔符文字
            String dateStr = ((ParagraphUiModel.Separator) uiModel).date.format(formatter);
            separatorViewHolder.binding.separatorText.setText(dateStr);
        }
    }

    /**
     * 获取情绪标签 Chip 视图
     *
     * @param context 上下文
     * @param title   情绪标签名称
     * @return 显示情绪标签名称的{@link Chip}实例
     */
    @NonNull
    private Chip getEmotionChip(Context context, String title) {
        Chip emotionChip = new Chip(
                context,
                null,
                com.google.android.material.R.style.Widget_Material3_Chip_Suggestion
        );
        emotionChip.setFocusable(false);

        //设置显示文本
        emotionChip.setText(title);
        emotionChip.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_LabelMedium
        );
        return emotionChip;
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
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前一个是分隔视图，判断为单独类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //前一个不是分隔视图，判断为底部类型
            }
        } else {
            ParagraphUiModel behind = getItem(position + 1);

            if (front instanceof ParagraphUiModel.Separator && behind instanceof ParagraphUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前后都是分隔视图，判断为单独类型
            } else if (front instanceof ParagraphUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.TOP);    //前一个是分隔但后一个不是，判断为顶部类型
            } else if (behind instanceof ParagraphUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //后一个是分隔但前一个不是，判断为底部类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.MIDDLE); //前后都不是分隔视图，判断为中间类型
            }
        }
    }
}
