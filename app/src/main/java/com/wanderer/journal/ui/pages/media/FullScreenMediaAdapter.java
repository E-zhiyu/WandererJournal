package com.wanderer.journal.ui.pages.media;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wanderer.journal.databinding.ViewHolderFullScreenMediaBinding;

public class FullScreenMediaAdapter
        extends RecyclerView.Adapter<FullScreenMediaAdapter.FullScreenPictureViewHolder> {
    private final String[] pictureUris; //图片Uri字符串数组

    public static class FullScreenPictureViewHolder extends RecyclerView.ViewHolder {
        ViewHolderFullScreenMediaBinding binding;

        public FullScreenPictureViewHolder(@NonNull ViewHolderFullScreenMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 全屏媒体适配器构造方法
     *
     * @param mediaUris 媒体 Uri 字符串数组
     */
    public FullScreenMediaAdapter(String[] mediaUris) {
        this.pictureUris = mediaUris;
    }

    @NonNull
    @Override
    public FullScreenPictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderFullScreenMediaBinding binding = ViewHolderFullScreenMediaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FullScreenPictureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenPictureViewHolder holder, int position) {
        Uri mediaUri = Uri.parse(pictureUris[position]);
        Context context = holder.itemView.getContext();
        Glide.with(context)
                .load(mediaUri)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.binding.photoView);
    }

    @Override
    public int getItemCount() {
        return pictureUris.length;
    }
}
