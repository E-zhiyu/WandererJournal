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
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.auxiliary.enums.bottom_options.DiaryShareOption;
import com.wanderer.journal.databinding.BottomSheetDiaryShareBinding;
import com.wanderer.journal.ui.others.viewmodel.DiaryShareViewModel;

public class DiaryShareBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetDiaryShareBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetDiaryShareBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        DiaryShareViewModel viewModel = new ViewModelProvider(requireActivity()).get(DiaryShareViewModel.class);

        //分享为图片
        binding.shareAsImage.setOnClickListener(view -> {
            viewModel.setClickEvent(DiaryShareOption.SHARE_AS_IMAGE.ordinal());
            dismiss();
        });

        //保存到相册
        binding.saveToAlbum.setOnClickListener(view -> {
            viewModel.setClickEvent(DiaryShareOption.SAVE_TO_ALBUM.ordinal());
            dismiss();
        });
    }
}
