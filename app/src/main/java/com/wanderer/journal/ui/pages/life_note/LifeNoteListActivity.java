package com.wanderer.journal.ui.pages.life_note;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.databinding.ActivityLifeNoteListBinding;
import com.wanderer.journal.helpers.BackPressedCallbackHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.SearchHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.ui.others.viewmodel.LifeNoteListViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LifeNoteListActivity extends AppCompatActivity {
    private ActivityLifeNoteListBinding binding;   //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();
    private BackPressedCallbackHelper backHelper;   //返回手势拦截器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;    //搜索返回处理器

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

        initBackHandlers();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化返回手势拦截器
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

        //搜索
        searchBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                setSearchMode(false);
                LifeNoteListViewModel viewModel = new ViewModelProvider(LifeNoteListActivity.this).get(LifeNoteListViewModel.class);
                viewModel.executeSearch("");
                return true;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            Intent skip2Input = new Intent(this, LifeNoteInputActivity.class);
            startActivity(skip2Input);
        });
        AppearanceHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离
        AppearanceHelper.attachMorphAnimation(binding.addFab);

        initRecycler();
        initSearchComponents();
    }

    /**
     * 初始化列表视图
     */
    private void initRecycler() {
        LifeNoteListAdapter adapter = new LifeNoteListAdapter(
                (entity, anchor) -> {
                    Intent skip2Input = new Intent(this, LifeNoteInputActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.LIFE_NOTE_ID.getS(), entity.getNoteId());
                    skip2Input.putExtras(bundle);

                    startActivity(skip2Input);
                },
                this::showEmotionTagPopupMenu
        );
        binding.recycler.setAdapter(adapter);

        //获取数据源
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        LifeNoteListViewModel viewModel = new ViewModelProvider(this).get(LifeNoteListViewModel.class);
        disposable.add(viewModel.getLifeNoteListFlowable(db)
                .subscribe(
                        lifeNoteList -> {
                            VisibilityHelper.toggleVisibilityWithFade(binding.loadingIndicator, false);
                            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, lifeNoteList.isEmpty());

                            adapter.submitList(lifeNoteList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 初始化搜索视图
     */
    private void initSearchComponents() {
        SearchHelper.initSearchComponents(
                binding.searchBar,
                binding.searchView,
                binding.searchHistoryRecycler,
                binding.clearHistoryBtn,
                SearchHistoryPreference.KEY_LIFE_NOTE,
                keyword -> {
                    LifeNoteListViewModel viewModel = new ViewModelProvider(this).get(LifeNoteListViewModel.class);
                    viewModel.executeSearch(keyword);

                    //根据搜索关键词是否为空开启和关闭搜索模式
                    setSearchMode(!keyword.isEmpty());
                },
                null
        );
    }

    /**
     * 设置搜索模式
     *
     * @param isSearchMode 是否为搜索模式
     */
    private void setSearchMode(boolean isSearchMode) {
        if (!isSearchMode) {
            binding.searchBar.setText("");
            backHelper.unregisterHandler(searchBackHandler);
        } else {
            backHelper.registerHandler(searchBackHandler);
        }
    }

    /**
     * 显示PopupMenu
     *
     * @param note 需要被操作的人生笔记
     * @param view PopupMenu 绑定的视图
     */
    private void showEmotionTagPopupMenu(LifeNoteEntity note, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_life_note_edit, popupMenu.getMenu());

        //设置监听
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_life_note) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("删除人生笔记")
                        .setMessage("确定要删除该人生笔记吗？此操作无法撤销。")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            DiaryDatabase db = DiaryDatabase.getInstance(this);
                            disposable.add(db.lifeNoteDao().deleteLifeNote(note)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            () -> Toast.makeText(this, "人生笔记已删除", Toast.LENGTH_SHORT).show(),
                                            e -> ExceptionHelper.showExceptionDialog(this, e)
                                    )
                            );
                        })
                        .setNegativeButton("取消", null)
                        .show();

                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}