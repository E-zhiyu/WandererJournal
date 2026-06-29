package com.wanderer.journal.ui.pages;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.RoleShower;
import com.wanderer.journal.auxiliary.enums.TransitionName;
import com.wanderer.journal.auxiliary.interfaces.PagingRecyclerScrollListener;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.data.save.db.services.EmotionTagService;
import com.wanderer.journal.data.save.db.services.ParagraphService;
import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.data.save.preference.TipPreference;
import com.wanderer.journal.databinding.ActivityDiaryReadBinding;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.TagStrings;
import com.wanderer.journal.databinding.ViewHolderSeparatorTextChipBinding;
import com.wanderer.journal.helpers.BackPressedCallbackHelper;
import com.wanderer.journal.helpers.SearchHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.ScrollHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagInAppBarAdapter;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.wanderer.journal.ui.others.selections.paragraph.ParagraphKeyProvider;
import com.wanderer.journal.ui.others.selections.paragraph.ParagraphLookup;
import com.wanderer.journal.ui.others.viewmodel.EmotionTagSelectViewModel;
import com.wanderer.journal.ui.others.viewmodel.ParagraphFilterViewModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphPagingAdapter;
import com.wanderer.journal.ui.others.bottom.ParagraphFilterBottomSheet;
import com.wanderer.journal.ui.others.bottom.EmotionTagSelectBottomSheet;
import com.wanderer.journal.ui.others.dialogs.ProgressDialogBuilder;
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class DiaryReadActivity extends AppCompatActivity {
    private ActivityDiaryReadBinding binding;                               //绑定的XML布局
    private Bundle initBundle = null;                                       //传递初始化数据的数据包
    private final CompositeDisposable disposable = new CompositeDisposable();   //多线程任务订阅队列
    private Disposable searchDisposable;                                    //搜索模式下获取匹配项下标的 Disposable 对象
    private ParagraphPagingAdapter adapter;                                       //段落列表适配器
    private final AtomicInteger initScrollPosition = new AtomicInteger(-1); //界面加载时初始滚动到的位置
    private final Runnable scrollToInit = this::scrollRecyclerToInitPosition;   //滚动到初始位置的 Runnable 实例
    private BackPressedCallbackHelper backHelper;                           //返回监听帮助器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;        //搜索返回处理器
    private BackPressedCallbackHelper.BackHandler shareChoiceBackHandler;   //分享日记时多选模式的返回处理器
    private EmotionTagInAppBarAdapter appbarEmotionAdapter;                 //过滤情绪标签的显示适配器
    private SelectionTracker<Long> selectionTracker;                        //段落分享选择器
    private boolean isAndMode = true;                                       //多词搜索是否为“与”模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryReadBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //列表视图
            binding.contentRecycler.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        initBundle = getIntent().getExtras();
        initViews();
        binding.getRoot().postDelayed(this::initGuide, 250);
        observeLiveData();
        initBackHandlers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //移除待滚动的任务
        binding.contentRecycler.removeCallbacks(scrollToInit);

        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //搜索组件
        initSearchComponents();

        //顶部情绪标签视图
        appbarEmotionAdapter = new EmotionTagInAppBarAdapter(
                emotionTag -> {
                    ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);

                    //移除已选择的情绪标签 ID
                    Set<Long> checkedEmotionIdSet = viewModel.getCheckedEmotionIdSet();
                    checkedEmotionIdSet.remove(emotionTag.getEmotionId());

                    //执行一次搜索
                    executeSearch();
                }
        );
        binding.emotionTagInAppbarRecycler.setAdapter(appbarEmotionAdapter);

        //日记段落列表
        initRecyclerView();

        //向上按钮
        binding.upFab.setOnClickListener(view -> {
            ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
            viewModel.jumpToPrevious();
        });
        AppearanceHelper.attachMorphAnimation(binding.upFab);

        //向下按钮
        binding.downFab.setOnClickListener(view -> {
            ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
            viewModel.jumpToNext();
        });
        AppearanceHelper.attachMorphAnimation(binding.downFab);

        //搜索跳转组件布局
        AppearanceHelper.setMarginToNavigation(binding.searchSkipLayout, this);

        //多词搜索模式切换按钮
        binding.multiSearchModeSwitchBtn.setOnClickListener(view -> {
            isAndMode = !isAndMode;

            if (isAndMode) {
                binding.multiSearchModeSwitchBtn.setText(R.string.multi_word_search_and);
            } else {
                binding.multiSearchModeSwitchBtn.setText(R.string.multi_word_search_or);
            }
        });
    }

    /**
     * 初始化引导内容
     */
    private void initGuide() {
        TipPreference.showTip(
                binding.contentSearchBar,
                Gravity.BOTTOM,
                "用空格隔开多个关键词可以实现多词搜索",
                TipPreference.KEY_READ_MULTI_SEARCH,
                1
        );
    }

    /**
     * 初始化返回手势处理
     */
    private void initBackHandlers() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                backHelper.dispatchBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
        backHelper = new BackPressedCallbackHelper(backPressedCallback);

        //搜索返回处理器
        searchBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                ParagraphFilterViewModel viewModel = new ViewModelProvider(DiaryReadActivity.this).get(ParagraphFilterViewModel.class);
                viewModel.clearFilter();
                executeSearch();    //清空后执行一次查询（没有过滤条件不会触发滚动）
                return true;
            }

            @Override
            public int getPriority() {
                return 2;
            }
        };

        //多选模式返回处理器
        shareChoiceBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                setShareSelectMode(false);
                return true;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
    }

    /**
     * 初始化搜索组件
     */
    private void initSearchComponents() {
        SearchHelper.initSearchComponents(
                binding.contentSearchBar,
                binding.diaryContentSearchView,
                binding.searchHistoryRecycler,
                binding.clearHistoryBtn,
                SearchHistoryPreference.KEY_DIARY_CONTENT,
                keyword -> {
                    ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
                    viewModel.setSearchText(keyword);

                    //执行一次搜索
                    executeSearch();
                },
                item -> {
                    if (item.getItemId() == R.id.action_emotion_select) {
                        showFilterBottomSheet();

                        return true;
                    } else if (item.getItemId() == R.id.action_share) {
                        if (!adapter.getSelectMode()) {
                            TipPreference.showTip(
                                    binding.appBarLayout,
                                    Gravity.BOTTOM,
                                    "长按拖动可以快速多选",
                                    TipPreference.KEY_SHARE_MULTI_CHOICE,
                                    1
                            );

                            Toast.makeText(this, "选完再次点击即可分享", Toast.LENGTH_SHORT).show();
                            setShareSelectMode(true);
                        } else {
                            //判空
                            if (!selectionTracker.hasSelection()) {
                                Toast.makeText(this, "请选择至少一条日记段落", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            //获取所有选择的段落 ID
                            long[] selectedIds = StreamSupport.stream(selectionTracker.getSelection().spliterator(), false)
                                    .mapToLong(Long::longValue)
                                    .toArray();

                            //创建 Intent
                            Intent skip2SharePreview = new Intent(this, SharePreviewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putLongArray(KeyStrings.SHARED_PARAGRAPH_ID.getS(), selectedIds);
                            skip2SharePreview.putExtras(bundle);

                            //跳转界面
                            startActivity(skip2SharePreview);
                        }
                        return true;
                    } else if (item.getItemId() == R.id.action_skip_date) {
                        LocalDate currentDate;  //当前正在显示的段落的日期
                        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.contentRecycler.getLayoutManager();
                        if (layoutManager != null) {
                            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                            ParagraphUiModel model = adapter.peek(firstVisiblePosition);
                            if (model instanceof ParagraphUiModel.Separator) {
                                currentDate = ((ParagraphUiModel.Separator) model).date;
                            } else if (model instanceof ParagraphUiModel.Item) {
                                currentDate = ((ParagraphUiModel.Item) model).model.getParagraph().getCreateTime().toLocalDate();
                            } else {
                                currentDate = LocalDate.now();
                            }
                        } else {
                            currentDate = LocalDate.now();
                        }
                        DateTimePickerHelper.selectDate(
                                currentDate,
                                getSupportFragmentManager(),
                                selection -> {
                                    LocalDate selectedDate = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);

                                    //跳转到对应位置
                                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                                    DiaryDao diaryDao = db.diaryDao();
                                    disposable.add(diaryDao.getDiaryDateSeparatorPositionSingleByDate(selectedDate)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(
                                                    position -> scrollContentRecycler(
                                                            position,
                                                            true,
                                                            new PagingRecyclerScrollListener() {
                                                                @Override
                                                                public void onSucceed() {
                                                                    //判断跳转到的日期是否为选择的日期
                                                                    ParagraphUiModel model = adapter.peek(position);
                                                                    LocalDate resultDate;
                                                                    if (model instanceof ParagraphUiModel.Separator) {
                                                                        resultDate = ((ParagraphUiModel.Separator) model).date;
                                                                    } else if (model instanceof ParagraphUiModel.Item) {
                                                                        resultDate = ((ParagraphUiModel.Item) model).model
                                                                                .getParagraph()
                                                                                .getCreateTime()
                                                                                .toLocalDate();
                                                                    } else {
                                                                        resultDate = null;
                                                                    }
                                                                    if (!selectedDate.equals(resultDate)) {
                                                                        Toast.makeText(
                                                                                DiaryReadActivity.this,
                                                                                "未找到内容，已跳转至相邻日记",
                                                                                Toast.LENGTH_SHORT
                                                                        ).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onRetry(int failCount) {
                                                                }

                                                                @Override
                                                                public void onFailed() {
                                                                }
                                                            }
                                                    ),
                                                    e -> ExceptionHelper.showExceptionDialog(this, e)
                                            )
                                    );
                                }
                        );
                    }

                    return false;
                }
        );
    }

    /**
     * 显示过滤选项对话框
     */
    private void showFilterBottomSheet() {
        ParagraphFilterBottomSheet bottomSheet = new ParagraphFilterBottomSheet();
        bottomSheet.setOnDismissListener(this::executeSearch);
        bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_FILTER_BOTTOM_SHEET.getTag());
    }

    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView() {
        //设置适配器
        adapter = new ParagraphPagingAdapter(
                (model, view) -> {
                    ParagraphEntity paragraph = model.getParagraph();

                    PopupMenu menu = new PopupMenu(this, view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_modify_content) {
                            Intent skip2Write = new Intent(DiaryReadActivity.this, WriteActivity.class);
                            Bundle bundle = new Bundle();

                            bundle.putLong(KeyStrings.INIT_DATE.getS(), DateTimeConverter.fromLocalDateTime(paragraph.getCreateTime())); //段落所在的日期
                            bundle.putLong(KeyStrings.WRITE_MODIFY_PARAGRAPH_ID.getS(), paragraph.getParagraphId());    //段落 ID

                            skip2Write.putExtras(bundle);
                            startActivity(skip2Write);
                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            updateParagraphCreateTime(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_modify_emotion) {
                            modifyEmotion(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_copy_paragraph) {
                            TextHelper.copyToClipBoard(this, "日记段落", paragraph.getContent());
                            Toast.makeText(this, "段落内容已复制", Toast.LENGTH_SHORT).show();
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
                    Bundle bundle = new Bundle();
                    bundle.putStringArray(KeyStrings.FILE_URIS.getS(), uriStrArray);
                    bundle.putInt(KeyStrings.VIEW_HOLDER_POSITION.getS(), position);
                    skip2FullScreen.putExtras(bundle);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            mediaView,
                            TransitionName.PARAGRAPH_MEDIA.getS()
                    );

                    startActivity(skip2FullScreen, options.toBundle());
                },
                roleId -> RoleShower.showRoleDetail(this, disposable, roleId)
        );
        binding.contentRecycler.setAdapter(adapter);

        //应用粘性头部装饰器
        StickyHeaderItemDecoration<ViewHolderSeparatorTextChipBinding> decoration = new StickyHeaderItemDecoration<>(
                adapter,
                ViewHolderSeparatorTextChipBinding::inflate,
                (binding, data) -> binding.separatorText.setText(data)
        );
        binding.contentRecycler.addItemDecoration(decoration);

        //为适配器绑定选择追踪器
        selectionTracker = new SelectionTracker.Builder<>(
                TagStrings.PARAGRAPH_SELECTION.getTag(),
                binding.contentRecycler,
                new ParagraphKeyProvider(adapter),
                new ParagraphLookup(binding.contentRecycler),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                new SelectionTracker.SelectionPredicate<>() {
                    @Override
                    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
                        if (key < 0) return false;

                        boolean isItem = false;
                        List<ParagraphUiModel> snapshot = adapter.snapshot().getItems();
                        for (int i = 0; i < snapshot.size(); i++) {
                            ParagraphUiModel item = snapshot.get(i);
                            if ((item instanceof ParagraphUiModel.Item) && ((ParagraphUiModel.Item) item).model.getParagraph().getParagraphId() == key) {
                                isItem = true;
                                break;
                            }
                        }
                        return adapter != null && adapter.getSelectMode() && isItem;
                    }

                    @Override
                    public boolean canSetStateAtPosition(int position, boolean nextState) {
                        if (adapter == null) {
                            return false;
                        }

                        try {
                            boolean isSelectMode = adapter.getSelectMode();
                            boolean isItem = adapter.peek(position) instanceof ParagraphUiModel.Item;
                            return isSelectMode && isItem;
                        } catch (IndexOutOfBoundsException e) {
                            return false;
                        }
                    }

                    @Override
                    public boolean canSelectMultiple() {
                        return true;
                    }
                }
        ).build();
        adapter.setSelectionTracker(selectionTracker);

        //设置多选追踪器选择监听
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (selectionTracker.hasSelection()) {
                    new Handler(Looper.getMainLooper()).post(() -> setShareSelectMode(true));

                    int size = selectionTracker.getSelection().size();
                    Log.d(LogTags.WRITE_ACTIVITY.n(), "已选择：" + size);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> setShareSelectMode(false));
                    Log.d(LogTags.WRITE_ACTIVITY.n(), "选择已清除");
                }
            }
        });

        //监听数据库的响应
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        if (initBundle != null && initBundle.getLong(KeyStrings.INIT_DATE.getS(), -1) != -1) {
            long initDateTimestamp = initBundle.getLong(KeyStrings.INIT_DATE.getS());
            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "初始日期：" + initDateTimestamp);
            LocalDate initDiaryDate = DateTimeConverter.toLocalDate(initDateTimestamp);
            disposable.add(db.paragraphDao().getAdjustedPositionSingle(initDiaryDate)
                    .flatMapPublisher(
                            initPosition -> {
                                initScrollPosition.set(initPosition);
                                return viewModel.getPagingDataFlow(initPosition, db);
                            }
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            pagingData -> adapter.submitData(getLifecycle(), pagingData),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {    //没有传递参数直接从最顶部开始
            initScrollPosition.set(0);
            disposable.add(viewModel.getPagingDataFlow(0, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            pagingData -> adapter.submitData(getLifecycle(), pagingData),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }

        //添加页面加载监听，用以滚动到初始位置
        adapter.addOnPagesUpdatedListener(() -> {
            if (initScrollPosition.get() != -1) {
                //500毫秒的间隔防抖
                binding.contentRecycler.removeCallbacks(scrollToInit);
                binding.contentRecycler.postDelayed(scrollToInit, 100);
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * 将段落内容列表滚动到初始位置
     */
    private void scrollRecyclerToInitPosition() {
        //控制视图显示
        VisibilityHelper.toggleVisibilityWithFade(binding.recyclerLoadingIndicator, false);
        if (adapter.getItemCount() == 0) {
            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, true);
        } else if (adapter.getItemCount() != 0) {
            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, false);
        }

        //执行滚动操作
        int position = initScrollPosition.get();
        if (position >= adapter.getItemCount()) {
            position = adapter.getItemCount() - 1;
        }
        Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "pagesUpdated count=" + adapter.getItemCount());
        Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "LoadState 触发精确滚动位置：" + initScrollPosition.get());
        scrollContentRecycler(
                position,
                false,
                new PagingRecyclerScrollListener() {
                    @Override
                    public void onSucceed() {
                        //折叠标题栏
                        binding.appBarLayout.setExpanded(false);

                        Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动成功");
                        initScrollPosition.set(-1);
                    }

                    @Override
                    public void onRetry(int failCount) {
                        Log.w(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动重试次数：" + failCount);
                    }

                    @Override
                    public void onFailed() {
                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动失败");
                        initScrollPosition.set(-1);
                    }
                }
        );
    }

    /**
     * 开始监听 ViewModel 的 LiveData
     */
    private void observeLiveData() {
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);

        //匹配项的位置列表
        viewModel.getMatchedPositions().observe(this, positionList -> {
            Integer index = viewModel.getCurrentMatchIndex().getValue();
            if (positionList == null || index == null) {
                return;
            }

            if (positionList.isEmpty()) {
                binding.counterText.setText(R.string.not_applicable);
                Toast.makeText(this, "未找到匹配的搜索项", Toast.LENGTH_SHORT).show();
            } else {
                //更新跳转数量指示器
                String counterText = String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        positionList.size() - index,
                        positionList.size()
                );
                binding.counterText.setText(counterText);
            }
        });

        //当前匹配项下标
        viewModel.getCurrentMatchIndex().observe(this, index -> {
            //判断是否为默认占位符
            if (index == -1) {
                return;
            }

            //获取目标下标
            List<Integer> positionList = viewModel.getMatchedPositions().getValue();
            if (index >= 0 && positionList != null && !positionList.isEmpty() && index < positionList.size()) {
                int targetPosition = positionList.get(index);

                //更新跳转数量指示器
                String counterText = String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        positionList.size() - index,
                        positionList.size()
                );
                binding.counterText.setText(counterText);

                //滚动列表
                scrollContentRecycler(targetPosition, true, null);
            }
        });

        //情绪标签选择状态
        EmotionTagSelectViewModel emotionTagSelectViewModel = new ViewModelProvider(this).get(EmotionTagSelectViewModel.class);
        emotionTagSelectViewModel.getCheckedEmotionTag().observe(this, emotionTagEntity -> {
            long paragraphId = emotionTagSelectViewModel.getParagraphId();
            long emotionId = emotionTagEntity.getEmotionId();
            int degree = emotionTagSelectViewModel.getDegree();
            boolean isChecked = emotionTagSelectViewModel.isChecked();

            EmotionParagraphRefEntity refEntity = new EmotionParagraphRefEntity(emotionId, paragraphId, degree);
            DiaryDatabase db = DiaryDatabase.getInstance(this);
            if (isChecked) {
                disposable.add(EmotionTagService.addOrUpdateEmotionTagRef(refEntity, db)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> Log.i(
                                        LogTags.DIARY_READ_ACTIVITY.n(),
                                        "添加情绪标签引用，段落编号：" + paragraphId +
                                                "，情绪标签：" + emotionId +
                                                "，强烈程度：" + degree
                                ),
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
                );
            } else {
                disposable.add(db.emotionTagDao().deleteEmotionParagraphRefCompletable(refEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> Log.i(LogTags.DIARY_READ_ACTIVITY.n(),
                                        "删除情绪标签引用，段落编号：" + paragraphId +
                                                "，情绪标签：" + emotionId
                                ),
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
                );
            }
        });
    }

    /**
     * 滚动日记内容 RecyclerView，并显示进度条对话框
     *
     * @param targetPosition 需要滚动到的位置
     * @param listener       滚动状态监听器
     */
    private void scrollContentRecycler(
            int targetPosition,
            boolean withDialog,
            @Nullable PagingRecyclerScrollListener listener
    ) {
        //构建滚动进度条
        int maxRetryCount = 10;
        ProgressDialogBuilder builder = new ProgressDialogBuilder(
                this,
                "加载日记内容",
                "正在跳转至目标位置……"
        );
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);    //不可取消

        //执行滚动逻辑
        ScrollHelper.scrollPagingRecycler(
                binding.contentRecycler,
                (LinearLayoutManager) binding.contentRecycler.getLayoutManager(),
                adapter,
                targetPosition,
                AppearanceHelper.dpToPx(this, 63),
                maxRetryCount,
                750,
                new PagingRecyclerScrollListener() {
                    @Override
                    public void onSucceed() {
                        if (listener != null) {
                            listener.onSucceed();
                        }

                        if (dialog.isShowing()) {
                            Toast.makeText(DiaryReadActivity.this, "跳转成功", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "跳转成功");
                    }

                    @Override
                    public void onRetry(int failCount) {
                        if (listener != null) {
                            listener.onRetry(failCount);
                        }

                        if (withDialog) {
                            dialog.show();
                            builder.setIndeterminate(false);
                            builder.updateProgress(failCount, maxRetryCount, "正在加载日记内容……");
                        }
                        Log.w(LogTags.DIARY_READ_ACTIVITY.n(), "跳转失败重试，次数：" + failCount);
                    }

                    @Override
                    public void onFailed() {
                        if (listener != null) {
                            listener.onFailed();
                        }

                        Toast.makeText(DiaryReadActivity.this, "跳转失败", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "跳转失败，请尝试点击右侧按钮跳转至附近");
                    }
                }
        );
    }

    /**
     * 执行 ViewModel 的搜索方法并根据返回的数据更新 UI
     */
    private void executeSearch() {
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        String keyword = viewModel.getSearchText();

        //每次搜索前先清除之前的搜索订阅
        if (searchDisposable != null) {
            disposable.remove(searchDisposable);
            searchDisposable = null;
        }

        //拆分输入的字符串
        String[] words = keyword.split("\\s+"); // 按空格拆分
        List<String> validKeywordList = Arrays.stream(words)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        //开始搜索
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        Set<Long> checkedEmotionIdSet = viewModel.getCheckedEmotionIdSet();
        disposable.add((searchDisposable = viewModel.executeSearch(validKeywordList, db, isAndMode)
                .defaultIfEmpty(new ArrayList<>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        positionList -> {
                            if (!positionList.isEmpty()) {
                                adapter.setHighlightTarget(validKeywordList, checkedEmotionIdSet, positionList);
                            } else {
                                adapter.clearHighlight();
                            }
                            setSearchMode(viewModel.isHasFilter());
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                ))
        );

        //更新情绪标签列表
        binding.getRoot().post(() -> refreshFilterEmotionTagGroup(checkedEmotionIdSet));
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
                                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改成功");
                                        Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改失败");
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
        //实例化底部对话框并显示
        EmotionTagSelectBottomSheet bottomSheet = EmotionTagSelectBottomSheet.newInstance(paragraph.getParagraphId());
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
                .setPositiveButton("确定", (dialogInterface, i) -> disposable.add(ParagraphService.deleteParagraphAndMedia(paragraph, this)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {
                            Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除成功");
                            Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                        }, throwable -> {
                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除失败");
                            ExceptionHelper.showExceptionDialog(this, throwable);
                        })
                ))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 设置搜索模式
     *
     * @param isSearchMode 是否为搜索模式
     */
    private void setSearchMode(boolean isSearchMode) {
        //定义过渡动画
        TransitionSet set = new TransitionSet()
                .addTransition(new Slide(Gravity.END))
                .addTransition(new Fade())
                .addTarget(binding.searchSkipLayout)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.getRoot(), set);

        if (!isSearchMode) {
            binding.contentSearchBar.setText(null);
            binding.searchSkipLayout.setVisibility(View.GONE);

            backHelper.unregisterHandler(searchBackHandler);
            adapter.clearHighlight();                               //清除文本高亮

            disposable.remove(searchDisposable);
        } else {
            binding.searchSkipLayout.setVisibility(View.VISIBLE);

            backHelper.registerHandler(searchBackHandler);
        }
    }

    /**
     * 设置分享时的段落多选模式是否开启
     *
     * @param isSelectMode 是否开启段落多选模式
     */
    private void setShareSelectMode(boolean isSelectMode) {
        if (isSelectMode == adapter.getSelectMode()) return;

        //更新 UI
        adapter.setSelectMode(isSelectMode);
        if (isSelectMode) {
            backHelper.registerHandler(shareChoiceBackHandler);
        } else {
            backHelper.unregisterHandler(shareChoiceBackHandler);

            selectionTracker.clearSelection();  //清空多选
        }
    }

    /**
     * 刷新选中的情绪标签显示视图
     *
     * @param checkedEmotionTagIdSet 需要获取的情绪标签的 ID 集合
     */
    private void refreshFilterEmotionTagGroup(@Nullable Set<Long> checkedEmotionTagIdSet) {
        if (checkedEmotionTagIdSet != null && !checkedEmotionTagIdSet.isEmpty()) {
            VisibilityHelper.toggleViewExpansion(
                    binding.getRoot(),
                    binding.emotionTagInAppbarRecycler,
                    true,
                    Gravity.TOP,
                    250,
                    () -> {
                        EmotionTagDao emotionTagDao = DiaryDatabase.getInstance(this).emotionTagDao();
                        disposable.add(emotionTagDao.getEmotionTagSingleByIdList(checkedEmotionTagIdSet)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        emotionTagList -> appbarEmotionAdapter.submitList(emotionTagList),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        );
                    }
            );
        } else {
            VisibilityHelper.toggleViewExpansion(
                    binding.getRoot(),
                    binding.emotionTagInAppbarRecycler,
                    false,
                    Gravity.TOP,
                    250,
                    () -> appbarEmotionAdapter.submitList(new ArrayList<>())
            );
        }
    }
}