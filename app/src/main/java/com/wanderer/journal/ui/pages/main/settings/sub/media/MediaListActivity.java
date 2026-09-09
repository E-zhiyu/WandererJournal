package com.wanderer.journal.ui.pages.main.settings.sub.media;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.wanderer.journal.databinding.ActivityMediaListBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.helpers.file.MediaHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MediaListActivity extends AppCompatActivity {
    private ActivityMediaListBinding binding;
    private final CompositeDisposable disposable = new CompositeDisposable();

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
                    AppearanceHelper.dpToPx(this, 10),
                    AppearanceHelper.dpToPx(this, 10),
                    systemBars.bottom
            );
            return insets;
        });

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //TODO:媒体列表
        MediaListAdapter adapter = new MediaListAdapter(
                this,
                (entity, anchor) -> {
                    //TODO:完成点击监听
                }
        );
        binding.recycler.setAdapter(adapter);
        final int SPAN_COUNT = AppearanceHelper.getScreenHeight(this) > AppearanceHelper.getScreenWidth(this) ?
                4 : 9;
        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT);
        binding.recycler.setLayoutManager(layoutManager);

        //读取文件数据
        disposable.add(MediaHelper.readMediaDir(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        infoList -> {
                            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, infoList.isEmpty());
                            adapter.submitList(infoList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //刷新按钮
        binding.refreshBtn.setOnClickListener(view -> disposable.add(MediaHelper.readMediaDir(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        infoList -> {
                            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, infoList.isEmpty());
                            adapter.submitList(infoList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        ));
        AppearanceHelper.attachMorphAnimation(binding.refreshBtn);
    }
}