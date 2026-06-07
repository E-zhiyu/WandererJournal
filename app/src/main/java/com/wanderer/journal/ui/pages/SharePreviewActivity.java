package com.wanderer.journal.ui.pages;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.TagStrings;
import com.wanderer.journal.auxiliary.enums.TransitionName;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphUiModel;
import com.wanderer.journal.databinding.ActivitySharePreviewBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.HtmlHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.helpers.file.FileHelper;
import com.wanderer.journal.helpers.file.MediaHelper;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphListAdapter;
import com.wanderer.journal.ui.others.bottom.DiaryShareBottomSheet;
import com.wanderer.journal.ui.others.dialogs.ProgressDialogBuilder;
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SharePreviewActivity extends AppCompatActivity {
    private ActivitySharePreviewBinding binding;    //绑定的 XML 布局
    private long[] sharedParagraphIds;              //分享的段落 ID 数组
    private final CompositeDisposable disposable = new CompositeDisposable();   //多线程任务订阅队列
    private ParagraphListAdapter adapter;           //段落列表适配器
    private final HtmlHelper htmlHelper = new HtmlHelper(); // HTML 网页生成器

    private interface ImageGeneratedListener {
        /**
         * 图片生成完毕回调
         *
         * @param imageFile 生成的图片文件
         */
        void onImageGenerated(File imageFile);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySharePreviewBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //列表视图
            binding.previewRecycler.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        receiveIntent();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //取消图片生成
        htmlHelper.cancelGenerateImage();

        //清空临时媒体目录
        FileHelper.clearMediaTempDir(this);

        binding = null;
        disposable.dispose();
    }

    /**
     * 接收 Intent 中传递的数据
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        //分享的段落 ID 数组
        sharedParagraphIds = bundle.getLongArray(KeyStrings.SHARED_PARAGRAPH_ID.getS());
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //导出按钮
        binding.shareBtn.setOnClickListener(view -> {
            DiaryShareBottomSheet bottomSheet = new DiaryShareBottomSheet(
                    new DiaryShareBottomSheet.OptionListener() {
                        @Override
                        public void onShareAsImage() {
                            generateImageAndDoAction(imageFile -> {
                                //根据文件后缀获取 MimeType (例如 image/jpeg, video/mp4)
                                Uri uri = Uri.fromFile(imageFile);
                                String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());

                                //调用系统分享 API
                                disposable.add(FileHelper.shareFileCompletable(
                                                SharePreviewActivity.this,
                                                imageFile,
                                                mimeType
                                        )
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                () -> Toast.makeText(SharePreviewActivity.this, "正在分享图片……", Toast.LENGTH_SHORT).show(),
                                                e -> ExceptionHelper.showExceptionDialog(SharePreviewActivity.this, e)
                                        ));
                            });
                        }

                        @Override
                        public void onSaveToAlbum() {
                            generateImageAndDoAction(imageFile -> {
                                //保存至相册
                                Uri currentUri = Uri.fromFile(imageFile);
                                disposable.add(MediaHelper.saveMediaToGalleryObservable(SharePreviewActivity.this, currentUri)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                uri -> Toast.makeText(
                                                        SharePreviewActivity.this,
                                                        "图片已保存至相册", Toast.LENGTH_SHORT
                                                ).show(),
                                                e -> ExceptionHelper.showExceptionDialog(
                                                        SharePreviewActivity.this,
                                                        e
                                                )
                                        )
                                );
                            });
                        }
                    }
            );
            bottomSheet.show(getSupportFragmentManager(), TagStrings.DIARY_SHARE_BOTTOM_SHEET.getTag());
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.shareBtn);
        ViewEdgeHelper.setMarginToNavigation(binding.shareBtn, this);

        //段落列表
        initRecycler();
    }

    /**
     * 初始化段落列表
     */
    private void initRecycler() {
        //绑定适配器
        adapter = new ParagraphListAdapter(
                (position, mediaView, mediaList) -> {
                    String[] uriStrArray = mediaList.stream()
                            .map(MediaEntity::getFileUri)
                            .map(Uri::toString)
                            .toArray(String[]::new);

                    //实例化 Intent 并放入数据
                    Intent skip2FullScreen = new Intent(this, FullScreenMediaActivity.class);
                    skip2FullScreen.putExtra(KeyStrings.FILE_URIS.getS(), uriStrArray);
                    skip2FullScreen.putExtra(KeyStrings.VIEW_HOLDER_POSITION.getS(), position);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            mediaView,
                            TransitionName.PARAGRAPH_MEDIA.getS()
                    );

                    startActivity(skip2FullScreen, options.toBundle());
                }
        );
        binding.previewRecycler.setAdapter(adapter);

        //获取数据源
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(db.paragraphDao().getParagraphSingleById(sharedParagraphIds)
                .flatMap(paragraphEntityModels ->
                        Single.just(insertDateSeparator(paragraphEntityModels))
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        paragraphList -> {
                            if (paragraphList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }

                            adapter.submitList(paragraphList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 将{@link ParagraphEntityModel}列表转换为{@link ParagraphUiModel}列表并插入日期分隔符
     *
     * @param paragraphList 纯段落列表
     * @return 包含日期分隔符的段落列表
     */
    @NonNull
    private List<ParagraphUiModel> insertDateSeparator(List<ParagraphEntityModel> paragraphList) {
        List<ParagraphUiModel> uiModelList = new ArrayList<>();

        //判空
        if (paragraphList == null || paragraphList.isEmpty()) {
            return uiModelList;
        }

        //第一个位置插入日期分隔符
        uiModelList.add(new ParagraphUiModel.Separator(
                paragraphList.get(0).getParagraph().getCreateTime().toLocalDate())
        );

        //遍历插入段落和剩下的分隔符
        for (int i = 0; i < paragraphList.size(); i++) {
            //获取 ParagraphEntityModel 实例
            ParagraphEntityModel current = paragraphList.get(i);
            ParagraphEntityModel next = i >= paragraphList.size() - 1 ? null : paragraphList.get(i + 1);

            //插入段落数据
            uiModelList.add(new ParagraphUiModel.Item(current));

            //获取日期
            LocalDateTime currentTime = current.getParagraph().getCreateTime();
            LocalDateTime nextTime = next == null ? null : next.getParagraph().getCreateTime();

            //比较日期并判断是否需要插入分隔符
            if (nextTime != null && !currentTime.toLocalDate().isEqual(nextTime.toLocalDate())) {
                uiModelList.add(new ParagraphUiModel.Separator(nextTime.toLocalDate()));
            }
        }

        return uiModelList;
    }

    /**
     * 将列表中的内容格式化为 JSON，供 WebView 加载内容
     *
     * @return 转换得到的 JSON 字符串，结构为[{"type":***,"content":***,"imageUris":***,"time":***},……]，其中 imageUris 和 time 字段只有段落才有
     */
    @NonNull
    private String formatToJson() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");   //日期转换器
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");             //时间转换器

        //获取数据
        List<ParagraphUiModel> uiModelList = adapter.getCurrentList();

        //遍历转换为 JSON
        StringBuilder builder = new StringBuilder("[");
        int i = 0;
        for (ParagraphUiModel model : uiModelList) {
            builder.append("{");
            if (model instanceof ParagraphUiModel.Separator) {
                builder.append("\"type\":\"date\",\"content\":");
                builder.append("\"");
                String date = ((ParagraphUiModel.Separator) model).date.format(dateFormatter);
                builder.append(date);
                builder.append("\"");
            } else if (model instanceof ParagraphUiModel.Item) {
                ParagraphEntityModel entityModel = ((ParagraphUiModel.Item) model).model;
                ParagraphEntity paragraph = entityModel.getParagraph();

                //添加段落内容字段
                builder.append("\"type\":\"text\",\"content\":");
                builder.append("\"");
                String paragraphContent = paragraph.getContent();
                builder.append(paragraphContent);
                builder.append("\"");

                //添加图片字段
                List<MediaEntity> mediaList = entityModel.getMediaList();
                if (!mediaList.isEmpty()) {
                    builder.append(",");
                    builder.append("\"imageUris\":[");

                    int mediaIndex = 0;
                    for (MediaEntity media : mediaList) {
                        builder.append("\"");
                        builder.append(media.getFileUri().toString());
                        builder.append("\"");

                        if (mediaIndex < mediaList.size() - 1) {
                            builder.append(",");
                        }
                        mediaIndex++;
                    }
                    builder.append("]");
                }

                //添加时间字段
                String time = paragraph.getCreateTime().format(timeFormatter);
                builder.append(",");
                builder.append("\"time\":");
                builder.append("\"");
                builder.append(time);
                builder.append("\"");
            }

            builder.append("}");
            if (i < uiModelList.size() - 1) {
                builder.append(",");
            }
            i++;
        }
        builder.append("]");

        return builder.toString();
    }

    /**
     * 生成图片并执行指定的操作
     *
     * @param listener 生成图片后需要执行的操作
     */
    private void generateImageAndDoAction(ImageGeneratedListener listener) {
        ProgressDialogBuilder builder = new ProgressDialogBuilder(this,"分享日记","正在生成图片……");
        builder.setNegativeButton("取消",(dialogInterface, i) -> {
            disposable.clear();
            htmlHelper.cancelGenerateImage();
        });
        AlertDialog dialog = builder.show();

        String json = formatToJson();
        htmlHelper.generateAndShare(
                json,
                SharePreviewActivity.this,
                new HtmlHelper.ImageGenerateListener() {
                    @Override
                    public void onLoadingStart() {
                        Log.d(LogTags.SHARE_PREVIEW_ACTIVITY.n(), "开始加载WebView");
                    }

                    @Override
                    public void onReady(File imageFile) {
                        Log.i(LogTags.SHARE_PREVIEW_ACTIVITY.n(), "图片已生成，Path:" + imageFile);
                        dialog.dismiss();
                        listener.onImageGenerated(imageFile);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LogTags.SHARE_PREVIEW_ACTIVITY.n(), message);
                        dialog.dismiss();
                        Toast.makeText(SharePreviewActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}