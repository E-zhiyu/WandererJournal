package com.wanderer.journal.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.databinding.ViewHolderMediaBinding;

public class MediaAdapter extends ListAdapter<MediaEntity, MediaAdapter.MediaViewHolder> {
    private final static DiffUtil.ItemCallback<MediaEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaEntity oldItem, @NonNull MediaEntity newItem) {
            return oldItem.getMediaId() == newItem.getMediaId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaEntity oldItem, @NonNull MediaEntity newItem) {
            return oldItem.getFileUri().equals(newItem.getFileUri());
        }
    };

    public MediaAdapter() {
        super(ITEM_CALLBACK);
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
