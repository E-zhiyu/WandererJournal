package com.wanderer.journal.ui.pages;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.RoleShower;
import com.wanderer.journal.auxiliary.enums.TransitionName;
import com.wanderer.journal.auxiliary.interfaces.PagingRecyclerScrollListener;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.ParagraphEntityModel;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.data.save.db.services.MediaService;
import com.wanderer.journal.data.save.db.services.ParagraphService;
import com.wanderer.journal.data.save.preference.DraftPreference;
import com.wanderer.journal.databinding.ActivityWriteBinding;
import com.wanderer.journal.auxiliary.enums.DirectoryPaths;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.TagStrings;
import com.wanderer.journal.databinding.ViewHolderDateSeparatorBinding;
import com.wanderer.journal.helpers.BackPressedCallbackHelper;
import com.wanderer.journal.helpers.ImmHelper;
import com.wanderer.journal.helpers.PermissionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.KeyboardAttachmentHelper;
import com.wanderer.journal.helpers.text.ParagraphTextConverter;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.helpers.file.FileHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.others.adapters.MediaAdapter;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphPagingAdapter;
import com.wanderer.journal.ui.others.bottom.RoleSelectBottomSheet;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.wanderer.journal.ui.others.viewmodel.ParagraphViewModel;
import com.wanderer.journal.ui.others.bottom.MediaAddBottomSheet;
import com.wanderer.journal.ui.others.bottom.EmotionTagSelectBottomSheet;
import com.wanderer.journal.ui.others.dialogs.ProgressDialogBuilder;
import com.wanderer.journal.ui.others.selections.media.MediaIdKeyProvider;
import com.wanderer.journal.ui.others.selections.media.MediaLookup;
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class WriteActivity extends AppCompatActivity {
    private ActivityWriteBinding binding;                   //绑定的XML布局
    private ParagraphEntity modifyingParagraph = null;      //正在编辑的段落
    private BackPressedCallbackHelper backHelper;           //返回逻辑管理器
    private BackPressedCallbackHelper.BackHandler selectionBackHandler; //媒体多选返回处理器
    private BackPressedCallbackHelper.BackHandler mediaBackHandler;     //媒体显示返回处理器
    private BackPressedCallbackHelper.BackHandler editBackHandler;      //内容编辑返回处理器
    private LocalDate diaryDate = LocalDate.now();          //父日记的日期
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表
    private boolean needScrollToBottom = false;             //是否需要在段落刷新的时候滚动到底部
    private ActivityResultLauncher<PickVisualMediaRequest> albumLauncher;   //相册图片选择启动器
    private ActivityResultLauncher<Uri> takePictureLauncher;    //调用系统相机的启动器
    private ActivityResultLauncher<String> permissionLauncher;  //权限申请启动器
    private Uri cameraFileUri = null;                       //相机拍照得到的临时图片 File 类型的 Uri
    private MediaAdapter mediaAdapter;                      //媒体文件列表适配器
    private SelectionTracker<Long> selectionTracker;        //图片列表选择追踪器
    private final Handler draftSavingHandler = new Handler(Looper.getMainLooper()); //保存草稿的执行器
    private final Runnable draftSavingRunnable = this::saveDraft;   //保存草稿的 Runnable 实例
    private static final int DRAFT_SAVING_DELAY = 3000;     //3s没有修改文本则保存草稿
    private KeyboardAttachmentHelper keyboardAttachmentHelper;  //失去焦点时的键盘监听器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //底部输入框卡片
            binding.contentInputLayout.setPadding(
                    ViewEdgeHelper.dpToPx(this, 20),
                    ViewEdgeHelper.dpToPx(this, 10),
                    ViewEdgeHelper.dpToPx(this, 10),
                    systemBars.bottom
            );

            //内容 RecyclerView 额外增加5dp的底部内边距
            binding.contentRecycler.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    imeInsets.bottom + ViewEdgeHelper.dpToPx(WriteActivity.this, 5)
            );

            return insets;
        });

        //设置键盘动画监听器
        ViewCompat.setWindowInsetsAnimationCallback(binding.getRoot(), new WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
        ) {
            @NonNull
            @Override
            public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                // 获取当前帧键盘（IME）和系统栏的高度
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // 计算键盘弹起的高度（减去底部导航栏的高度，防止重复偏移）
                int keyboardHeight = Math.max(0, imeInsets.bottom - systemBars.bottom);
                binding.contentInputCard.setTranslationY(-keyboardHeight);
                binding.contentEditCard.setTranslationY(-keyboardHeight);
                binding.mediaCard.setTranslationY(-keyboardHeight);
                binding.emptyText.setTranslationY(-keyboardHeight * 2 / 5f);

                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                super.onEnd(animation);
                // 动画结束时，如果需要将 Translation 转换为永久的 Padding，可以在这里处理
            }
        });

        receiveIntent();
        initViews();
        initLaunchers();
        initOnBackPressedHandlers();

        //实例化键盘监听器
        keyboardAttachmentHelper = new KeyboardAttachmentHelper(binding.getRoot());

        //第一次加载界面时显示草稿恢复对话框
        if (savedInstanceState == null) {
            String draft = DraftPreference.getDraft(this);
            if (!draft.trim().isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("草稿恢复")
                        .setMessage("您有一篇段落草稿未发送，是否恢复该草稿？")
                        .setPositiveButton("恢复", (dialogInterface, i) -> {
                            CharSequence richText = ParagraphTextConverter.hierarchic(this, draft);
                            binding.contentTextInput.setText(richText);
                            binding.contentTextInput.setSelection(richText.length());
                            ImmHelper.showImm(binding.contentTextInput);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (keyboardAttachmentHelper != null) {
            keyboardAttachmentHelper.startLegacyTracking(
                    (currentHeight, previousHeight) -> {
                        if (hasWindowFocus()) return;

                        int moveDistance = Math.max(0, currentHeight - binding.contentInputLayout.getPaddingBottom());
                        binding.contentInputCard
                                .animate()
                                .translationY(-moveDistance)
                                .setDuration(250)
                                .start();
                        binding.contentEditCard
                                .animate()
                                .translationY(-moveDistance)
                                .setDuration(250)
                                .start();
                        binding.mediaCard
                                .animate()
                                .translationY(-moveDistance)
                                .setDuration(250)
                                .start();
                        binding.emptyText
                                .animate()
                                .translationY(-moveDistance / 2f)
                                .setDuration(250)
                                .start();

                        //内容 RecyclerView 额外增加5dp的底部内边距
                        binding.contentRecycler.setPadding(
                                0,
                                0,
                                0,
                                currentHeight + ViewEdgeHelper.dpToPx(WriteActivity.this, 5)
                        );
                    }
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (keyboardAttachmentHelper != null) {
            keyboardAttachmentHelper.stopTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //清空草稿保存任务并直接保存一次草稿
        draftSavingHandler.removeCallbacks(draftSavingRunnable);
        saveDraft();

        binding = null;
        disposable.dispose();

        //清空临时媒体目录
        FileHelper.clearMediaTempDir(this);
    }

    /**
     * 获取由父界面传递的参数
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        //初始化日期数据
        try {
            String date = bundle.getString(KeyStrings.WRITE_DIARY_DATE.getS());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            diaryDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            Log.e(LogTags.WRITE_ACTIVITY.n(), "无法读取传递的日期数据，已默认设置为当前日期");
            diaryDate = LocalDate.now();
        }

        //待编辑的
        long modifyParagraphId = bundle.getLong(KeyStrings.WRITE_MODIFY_PARAGRAPH_ID.getS());
        ParagraphDao paragraphDao = DiaryDatabase.getInstance(this).paragraphDao();
        disposable.add(paragraphDao.getParagraphOptionalSingleById(modifyParagraphId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        paragraphOptional -> {
                            if (paragraphOptional.isEmpty()) {
                                return;
                            }

                            //启用编辑模式
                            ParagraphEntityModel model = paragraphOptional.get();
                            ParagraphEntity paragraph = model.getParagraph();
                            List<MediaEntity> mediaList = model.getMediaList();
                            setEditMode(true, paragraph, mediaList);
                        }
                )
        );
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //初始化RecyclerView
        initParagraphRecycler();

        //媒体添加按钮
        binding.mediaAddBtn.setOnClickListener(view -> {
            MediaAddBottomSheet bottomSheet = new MediaAddBottomSheet(
                    () -> {
                        if (PermissionHelper.isRuntimePermissionGranted(
                                Manifest.permission.CAMERA,
                                this
                        )) {
                            launchSystemCamera();
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    },
                    () -> albumLauncher.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
                    )
            );
            bottomSheet.show(getSupportFragmentManager(), TagStrings.MEDIA_ADD_BOTTOM_SHEET.getTag());
        });

        //媒体删除按钮
        binding.mediaDeleteBtn.setOnClickListener(view -> {
                    //获取需要删除的媒体
                    List<MediaEntity> mediaListToBeDeleted = new ArrayList<>();
                    for (long id : selectionTracker.getSelection()) {
                        MediaEntity media = mediaAdapter.getItemById(id);
                        if (media != null) {
                            mediaListToBeDeleted.add(media);
                        }
                    }

                    //显示对话框
                    String message = String.format(
                            Locale.getDefault(),
                            "即将删除%d个媒体文件，此操作无法撤销，确认继续吗？",
                            mediaListToBeDeleted.size()
                    );
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("删除媒体")
                            .setMessage(message)
                            .setPositiveButton("确定", (dialogInterface, i) -> {
                                //退出多选
                                selectionTracker.clearSelection();

                                //多线程删除媒体
                                disposable.add(MediaService.deleteMedia(mediaListToBeDeleted, this)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                () -> {
                                                    //显示提示
                                                    String tip = String.format(
                                                            Locale.getDefault(),
                                                            "删除了%d个媒体",
                                                            mediaListToBeDeleted.size()
                                                    );
                                                    Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();

                                                    //更新适配器列表
                                                    List<MediaEntity> currentMediaList = new ArrayList<>(mediaAdapter.getCurrentList());
                                                    currentMediaList.removeAll(mediaListToBeDeleted);
                                                    mediaAdapter.submitList(currentMediaList);

                                                    //没有媒体时隐藏（延迟270ms）防止因动画竞争导致删除按钮和媒体添加重叠
                                                    if (currentMediaList.isEmpty()) {
                                                        new Handler(Looper.getMainLooper()).postDelayed(
                                                                () -> setMediaRecyclerVisible(false),
                                                                270
                                                        );
                                                    }
                                                },
                                                e -> ExceptionHelper.showExceptionDialog(this, e)
                                        )
                                );
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
        );

        //媒体Recycler
        initMediaRecycler();

        //发送按钮
        binding.sendBtn.setOnClickListener(view -> {
            //获取输入内容
            String content = ParagraphTextConverter.flatten(binding.contentTextInput.getEditableText());
            if (content.trim().isEmpty()) {
                return;
            }

            //显示进度条对话框
            ProgressDialogBuilder dialogBuilder = new ProgressDialogBuilder(
                    this,
                    "保存媒体文件",
                    "正在移动媒体文件……"
            );
            AlertDialog dialog = dialogBuilder
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                        disposable.clear();
                        Toast.makeText(this, "已取消媒体文件保存", Toast.LENGTH_SHORT).show();
                    })
                    .create();

            //获取新添加的媒体的 Uri
            List<Uri> newMediaUriList = mediaAdapter.getCurrentList().stream()
                    .filter(media -> media.getMediaId() == 0)   //只需要新添加的
                    .map(MediaEntity::getFileUri)
                    .collect(Collectors.toList());

            //如果有新媒体，则显示对话框
            if (!newMediaUriList.isEmpty()) {
                dialog.show();
            }

            //创建移动任务，并逐个返回移动成功的 File 型 Uri
            Observable<Uri> moveTask = Observable.create(emitter -> {
                File targetDir = DirectoryPaths.MEDIA.getDir(this);

                for (Uri originUri : newMediaUriList) {
                    File originFile = new File(Objects.requireNonNull(originUri.getPath()));
                    File movedFile = FileHelper.moveFile(originFile, targetDir);
                    emitter.onNext(Uri.fromFile(movedFile));    //不论是否成功都返回
                }

                emitter.onComplete();
            });

            //多线程执行任务并调用段落添加/更新方法
            List<Uri> resultList = new ArrayList<>();   //移动结果列表（可能包含 null）
            disposable.add(moveTask
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            uri -> {
                                resultList.add(uri);
                                dialogBuilder.updateProgress(resultList.size(), newMediaUriList.size(), "正在移动媒体文件……");
                            },
                            e -> {
                                ExceptionHelper.showExceptionDialog(this, e);
                                dialog.dismiss();
                            },
                            () -> {
                                dialog.dismiss();

                                //排除移动失败的文件
                                List<Uri> succeedFileUriList = resultList.stream()
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());

                                //调用数据写入方法
                                if (modifyingParagraph == null) {
                                    addParagraph(content, succeedFileUriList);
                                } else {
                                    updateParagraphContent(content, modifyingParagraph, succeedFileUriList);
                                    setEditMode(false, null, null);
                                }
                            }
                    )
            );

            //清理图片列表并隐藏
            mediaAdapter.submitList(new ArrayList<>());
            setMediaRecyclerVisible(false);
            backHelper.unregisterHandler(mediaBackHandler);
        });

        //文本输入框
        binding.contentTextInput.addTextChangedListener(new TextWatcher() {
            private boolean isChanging = false; // 防止死循环

            @Override
            public void afterTextChanged(Editable s) {
                if (isChanging) return;

                if (s != null && s.toString().contains("\n")) {
                    isChanging = true;

                    //将所有换行符替换为空字符串
                    int selectionStart = binding.contentTextInput.getSelectionStart();
                    int replacedCount = TextHelper.getKeywordCount(
                            s.toString(),
                            selectionStart,
                            "\n"
                    ); //计算换行符的数量（只计算在光标前面的）
                    String cleanString = s.toString().replace("\n", "");

                    //重新设置文本并移动光标
                    binding.contentTextInput.setText(cleanString);
                    binding.contentTextInput.setSelection(selectionStart - replacedCount);

                    isChanging = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //处理草稿保存任务
                draftSavingHandler.removeCallbacks(draftSavingRunnable);
                draftSavingHandler.postDelayed(draftSavingRunnable, DRAFT_SAVING_DELAY);

                //当输入“@”时弹出角色选择对话框
                if (i2 == 1 && charSequence.charAt(i) == '@') {
                    RoleSelectBottomSheet bottomSheet = new RoleSelectBottomSheet((name, roleId) -> {
                        //生成包装好的富文本标签块
                        String display = "@" + name;
                        String value = String.valueOf(roleId);
                        SpannableString roleTag = TextHelper.createTextTag(
                                WriteActivity.this,
                                display,
                                KeyStrings.ROLE_ID.getS(),
                                value
                        );

                        //把刚才打出"@"替换成高亮的标签块
                        Editable editable = binding.contentTextInput.getText();
                        int selectionStart = binding.contentTextInput.getSelectionStart();
                        if (editable != null) {
                            editable.replace(selectionStart - 1, selectionStart, roleTag);
                        }

                        //让光标跳到这个标签块的后面
                        binding.contentTextInput.setSelection(selectionStart - 1 + roleTag.length());
                    });
                    bottomSheet.show(getSupportFragmentManager(), TagStrings.ROLE_SELECT_BOTTOM_SHEET.getTag());
                }
            }
        });

        //内容编辑关闭按钮
        binding.modifyCloseBtn.setOnClickListener(view -> setEditMode(false, null, null));
    }

    /**
     * 初始化RecyclerView
     */
    private void initParagraphRecycler() {
        //设置适配器
        ParagraphPagingAdapter adapter = new ParagraphPagingAdapter(
                (dataModel, view) -> {
                    ParagraphEntity paragraph = dataModel.getParagraph();

                    //先收起输入法
                    ImmHelper.hideImm(binding.contentTextInput);

                    PopupMenu menu = new PopupMenu(this, view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_modify_content) {
                            setEditMode(true, paragraph, dataModel.getMediaList());

                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            updateParagraphCreateTime(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_modify_emotion) {
                            modifyEmotion(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_copy_paragraph) {
                            TextHelper.copyToClipBoard(this, "日记段落", paragraph.getContent());
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_paragraph) {
                            deleteParagraph(paragraph);
                            return true;
                        } else {
                            return false;
                        }
                    });

                    menu.show();
                },
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
                },
                roleId -> RoleShower.showRoleDetail(this, disposable, roleId)
        );

        //添加粘性头部适配器
        StickyHeaderItemDecoration<ViewHolderDateSeparatorBinding> decoration = new StickyHeaderItemDecoration<>(
                adapter,
                ViewHolderDateSeparatorBinding::inflate,
                (binding, data) -> binding.dateText.setText(data)
        );
        binding.contentRecycler.addItemDecoration(decoration);

        //适配器添加加载状态监听器
        adapter.addLoadStateListener(loadStates -> {
            boolean isNotLoading = loadStates.getRefresh() instanceof LoadState.NotLoading;
            boolean endOfPaginationReached = loadStates.getAppend().getEndOfPaginationReached();

            //滚动到底部
            if (isNotLoading && needScrollToBottom) {
                needScrollToBottom = false;

                int itemCount = adapter.getItemCount();
                if (itemCount > 0) {
                    AppearanceAnimationHelper.scrollPagingRecycler(
                            binding.contentRecycler,
                            (LinearLayoutManager) binding.contentRecycler.getLayoutManager(),
                            adapter,
                            itemCount - 1,
                            63,
                            10,
                            750,
                            new PagingRecyclerScrollListener() {
                                @Override
                                public void onSucceed() {
                                }

                                @Override
                                public void onRetry(int failCount) {
                                }

                                @Override
                                public void onFailed() {
                                }
                            }
                    );
                }
            }

            if (isNotLoading && endOfPaginationReached) {
                if (adapter.getItemCount() == 0) {
                    binding.emptyText.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyText.setVisibility(View.GONE);
                }
            } else {
                binding.emptyText.setVisibility(View.GONE);
            }
            return Unit.INSTANCE;
        });
        binding.contentRecycler.setAdapter(adapter);

        //监听数据库的响应
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);
        disposable.add(viewModel.getPagingDataFlow(diaryDate, diaryDate.plusDays(1), db)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData ->
                                adapter.submitData(getLifecycle(), pagingData),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 初始化媒体文件列表
     */
    private void initMediaRecycler() {
        //实例化媒体适配器并分配给 RecyclerView
        mediaAdapter = new MediaAdapter(this);
        binding.mediaRecycler.setAdapter(mediaAdapter);

        //构建选择追踪器
        selectionTracker = new SelectionTracker.Builder<>(
                TagStrings.MEDIA_SELECTION.getTag(),
                binding.mediaRecycler,
                new MediaIdKeyProvider(mediaAdapter),
                new MediaLookup(binding.mediaRecycler),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything() // 允许多选
        ).build();
        mediaAdapter.setSelectionTracker(selectionTracker);

        //追踪选择状态
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (selectionTracker.hasSelection()) {
                    new Handler(Looper.getMainLooper()).post(   //必须主线程更新 UI
                            () -> setSelectMode(true)
                    );

                    int size = selectionTracker.getSelection().size();
                    Log.d(LogTags.WRITE_ACTIVITY.n(), "已选择：" + size);
                } else {
                    new Handler(Looper.getMainLooper()).post(   //必须主线程更新 UI
                            () -> setSelectMode(false)
                    );
                    Log.d(LogTags.WRITE_ACTIVITY.n(), "选择已清除");
                }
            }
        });
    }

    /**
     * 初始化启动器
     */
    private void initLaunchers() {
        //从相册选择图片启动器
        albumLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                this::onAlbumPictureUrisReceived
        );

        //系统相机拍照启动器
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        onCameraPictureUriReceived(cameraFileUri);
                    } else {
                        Toast.makeText(this, "拍照已取消", Toast.LENGTH_SHORT).show();

                        //删除刚刚创建的照片文件
                        FileHelper.deleteFile(cameraFileUri, this);
                    }
                }
        );

        //权限申请启动器
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        launchSystemCamera();
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        //弹出解释对话框
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("申请权限")
                                .setMessage("使用相机拍照需要先授予摄像头权限")
                                .setNegativeButton("取消", null)
                                .setPositiveButton(
                                        "确定",
                                        (dialogInterface, i) ->
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                )
                                .show();
                    } else {
                        Toast.makeText(this, "请授予相机权限后再拍照", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * 初始化返回拦截逻辑
     */
    private void initOnBackPressedHandlers() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                backHelper.dispatchBackPressed();
            }
        };
        backHelper = new BackPressedCallbackHelper(backPressedCallback);
        getOnBackPressedDispatcher().addCallback(backPressedCallback);

        //媒体多选处理器
        selectionBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                selectionTracker.clearSelection();

                backHelper.unregisterHandler(this);
                return true;
            }

            @Override
            public int getPriority() {
                return 3;
            }
        };

        //媒体显示处理器
        mediaBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                new MaterialAlertDialogBuilder(WriteActivity.this)
                        .setTitle("清除媒体")
                        .setMessage("确认要舍弃所有未保存的媒体文件吗？")
                        .setPositiveButton("确认", (dialogInterface, i) -> {
                            selectionTracker.clearSelection();          //清除选择
                            mediaAdapter.submitList(new ArrayList<>()); //清空适配器中的 UI
                            setMediaRecyclerVisible(false);          //隐藏图片列表

                            backHelper.unregisterHandler(this);
                        })
                        .setNegativeButton("取消", null)
                        .show();

                return true;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };

        //内容编辑模式处理器
        editBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                setEditMode(false, null, null);
                backHelper.unregisterHandler(this);

                //一并清除媒体列表
                selectionTracker.clearSelection();          //清除选择
                mediaAdapter.submitList(new ArrayList<>()); //清空适配器中的 UI
                setMediaRecyclerVisible(false);          //隐藏图片列表
                backHelper.unregisterHandler(mediaBackHandler);

                return true;
            }

            @Override
            public int getPriority() {
                return 2;
            }
        };
    }

    /**
     * 启动系统相机进行拍照
     */
    private void launchSystemCamera() {
        try {
            //在缓存目录下创建一个临时文件
            File photoFile = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    DirectoryPaths.MEDIA_TEMP.getDir(this)
            );
            cameraFileUri = Uri.fromFile(photoFile);    //保存 File 类型的 Uri

            //通过 FileProvider 获取 Content URI
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            //启动相机
            takePictureLauncher.launch(contentUri);
        } catch (IOException e) {
            Toast.makeText(this, "无法创建相片文件", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "请授予相机权限后再拍照", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 相机拍照成功的回调
     *
     * @param tempPictureUri 系统相机拍照后的临时图片 Uri
     */
    private void onCameraPictureUriReceived(Uri tempPictureUri) {
        //获取现有的列表
        List<MediaEntity> mediaList = new ArrayList<>(mediaAdapter.getCurrentList());

        //展开图片列表视图
        setMediaRecyclerVisible(true);

        //更新列表
        long paragraphId = modifyingParagraph == null ? 0 : modifyingParagraph.getParagraphId();
        mediaList.add(new MediaEntity(paragraphId, tempPictureUri));
        mediaAdapter.submitList(mediaList);
    }

    /**
     * 相册图片的 Uri 接收回调
     *
     * @param uriList 用户选择的相册图片 Uri
     */
    private void onAlbumPictureUrisReceived(@NonNull List<Uri> uriList) {
        //判断是否为空
        if (uriList.isEmpty()) {
            Toast.makeText(this, "未选择媒体文件", Toast.LENGTH_SHORT).show();
            return;
        }

        //显示进度条对话框
        ProgressDialogBuilder builder = new ProgressDialogBuilder(this, "导入媒体", "正在复制媒体文件");
        AlertDialog progressDialog = builder.
                setNegativeButton("取消", (dialogInterface, i) -> {
                    Toast.makeText(this, "已取消媒体导入", Toast.LENGTH_SHORT).show();
                    disposable.clear();
                })
                .show();

        //创建复制任务
        List<MediaEntity> mediaList = new ArrayList<>(mediaAdapter.getCurrentList());
        Observable<Integer> task = Observable.create(emitter -> {
            File mediaDir = DirectoryPaths.MEDIA_TEMP.getDir(this);
            byte[] sharedBuffer = new byte[1024 * 32];  //共享32KB缓存

            //复制文件并保存引用
            for (Uri uri : uriList) {
                File resultFile = null;
                try {
                    resultFile = FileHelper.copyFile(this, uri, mediaDir, sharedBuffer);
                } catch (IOException e) {
                    emitter.onError(e);
                }
                if (resultFile == null) {
                    continue;
                }

                //保存到列表中
                Uri successfulUri = Uri.fromFile(resultFile);
                long paragraphId = modifyingParagraph == null ? 0 : modifyingParagraph.getParagraphId();
                MediaEntity media = new MediaEntity(paragraphId, successfulUri);
                mediaList.add(media);

                //更新进度
                emitter.onNext(mediaList.size());
            }

            //完成任务
            emitter.onComplete();
        });

        //执行复制操作
        disposable.add(task
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        progress -> {
                            builder.setIndeterminate(false);
                            builder.updateProgress(progress, uriList.size(), "正在导入媒体……");
                        },
                        e -> {
                            ExceptionHelper.showExceptionDialog(this, e);
                            progressDialog.dismiss();
                        },
                        () -> {
                            //媒体文件显示在列表中
                            mediaAdapter.submitList(mediaList);
                            setMediaRecyclerVisible(true);   //展开媒体列表

                            progressDialog.dismiss();
                            Toast.makeText(this, "已导入" + mediaList.size() + "个媒体文件", Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    /**
     * 添加新段落
     *
     * @param content      新段落的内容
     * @param newMediaList 新添加到段落的媒体文件列表
     */
    private void addParagraph(String content, List<Uri> newMediaList) {
        //执行写入操作
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(DiaryService.getOrCreateDiaryIdByDate(diaryDate, this)
                .flatMapCompletable(diaryId -> {
                    ParagraphEntity newParagraph = new ParagraphEntity(
                            diaryId,
                            content.trim(),
                            diaryDate.atTime(LocalTime.now())
                    );
                    return ParagraphService.insertParagraphWithMedia(newParagraph, newMediaList, db);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            binding.contentTextInput.setText(null);

                            //将滚动到底部标识改为true
                            needScrollToBottom = true;
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 更新段落内容
     *
     * @param newContent      新内容
     * @param originParagraph 原本的段落实体
     * @param newMediaList    新添加到段落的文件
     */
    private void updateParagraphContent(
            String newContent,
            @NonNull ParagraphEntity originParagraph,
            List<Uri> newMediaList
    ) {
        //生成更新后的段落实例
        ParagraphEntity newParagraph = new ParagraphEntity(
                originParagraph.getParentDiaryId(),
                newContent,
                originParagraph.getCreateTime()
        );
        newParagraph.setParagraphId(originParagraph.getParagraphId());

        //更新段落
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(ParagraphService.updateParagraphWithMedia(newParagraph, newMediaList, db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            Toast.makeText(this, "段落内容修改成功", Toast.LENGTH_SHORT).show();
                            Log.i(LogTags.WRITE_ACTIVITY.n(), "段落内容修改成功");
                        },
                        throwable -> {
                            ExceptionHelper.showExceptionDialog(this, throwable);
                            Log.e(LogTags.WRITE_ACTIVITY.n(), "段落内容修改失败");
                        }
                )
        );
    }

    /**
     * 更新段落创建日期
     *
     * @param paragraph 原来的段落实例
     */
    private void updateParagraphCreateTime(@NonNull ParagraphEntity paragraph) {
        DateTimePickerHelper.selectTime(
                paragraph.getCreateTime(),
                getSupportFragmentManager(),
                timePicker -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    LocalDateTime newDateTime = paragraph.getCreateTime()
                            .withHour(hour)
                            .withMinute(minute);

                    ParagraphEntity newParagraph = new ParagraphEntity(
                            paragraph.getParentDiaryId(),
                            paragraph.getContent(),
                            newDateTime
                    );
                    newParagraph.setParagraphId(paragraph.getParagraphId());

                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphDao paragraphDao = db.paragraphDao();
                    disposable.add(paragraphDao.updateParagraphCompletable(newParagraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        Log.i(LogTags.WRITE_ACTIVITY.n(), "段落创建时间修改成功");
                                        Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.WRITE_ACTIVITY.n(), "段落创建时间修改失败");
                                    }
                            )
                    );
                }
        );
    }

    /**
     * 修改情绪标签
     *
     * @param paragraph 需要修改情绪标签的段落
     */
    private void modifyEmotion(@NonNull ParagraphEntity paragraph) {
        //先收起输入法
        ImmHelper.hideImm(binding.contentTextInput);

        //实例化底部对话框并显示
        EmotionTagSelectBottomSheet bottomSheet = new EmotionTagSelectBottomSheet(
                paragraph.getParagraphId(),
                (model, isChecked) -> {
                    EmotionParagraphRefEntity ref = new EmotionParagraphRefEntity(
                            model.getEmotionTag().getEmotionId(),
                            paragraph.getParagraphId()
                    );
                    EmotionTagDao dao = DiaryDatabase.getInstance(this).emotionTagDao();

                    if (isChecked) {
                        disposable.add(dao.insertEmotionParagraphRefCompletable(ref)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Log.i(
                                                LogTags.WRITE_ACTIVITY.n(),
                                                "段落编号：" + paragraph.getParagraphId() + "，添加情绪标签：" +
                                                        model.getEmotionTag().getEmotionId()
                                        ),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        );
                    } else {
                        disposable.add(dao.deleteEmotionParagraphRefCompletable(ref)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Log.i(
                                                LogTags.WRITE_ACTIVITY.n(),
                                                "段落编号：" + paragraph.getParagraphId() + "，删除情绪标签：" +
                                                        model.getEmotionTag().getEmotionId()
                                        ),
                                        e -> {
                                            Log.e(LogTags.WRITE_ACTIVITY.n(), "段落的情绪标签移除失败");
                                            ExceptionHelper.showExceptionDialog(this, e);
                                        }
                                )
                        );
                    }
                },
                (model, value) -> {
                    EmotionParagraphRefEntity ref = new EmotionParagraphRefEntity(
                            model.getEmotionTag().getEmotionId(),
                            paragraph.getParagraphId()
                    );
                    ref.setDegree(value);
                    EmotionTagDao dao = DiaryDatabase.getInstance(this).emotionTagDao();

                    disposable.add(dao.updateEmotionParagraphRefCompletable(ref)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Log.i(
                                            LogTags.WRITE_ACTIVITY.n(),
                                            "段落编号：" + paragraph.getParagraphId() +
                                                    "，情绪标签：" + model.getEmotionTag().getEmotionId() +
                                                    "，更新情绪强烈程度为" + value
                                    ),
                                    e -> {
                                        ExceptionHelper.showExceptionDialog(this, e);
                                        Log.e(
                                                LogTags.WRITE_ACTIVITY.n(),
                                                "段落编号：" + paragraph.getParagraphId() +
                                                        "，情绪标签：" + model.getEmotionTag().getEmotionId() +
                                                        "情绪强烈程度更新失败"
                                        );
                                    }
                            )
                    );
                }
        );
        bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_SELECT_BOTTOM_SHEET.getTag());
    }

    /**
     * 删除段落
     *
     * @param paragraph 待删除的段落实例
     */
    private void deleteParagraph(ParagraphEntity paragraph) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_paragraph)
                .setMessage("此操作将删除该段落，确定继续吗？")
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    //判断是否为正在编辑的段落，是则退出编辑
                    if (modifyingParagraph != null && paragraph.getParagraphId() == modifyingParagraph.getParagraphId()) {
                        setEditMode(false, null, null);
                    }

                    //多线程删除段落
                    disposable.add(ParagraphService.deleteParagraphAndMedia(paragraph, this)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                                Log.i(LogTags.WRITE_ACTIVITY.n(), "段落删除成功");
                                Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                            }, throwable -> {
                                Log.e(LogTags.WRITE_ACTIVITY.n(), "段落删除失败");
                                ExceptionHelper.showExceptionDialog(this, throwable);
                            })
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 设置编辑模式
     *
     * @param isEditMode         是否启用编辑模式
     * @param modifyingParagraph 如果启用编辑模式，该参数传递的是正在编辑的段落实体
     * @param mediaList          正在编辑的段落的媒体列表
     */
    private void setEditMode(boolean isEditMode, ParagraphEntity modifyingParagraph, List<MediaEntity> mediaList) {
        //如果启用则注册返回处理器
        if (isEditMode) {
            backHelper.registerHandler(editBackHandler);
        } else {
            backHelper.unregisterHandler(editBackHandler);
        }

        //定义过渡动画：组合滑入和渐变
        TransitionSet set = new TransitionSet()
                .addTransition(new Slide(Gravity.BOTTOM))
                .addTransition(new Fade())
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.contentEditCard, set);

        //执行状态改变
        if (isEditMode) {
            this.modifyingParagraph = modifyingParagraph;
            CharSequence richText = ParagraphTextConverter.hierarchic(this, modifyingParagraph.getContent());
            binding.originText.setText(richText);        //显示原始文本
            binding.contentTextInput.setText(richText);  //填充原始文本到输入框
            binding.contentTextInput.setSelection(richText.length());   //光标移动到末尾
            binding.contentEditCard.setVisibility(View.VISIBLE);

            //自动显示输入法
            ImmHelper.showImm(binding.contentTextInput);
        } else {
            this.modifyingParagraph = null;
            binding.contentEditCard.setVisibility(View.GONE);
            binding.contentTextInput.setText(null);         //清空输入框
        }

        //如果带有媒体，则显示媒体列表，否则关闭
        if (mediaList != null && !mediaList.isEmpty()) {
            mediaAdapter.submitList(new ArrayList<>(mediaList));
            setMediaRecyclerVisible(true);
        } else {
            mediaAdapter.submitList(new ArrayList<>());
            setMediaRecyclerVisible(false);
        }
    }

    /**
     * 设置临时媒体列表可见性
     *
     * @param isVisible 是否可见
     */
    private void setMediaRecyclerVisible(boolean isVisible) {
        if (isVisible && binding.mediaCard.getVisibility() == View.VISIBLE ||
                !isVisible && binding.mediaCard.getVisibility() == View.GONE) {
            return;
        }

        //如果显示则注册返回处理器
        if (isVisible) {
            backHelper.registerHandler(mediaBackHandler);
        } else {
            backHelper.unregisterHandler(mediaBackHandler);
        }

        //定义过渡动画：组合滑入和渐变
        TransitionSet set = new TransitionSet()
                .addTransition(new Slide(Gravity.BOTTOM))
                .addTransition(new Fade())
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.mediaCard, set);

        //切换视图可见性
        if (isVisible) {
            binding.mediaCard.setVisibility(View.VISIBLE);
        } else {
            binding.mediaCard.setVisibility(View.GONE);
        }
    }

    /**
     * 设置媒体多选模式是否启用
     *
     * @param isSelectMode 是否在媒体多选模式
     */
    private void setSelectMode(boolean isSelectMode) {
        if (isSelectMode && binding.mediaDeleteBtn.getVisibility() == View.VISIBLE ||
                !isSelectMode && binding.mediaDeleteBtn.getVisibility() == View.GONE) {
            return;
        }

        if (isSelectMode) {
            backHelper.registerHandler(selectionBackHandler);
        } else {
            backHelper.unregisterHandler(selectionBackHandler);
        }

        //定义过渡动画
        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .addTarget(binding.mediaDeleteBtn)
                .addTarget(binding.mediaAddBtn)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.contentInputCard, set);

        //切换视图可见性
        if (isSelectMode) {
            binding.mediaDeleteBtn.setVisibility(View.VISIBLE);
            binding.mediaAddBtn.setVisibility(View.GONE);
        } else {
            binding.mediaDeleteBtn.setVisibility(View.GONE);
            binding.mediaAddBtn.setVisibility(View.VISIBLE);
        }

        mediaAdapter.setSelectMode(isSelectMode);  //切换适配器选择模式
    }

    /**
     * 保存草稿到 SharedPreference
     */
    private void saveDraft() {
        if (binding != null) {
            String content = ParagraphTextConverter.flatten(binding.contentTextInput.getEditableText());
            DraftPreference.setDraft(this, content);
        }
    }
}