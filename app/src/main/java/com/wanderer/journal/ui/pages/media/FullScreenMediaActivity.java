package com.wanderer.journal.ui.pages.media;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.databinding.ActivityMediaBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.helpers.file.FileHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        receiveIntent();
        initViews();
        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                //动画播放完毕后隐藏状态栏
                hideSystemUI();
            }
        });

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
        //翻页视图
        FullScreenMediaAdapter adapter = new FullScreenMediaAdapter(mediaUriStrings);
        binding.viewPager2.setAdapter(adapter);
        List<Uri> mediaUriList = Arrays.stream(mediaUriStrings)
                .map(Uri::parse)
                .collect(Collectors.toList());
        adapter.submitList(mediaUriList);

        //设置初始位置
        binding.viewPager2.setCurrentItem(startIndex, false);

        //保存媒体
        binding.saveMediaBtn.setOnClickListener(v -> savePicture());
        AppearanceAnimationHelper.attachMorphAnimation(binding.saveMediaBtn);

        //分享媒体
        binding.shareMediaBtn.setOnClickListener(v -> sharePicture());
        AppearanceAnimationHelper.attachMorphAnimation(binding.shareMediaBtn);

        //按钮分组设置到底部的边距
        ViewEdgeHelper.setMarginToNavigation(binding.btnGroup, 30, this);
    }

    /**
     * 设置全屏，隐藏状态栏
     */
    private void hideSystemUI() {
        Window window = getWindow();

        // 强制让临时滑出的系统栏图标和文字变成白色，确保在黑底大图上清晰可见
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        insetsController.setAppearanceLightStatusBars(false);
        insetsController.setAppearanceLightNavigationBars(false);

        //设置导航栏和状态栏为透明
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // 确保当前 Window 铺满全屏
        WindowCompat.setDecorFitsSystemWindows(window, false);

        //根据不同 API 使用不同的方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android 11 (API 30) 及以上的全新标准写法
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                //隐藏状态栏
                controller.hide(WindowInsets.Type.statusBars());

                //设置状态栏行为：用户从边缘滑动手势时，临时半透明拉出系统栏，不影响应用本身的布局
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            //针对 Android 10 及以下老系统的兼容写法
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // 隐藏状态栏
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 粘性沉浸
            decorView.setSystemUiVisibility(uiOptions);
        }
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