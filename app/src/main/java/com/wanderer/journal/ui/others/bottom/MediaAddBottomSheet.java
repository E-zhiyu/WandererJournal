package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.databinding.BottomSheetPictureAddOptionBinding;

public class MediaAddBottomSheet extends BaseBottomSheetDialogFragment {
    private final TakePictureListener takePictureListener;
    private final OpenAlbumListener openAlbumListener;

    public interface TakePictureListener {
        void takePicture();
    }

    public interface OpenAlbumListener {
        void openAlbum();
    }

    public MediaAddBottomSheet(
            TakePictureListener takePictureListener,
            OpenAlbumListener openAlbumListener
    ) {
        this.takePictureListener = takePictureListener;
        this.openAlbumListener = openAlbumListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //绑定的XML视图
        BottomSheetPictureAddOptionBinding binding = BottomSheetPictureAddOptionBinding.inflate(inflater, container, false);

        binding.addViaCamera.setOnClickListener(v -> {
            takePictureListener.takePicture();
            dismiss();
        });

        binding.addViaAlbum.setOnClickListener(v -> {
            openAlbumListener.openAlbum();
            dismiss();
        });

        return binding.getRoot();
    }
}
