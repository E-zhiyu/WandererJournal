package com.wanderer.journal.ui.others.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.databinding.ViewHolderMediaBinding;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;

public class MediaAdapter extends ListAdapter<MediaEntity, MediaAdapter.MediaViewHolder> {
    private SelectionTracker<Long> selectionTracker;    // ViewHolder 选择追踪器
    private final RequestOptions glideOptions;          //图片显示设置
    private boolean isSelectMode = false;               //是否是选择模式
    private final static DiffUtil.ItemCallback<MediaEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaEntity oldItem, @NonNull MediaEntity newItem) {
            return oldItem.getFileUri().equals(newItem.getFileUri());   //仅判断图片 Uri 是否相同
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaEntity oldItem, @NonNull MediaEntity newItem) {
            return true;
        }
    };

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMediaBinding binding;
        private final SpringAnimation scaleXAnim;           //X轴缩放动画
        private final SpringAnimation scaleYAnim;           //Y轴缩放动画
        private static final float PRESSED_SCALE = 0.9f;    //按下时缩放程度
        private MediaEntity media;                          //媒体实例

        public MediaViewHolder(@NonNull ViewHolderMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            //设置缩放动画
            scaleXAnim = new SpringAnimation(binding.imageCard, SpringAnimation.SCALE_X);
            scaleYAnim = new SpringAnimation(binding.imageCard, SpringAnimation.SCALE_Y);
            initScaleAnimation();
        }

        /**
         * 绑定媒体实例
         *
         * @param media 媒体实例
         */
        public void bindMedia(MediaEntity media) {
            this.media = media;
        }

        /**
         * 初始化缩放动画
         */
        private void initScaleAnimation() {
            SpringForce forceX = new SpringForce(1f);
            SpringForce forceY = new SpringForce(1f);

            forceX.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            forceY.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

            forceX.setStiffness(SpringForce.STIFFNESS_LOW);
            forceY.setStiffness(SpringForce.STIFFNESS_LOW);

            scaleXAnim.setSpring(forceX);
            scaleYAnim.setSpring(forceY);
        }

        /**
         * 切换选择
         *
         * @param isChecked 是否选中
         */
        public void setChecked(boolean isChecked) {
            if (isChecked) {
                scaleXAnim.animateToFinalPosition(PRESSED_SCALE);
                scaleYAnim.animateToFinalPosition(PRESSED_SCALE);
            } else {
                scaleXAnim.animateToFinalPosition(1f);
                scaleYAnim.animateToFinalPosition(1f);
            }
            binding.checkedText.setChecked(isChecked);
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
                    // 必须严格过滤 NO_POSITION
                    return (pos != RecyclerView.NO_POSITION && pos < getItemCount()) ?
                            media.getItemId() : null;
                }
            };
        }
    }

    /**
     * 图片列表适配器构造方法
     *
     * @param context 上下文
     */
    public MediaAdapter(Context context) {
        super(ITEM_CALLBACK);

        //初始化Glide设置
        glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.outline_error_24)             //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //缓存策略
                .override(
                        ViewEdgeHelper.dpToPx(context, 200),
                        ViewEdgeHelper.dpToPx(context, 200)
                );                                              //图片尺寸

        //启用Stable Ids
        setHasStableIds(true);
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    /**
     * 更新选择模式
     *
     * @param selectMode 是否为选择模式
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setSelectMode(boolean selectMode) {
        if (isSelectMode == selectMode) return;

        isSelectMode = selectMode;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemId();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderMediaBinding binding = ViewHolderMediaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaEntity media = getItem(position);

        //设置缩放
        boolean isChecked = selectionTracker.isSelected(media.getItemId());
        holder.setChecked(isChecked);

        //通过 Glide 显示图片
        holder.bindMedia(media);
        Glide.with(holder.itemView.getContext())
                .load(media.getFileUri())
                .apply(glideOptions)
                .into(holder.binding.imageView);

        //设置复选框是否显示
        if (isSelectMode) {
            holder.binding.checkedText.setVisibility(View.VISIBLE);
        } else {
            holder.binding.checkedText.setVisibility(View.GONE);
        }
    }

    /**
     * 通过{@link Long}类型的 ID 查找 Item
     *
     * @param id 通过{@link #getItemId(int)}返回的 ID
     * @return 若找到则返回 Item 实例，否则返回 null
     */
    @Nullable
    public MediaEntity getItemById(long id) {
        for (MediaEntity media : getCurrentList()) {
            if (media.getItemId() == id) {
                return media;
            }
        }
        return null;
    }
}
