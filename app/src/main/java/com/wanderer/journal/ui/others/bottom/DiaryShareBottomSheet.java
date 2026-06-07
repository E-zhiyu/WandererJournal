package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.databinding.BottomSheetDiaryShareBinding;

public class DiaryShareBottomSheet extends BaseBottomSheetDialogFragment {
    private final OptionListener listener;
    private BottomSheetDiaryShareBinding binding;

    public interface OptionListener {
        void onShareAsImage();

        void onSaveToAlbum();
    }

    public DiaryShareBottomSheet(OptionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //绑定的XML视图
        binding = BottomSheetDiaryShareBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //分享为图片
        binding.shareAsImage.setOnClickListener(view -> {
            listener.onShareAsImage();
            dismiss();
        });

        //保存到相册
        binding.saveToAlbum.setOnClickListener(view -> {
            listener.onSaveToAlbum();
            dismiss();
        });
    }
}
