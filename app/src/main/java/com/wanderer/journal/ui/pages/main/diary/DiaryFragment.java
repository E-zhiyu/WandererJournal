package com.wanderer.journal.ui.pages.main.diary;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wanderer.journal.databinding.FragmentDiaryBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

public class DiaryFragment extends Fragment {
    private FragmentDiaryBinding binding;   //绑定的XML布局

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiaryBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //添加FAB
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFab);
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.diaryRecycler, binding.addFab);
    }
}