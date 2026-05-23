package com.wanderer.journal.ui.pages.media;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.databinding.ActivityMediaBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.file.FileHelper;

import java.io.File;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FullScreenMediaActivity extends AppCompatActivity {
    private ActivityMediaBinding binding;   //绑定的 XML 布局
    private String[] mediaUriStrings;       //媒体 Uri 字符串数组
    private int startIndex;                 //媒体起始下标
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        receiveIntent();
        initViews();

        //设置返回监听
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                supportFinishAfterTransition();
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    /**
     * 接收 Intent 中传递的数据
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        mediaUriStrings = intent.getStringArrayExtra(KeyStrings.FILE_URIS.getS());
        startIndex = intent.getIntExtra(KeyStrings.VIEW_HOLDER_POSITION.getS(), 0);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        FullScreenMediaAdapter adapter = new FullScreenMediaAdapter(mediaUriStrings);
        binding.viewPager2.setAdapter(adapter);

        //设置初始位置
        binding.viewPager2.setCurrentItem(startIndex, false);

        //保存媒体
        binding.saveMediaBtn.setOnClickListener(v -> savePicture());
        AppearanceAnimationHelper.attachMorphAnimation(binding.saveMediaBtn);

        //分享媒体
        binding.shareMediaBtn.setOnClickListener(v -> sharePicture());
        AppearanceAnimationHelper.attachMorphAnimation(binding.shareMediaBtn);
    }

    /**
     * 保存图片到系统相册
     */
    private void savePicture() {
        int currentIndex = binding.viewPager2.getCurrentItem();
        Uri currentUri = Uri.parse(mediaUriStrings[currentIndex]);
        disposable.add(FileHelper.saveMediaToGalleryObservable(this, currentUri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        uri -> Toast.makeText(this, "媒体文件已保存至相册", Toast.LENGTH_SHORT).show(),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 分享图片
     */
    private void sharePicture() {
        int currentIndex = binding.viewPager2.getCurrentItem();
        Uri currentUri = Uri.parse(mediaUriStrings[currentIndex]);
        File pictureFile = new File(Objects.requireNonNull(currentUri.getPath()));
        String extension = MimeTypeMap.getFileExtensionFromUrl(currentUri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        disposable.add(FileHelper.shareFileCompletable(this, pictureFile, mimeType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Toast.makeText(FullScreenMediaActivity.this, "正在分享图片……", Toast.LENGTH_SHORT).show(),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }
}