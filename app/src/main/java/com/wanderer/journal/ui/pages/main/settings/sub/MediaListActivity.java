package com.wanderer.journal.ui.pages.main.settings.sub;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.databinding.ActivityMediaListBinding;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;

public class MediaListActivity extends AppCompatActivity {
    private ActivityMediaListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaListBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.recycler.setPadding(
                    AppearanceHelper.dpToPx(this, 10),
                    0,
                    AppearanceHelper.dpToPx(this, 10),
                    systemBars.bottom
            );
            return insets;
        });

        initViews();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //TODO:完成列表布局
    }
}