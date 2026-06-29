package com.wanderer.journal.ui.others.adapters.paragraph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.text.RoleRefTextRule;
import com.wanderer.journal.auxiliary.interfaces.OnRoleClickListener;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.CrossRefWithEmotion;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;
import com.wanderer.journal.databinding.ViewHolderDateSeparatorBinding;
import com.wanderer.journal.databinding.ViewHolderParagraphBinding;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.helpers.RomanNumberHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.text.ParagraphTextConverter;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderAdapter;
import com.wanderer.journal.ui.others.method.FallbackLinkMovementMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ParagraphPagingAdapter extends PagingDataAdapter<ParagraphUiModel, RecyclerView.ViewHolder>
        implements StickyHeaderAdapter<String> {
    private SelectionTracker<Long> selectionTracker;                    // ViewHolder 选择追踪器
    private List<String> highlightedKeywordList = null;                 //当前高亮的搜索关键词
    private final Set<Long> filterEmotionIdSet = new HashSet<>();       //搜索的情绪标签 ID 集合
    private final Set<Integer> positionSet = new HashSet<>();           //当前高亮的段落下标集合
    private boolean isSelectMode = false;                               //是否是选择模式
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");
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
    private final OnParagraphClickListener paragraphClickListener;  //段落点击监听
    private final OnMediaClickedListener mediaClickedListener;      //媒体点击监听
    private final OnRoleClickListener roleClickListener;            //角色富文本点击监听

    /**
     * 设置多选追踪器
     *
     * @param selectionTracker 多选追踪器
     */
    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

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

    public interface ViewHolderListener {
        void onClicked(ParagraphEntityModel dataModel, View view);
    }

    public interface OnParagraphClickListener {
        /**
         * 段落被点击的回调
         *
         * @param dataModel 点击的段落的数据实例
         * @param view      用于显示PopupMenu的视图
         */
        void onClicked(ParagraphEntityModel dataModel, View view);
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
        ViewHolderDateSeparatorBinding binding;

        public DateSeparatorViewHolder(@NonNull ViewHolderDateSeparatorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 为 Selection 库提供信息
         *
         * @return Selection 库的 Item 信息
         */
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<>() {
                @Override
                public int getPosition() {
                    return getBindingAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return null;

                    // 通过 adapter.peek 检查当前项是不是占位符
                    ParagraphPagingAdapter adapter = (ParagraphPagingAdapter) getBindingAdapter();
                    ParagraphUiModel item = null;
                    if (adapter != null) {
                        item = adapter.peek(pos);
                    }
                    if (!(item instanceof ParagraphUiModel.Separator)) {
                        return null;
                    }

                    //转换为时间戳并返回负数 Key
                    Long timeMillis = DateTimeConverter.fromLocalDate(((ParagraphUiModel.Separator) item).date);
                    return -timeMillis;
                }
            };
        }
    }

    public static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        ViewHolderParagraphBinding binding;
        private ParagraphEntityModel data = null;   //数据实例

        public ParagraphViewHolder(@NonNull ViewHolderParagraphBinding binding, @Nullable ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置监听器
            if (listener != null) {
                //设置触摸监听
                AppearanceHelper.attachMorphAnimation(binding.getRoot());

                //设置点击监听
                binding.getRoot().setOnClickListener(view -> {
                    if (data == null) {
                        return;
                    }

                    listener.onClicked(data, binding.getRoot());
                });
            }
        }

        /**
         * 将ViewHolder与数据实例绑定
         *
         * @param data 数据实例
         */
        public void bindItem(ParagraphEntityModel data) {
            this.data = data;
        }

        /**
         * 为 Selection 库提供信息
         *
         * @return Selection 库的 Item 信息
         */
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<>() {
                @Override
                public int getPosition() {
                    return getBindingAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return null;

                    // 通过 adapter.peek 检查当前项是不是占位符
                    ParagraphPagingAdapter adapter = (ParagraphPagingAdapter) getBindingAdapter();
                    ParagraphUiModel item = null;
                    if (adapter != null) {
                        item = adapter.peek(pos);
                    }
                    if (!(item instanceof ParagraphUiModel.Item)) {
                        return null;
                    }
                    return ((ParagraphUiModel.Item) item).model.getParagraph().getParagraphId();
                }
            };
        }
    }

    /**
     * 段落适配器构造方法
     *
     * @param paragraphClickListener 段落点击监听器（传递 null 则不设置点击监听）
     * @param mediaClickedListener   媒体预览图点击监听
     */
    public ParagraphPagingAdapter(
            @Nullable OnParagraphClickListener paragraphClickListener,
            OnMediaClickedListener mediaClickedListener,
            OnRoleClickListener roleClickListener
    ) {
        super(ITEM_CALLBACK);
        this.paragraphClickListener = paragraphClickListener;
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
            ViewHolderListener listener;
            if (paragraphClickListener != null) {
                listener = (paragraphEntityModel, view) -> {
                    if (!isSelectMode) {
                        paragraphClickListener.onClicked(paragraphEntityModel, view);
                    } else {
                        selectionTracker.select(paragraphEntityModel.getParagraph().getParagraphId());
                    }
                };
            } else {
                listener = null;
            }
            return new ParagraphViewHolder(
                    binding,
                    listener
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ParagraphUiModel uiModel = getItem(position);
        if (uiModel == null) {
            holder.itemView.setVisibility(View.GONE);       //不显示占位符，防止加载时遮挡加载指示器
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);    //不是占位符时才显示
        }

        if (holder instanceof ParagraphViewHolder && uiModel instanceof ParagraphUiModel.Item) {
            ParagraphEntityModel dataModel = ((ParagraphUiModel.Item) uiModel).model;
            ParagraphEntity paragraph = dataModel.getParagraph();
            ParagraphViewHolder itemHolder = (ParagraphViewHolder) holder;
            Context context = itemHolder.binding.getRoot().getContext();

            //绑定数据原型
            itemHolder.bindItem(dataModel);

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

            //内容文本视图的属性设置
            itemHolder.binding.contentText.setMovementMethod(FallbackLinkMovementMethod.getInstance());
            itemHolder.binding.contentText.setFocusable(false);     //防止消费触摸监听
            itemHolder.binding.contentText.setClickable(false);     //防止消费点击监听
            itemHolder.binding.contentText.setLongClickable(false); //防止消费长按监听
            itemHolder.binding.contentText.setHighlightColor(Color.TRANSPARENT);

            //内容文本填充富文本
            String rawContent = paragraph.getContent(); //数据库中的原始数据
            CharSequence richText = ParagraphTextConverter.hierarchic(
                    context,
                    positionSet.contains(position) ? highlightedKeywordList : null,
                    rawContent,
                    new RoleRefTextRule() {
                        @Override
                        public void onClick(String clickData) {
                            try {
                                long roleId = Long.parseLong(clickData);
                                roleClickListener.onRoleClicked(roleId);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
            );
            itemHolder.binding.contentText.setText(richText);

            //选择状态
            if (isSelectMode) {
                //添加图标
                TypedValue typedValue = new TypedValue();
                boolean resolved = context.getTheme().resolveAttribute(
                        android.R.attr.listChoiceIndicatorMultiple,
                        typedValue,
                        true
                );
                if (resolved) {
                    Drawable drawable = AppCompatResources.getDrawable(context, typedValue.resourceId);
                    itemHolder.binding.contentText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            drawable,
                            null,
                            null,
                            null
                    );
                }
            } else {
                //去掉图标
                itemHolder.binding.contentText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                );
            }
            //设置选择状态
            itemHolder.binding.contentText.setChecked(
                    selectionTracker != null &&
                            selectionTracker.hasSelection() &&
                            selectionTracker.getSelection().contains(paragraph.getParagraphId())
            );

            //情绪标签
            List<CrossRefWithEmotion> emotionList = dataModel.getEmotionList();
            if (emotionList.isEmpty()) {
                itemHolder.binding.emotionChipGroup.setVisibility(View.GONE);
            } else {
                itemHolder.binding.emotionChipGroup.removeAllViews();   //先清空所有情绪标签
                for (CrossRefWithEmotion emotion : emotionList) {
                    long emotionId = emotion.emotionTag.getEmotionId();
                    String name = emotion.emotionTag.getName();
                    int degree = emotion.crossRef.getDegree();
                    String title = String.format(
                            Locale.getDefault(),
                            "%s %s",
                            name, RomanNumberHelper.toRoman(degree)
                    );

                    //添加 Chip 到视图中
                    Chip emotionChip = getEmotionChip(context, emotionId, title);
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
        } else if (holder instanceof DateSeparatorViewHolder && uiModel instanceof ParagraphUiModel.Separator) {
            DateSeparatorViewHolder separatorViewHolder = (DateSeparatorViewHolder) holder;

            String dateStr = ((ParagraphUiModel.Separator) uiModel).date.format(formatter);
            separatorViewHolder.binding.dateText.setText(dateStr);
            separatorViewHolder.binding.getRoot().setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新选择模式
     *
     * @param selectMode 是否为选择模式
     */
    public void setSelectMode(boolean selectMode) {
        if (isSelectMode == selectMode) return;

        isSelectMode = selectMode;
        notifyItemRangeChanged(0, getItemCount());
    }

    public boolean getSelectMode() {
        return isSelectMode;
    }

    /**
     * 获取情绪标签 Chip 视图
     *
     * @param context   上下文
     * @param emotionId 情绪标签 ID
     * @param title     情绪标签名称
     * @return 显示情绪标签名称的{@link Chip}实例
     */
    @NonNull
    private Chip getEmotionChip(Context context, long emotionId, String title) {
        Chip emotionChip = new Chip(
                context,
                null,
                com.google.android.material.R.style.Widget_Material3_Chip_Suggestion
        );
        emotionChip.setFocusable(false);

        //设置是否选中（即高亮）
        boolean isHighLighted = filterEmotionIdSet.contains(emotionId);
        emotionChip.setCheckable(isHighLighted);
        emotionChip.setChecked(isHighLighted);

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

    /**
     * 设置高亮
     *
     * @param keywordList  高亮关键词列表
     * @param positionList 有符合关键词的视图的下标
     */
    public void setHighlightTarget(List<String> keywordList, Set<Long> filterEmotionIdList, @NonNull List<Integer> positionList) {
        //修改搜索元素数据
        this.highlightedKeywordList = keywordList;
        this.filterEmotionIdSet.clear();
        this.filterEmotionIdSet.addAll(filterEmotionIdList);

        //更改位置列表中的内容
        List<Integer> oldPositionList = new ArrayList<>(positionList);
        this.positionSet.clear();
        this.positionSet.addAll(positionList);

        //提醒旧的取消高亮
        for (int i : oldPositionList) {
            notifyItemChanged(i);
        }

        //提醒新的进行高亮
        for (int i : positionList) {
            notifyItemChanged(i);
        }
    }

    /**
     * 清除高亮
     */
    public void clearHighlight() {
        this.highlightedKeywordList = null;
        this.filterEmotionIdSet.clear();
        for (int position : positionSet) {
            notifyItemChanged(position);
        }
        positionSet.clear();
    }
}
