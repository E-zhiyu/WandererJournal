package com.wanderer.journal.ui.others.adapters.paragraph;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.databinding.ViewHolderInnerMediaBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;

public class ParagraphInnerMediaAdapter extends ListAdapter<MediaEntity, ParagraphInnerMediaAdapter.MediaViewHolder> {
    private final int spanCount;                //媒体列数
    private final RequestOptions glideOptions;  //图片显示设置
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

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ViewHolderInnerMediaBinding binding;

        public MediaViewHolder(@NonNull ViewHolderInnerMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 段落内部媒体列表构造方法
     *
     * @param size      显示图片大小
     * @param spanCount 媒体列数
     */
    protected ParagraphInnerMediaAdapter(int size, int spanCount) {
        super(ITEM_CALLBACK);
        this.spanCount = spanCount;

        //初始化Glide设置
        glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.outline_error_24)             //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //缓存策略
                .override(size, size);                          //图片尺寸
    }

    @NonNull
    @Override
    public ParagraphInnerMediaAdapter.MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderInnerMediaBinding binding = ViewHolderInnerMediaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ParagraphInnerMediaAdapter.MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ParagraphInnerMediaAdapter.MediaViewHolder holder, int position) {
        MediaEntity media = getItem(position);

        //设置卡片圆角
        setRadius(holder.binding.imageViewCard, position);
        setRadius(holder.binding.imageView, position);

        //停止旧图片加载（适配RecyclerView的复用逻辑）
        Glide.with(holder.itemView.getContext()).clear(holder.binding.imageView);

        //等待布局完成后指定高度
        holder.binding.imageView.post(() -> {
            int w = holder.binding.imageView.getWidth();

            //确保拿到了有效宽度，并且当前高度还不等于宽度（避免重复设置触发死循环）
            if (w > 0 && holder.binding.imageView.getLayoutParams().height != w) {
                ViewGroup.LayoutParams params = holder.binding.imageView.getLayoutParams();
                params.height = w;  //强制将高度设为与宽度一致，形成正方形
                holder.binding.imageView.setLayoutParams(params);
            }

            //通过 Glide 显示图片
            Glide.with(holder.itemView.getContext())
                    .load(media.getFileUri())
                    .apply(glideOptions)
                    .into(holder.binding.imageView);
        });
    }

    /**
     * 根据位置设置卡片圆角
     *
     * @param view     目标视图
     * @param position 在布局中的位置
     */
    private void setRadius(View view, int position) {
        //没有实现 Shapeable 接口不执行任何操作
        if (!(view instanceof Shapeable)) {
            return;
        }

        //没有媒体或只有一个媒体时不执行任何操作
        if (getItemCount() == 0 || getItemCount() == 1) {
            return;
        }

        //获取可以修改的 model
        Context context = view.getContext();
        Shapeable shapeable = (Shapeable) view;
        ShapeAppearanceModel model = shapeable.getShapeAppearanceModel();
        ShapeAppearanceModel.Builder builder = model.toBuilder();

        //处理左上角
        if (position - 1 < 0) {                                                         //只有当它前面没有媒体才设置为中等圆角
            builder.setTopLeftCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
            ));
        } else {
            builder.setTopLeftCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.SMALL_CARD_RADIUS
            ));
        }

        //处理右上角
        if (position % spanCount == spanCount - 1 && position - spanCount < 0) {        //只有在行尾且上方没有媒体才设置为中圆角
            builder.setTopRightCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
            ));
        } else {
            builder.setTopRightCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.SMALL_CARD_RADIUS
            ));
        }

        //处理左下角
        if (position % spanCount == 0 && position + spanCount >= getItemCount()) {      //只有当在行首且下方没有媒体才设置为中圆角
            builder.setBottomLeftCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
            ));
        } else {
            builder.setBottomLeftCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.SMALL_CARD_RADIUS
            ));
        }

        //处理右下角
        if (position == getItemCount() - 1 ||
                position + spanCount >= getItemCount() && position % spanCount == spanCount - 1) {  //只有后面没有媒体或在行尾且下方没有媒体才设置为中圆角
            builder.setBottomRightCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
            ));
        } else {
            builder.setBottomRightCornerSize(ViewEdgeHelper.dpToPx(
                    context,
                    AppearanceAnimationHelper.SMALL_CARD_RADIUS
            ));
        }

        //应用设置好的圆角值
        shapeable.setShapeAppearanceModel(builder.build());
    }
}
