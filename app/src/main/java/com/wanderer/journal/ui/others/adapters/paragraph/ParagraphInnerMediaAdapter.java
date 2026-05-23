package com.wanderer.journal.ui.others.adapters.paragraph;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.databinding.ViewHolderInnerMediaBinding;

public class ParagraphInnerMediaAdapter extends ListAdapter<MediaEntity, ParagraphInnerMediaAdapter.MediaViewHolder> {
    private final RequestOptions glideOptions;          //图片显示设置
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
     * @param size 显示图片大小
     */
    protected ParagraphInnerMediaAdapter(int size) {
        super(ITEM_CALLBACK);

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
}
