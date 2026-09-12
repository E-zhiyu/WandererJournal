package com.wanderer.journal.ui.pages.media;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.auxiliary.classes.file.MediaDetail;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.databinding.ActivityMediaInfoBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.file.MediaHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MediaInfoActivity extends AppCompatActivity {
    private ActivityMediaInfoBinding binding;   //绑定的 XML 布局
    @Nullable
    private Bundle initBundle;                  //包含初始化数据的数据包
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaInfoBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.linearLayout.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initBundle = getIntent().getExtras();
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

        //获取文件信息
        String uriStr = initBundle != null ? initBundle.getString(KeyStrings.FILE_URIS.v()) : "";
        disposable.add(Single.fromCallable(() -> {
                            Uri uri = Uri.parse(uriStr);
                            MediaDetail mediaDetail = MediaHelper.getMediaDetail(this, uri);
                            return Single.just(mediaDetail);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                detail -> {
                                    //TODO:完成信息显示逻辑
                                },
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
        );
    }
}