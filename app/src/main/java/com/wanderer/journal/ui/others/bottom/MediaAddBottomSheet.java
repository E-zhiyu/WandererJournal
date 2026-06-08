package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        BottomSheetPictureAddOptionBinding binding = BottomSheetPictureAddOptionBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

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
