package com.wanderer.journal.ui.pages.main.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wanderer.journal.R;
import com.wanderer.journal.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;    //绑定的XML布局

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {

    }
}