package com.wanderer.journal.ui.others.adapters;

import android.content.Context;
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
import com.wanderer.journal.databinding.ViewHolderMediaBinding;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;

public class MediaAdapter extends ListAdapter<MediaEntity, MediaAdapter.MediaViewHolder> {
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

    /**
     * 图片列表适配器构造方法
     *
     * @param context 上下文
     */
    public MediaAdapter(Context context) {
        super(ITEM_CALLBACK);

        glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.outline_error_24)             //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //缓存策略
                .override(
                        ViewEdgeHelper.dpToPx(context, 200),
                        ViewEdgeHelper.dpToPx(context, 200)
                );                                              //图片尺寸
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
        Glide.with(holder.itemView.getContext())
                .load(media.getFileUri())
                .apply(glideOptions)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.binding.imageView);
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMediaBinding binding;

        public MediaViewHolder(@NonNull ViewHolderMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
