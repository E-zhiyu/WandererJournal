package com.wanderer.journal.ui.pages.main.settings.sub.media;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.TransitionName;
import com.wanderer.journal.databinding.ActivityMediaListBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.helpers.file.MediaHelper;
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity;

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

        //媒体列表
        MediaListAdapter adapter = new MediaListAdapter(
                this,
                (pos, uriList, view) -> {
                    String[] uriStrArray = uriList.stream()
                            .map(Uri::toString)
                            .toArray(String[]::new);

                    //实例化 Intent 并放入数据
                    Intent skip2FullScreen = new Intent(this, FullScreenMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArray(KeyStrings.FILE_URIS.v(), uriStrArray);
                    bundle.putInt(KeyStrings.VIEW_HOLDER_POSITION.v(), pos);
                    skip2FullScreen.putExtras(bundle);

                    //添加动画并启动
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            view,
                            TransitionName.FULLSCREEN_MEDIA.getS()
                    );
                    startActivity(skip2FullScreen, options.toBundle());
                }
        );
        binding.recycler.setAdapter(adapter);
        final int SPAN_COUNT = AppearanceHelper.getScreenHeight(this) > AppearanceHelper.getScreenWidth(this) ?
                4 : 9;
        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT);
        binding.recycler.setLayoutManager(layoutManager);

        //读取文件数据并加载列表
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