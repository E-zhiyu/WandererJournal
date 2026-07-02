package com.wanderer.journal.ui.pages.life_note;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.databinding.ActivityLifeNoteListBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;

public class LifeNoteListActivity extends AppCompatActivity {
    private ActivityLifeNoteListBinding binding;   //绑定的 XML 布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLifeNoteListBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initViews();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            //TODO:跳转至输入界面
        });
        AppearanceHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离
        AppearanceHelper.attachMorphAnimation(binding.addFab);

        //TODO:初始化搜索和列表
    }
}