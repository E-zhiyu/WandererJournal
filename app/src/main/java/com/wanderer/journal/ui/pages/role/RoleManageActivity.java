package com.wanderer.journal.ui.pages.role;

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
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleUiModel;
import com.wanderer.journal.data.save.db.services.RoleService;
import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.data.save.preference.TipPreference;
import com.wanderer.journal.databinding.ActivityRoleManageBinding;
import com.wanderer.journal.databinding.ViewHolderRoleRelationshipSeparatorBinding;
import com.wanderer.journal.helpers.BackPressedCallbackHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.SearchHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.wanderer.journal.ui.others.viewmodel.RoleManageViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoleManageActivity extends AppCompatActivity {
    private ActivityRoleManageBinding binding;  //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();
    private BackPressedCallbackHelper backHelper;   //返回手势拦截器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;    //搜索返回处理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleManageBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //RecyclerView
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);

            return insets;
        });

        initViews();
        initBackHandlers();

        binding.getRoot().postDelayed(this::initGuide, 250);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            Intent skip2RoleInput = new Intent(this, RoleInputActivity.class);
            startActivity(skip2RoleInput);
        });
        AppearanceHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离
        AppearanceHelper.attachMorphAnimation(binding.addFab);

        //初始化 RecyclerView
        initRecyclerView();

        //初始化搜索视图
        initSearchComponents();
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
                RoleManageViewModel viewModel = new ViewModelProvider(RoleManageActivity.this).get(RoleManageViewModel.class);
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
     * 初始化引导提示
     */
    private void initGuide() {
        //角色引用的方法
        TipPreference.showTip(
                binding.addFab,
                Gravity.START,
                "添加角色后，可在日记段落中使用“@”引用该角色",
                TipPreference.KEY_ROLE_REF_METHOD,
                1
        );
    }

    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView() {
        RoleAdapter adapter = new RoleAdapter(
                model -> {
                    if (!(model instanceof RoleUiModel.Item)) {
                        return;
                    }

                    //解析数据
                    RoleUiModel.Item item = (RoleUiModel.Item) model;
                    RoleEntity role = item.model.getRole();
                    long roleId = role.getRoleId();
                    String roleName = role.getName();
                    String roleDisplayName = role.getDisplayName();
                    String identity = role.getIdentity();
                    String impression = role.getImpression();
                    int relationship = role.getRelationship();
                    String[] alias = item.model.getRoleAliaList().stream()
                            .map(RoleAliaEntity::getAlia)
                            .toArray(String[]::new);

                    //生成数据包
                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.ROLE_ID.getS(), roleId);
                    bundle.putString(KeyStrings.ROLE_NAME.getS(), roleName);
                    bundle.putString(KeyStrings.ROLE_DISPLAY_NAME.getS(), roleDisplayName);
                    bundle.putString(KeyStrings.ROLE_IDENTITY.getS(), identity);
                    bundle.putString(KeyStrings.ROLE_IMPRESSION.getS(), impression);
                    bundle.putInt(KeyStrings.ROLE_RELATIONSHIP.getS(), relationship);
                    bundle.putStringArray(KeyStrings.ROLE_ALIAS.getS(), alias);

                    //跳转界面
                    Intent skip2RoleInput = new Intent(this, RoleInputActivity.class);
                    skip2RoleInput.putExtras(bundle);
                    startActivity(skip2RoleInput);
                },
                this::showRolePopupMenu
        );
        binding.recycler.setAdapter(adapter);

        //添加粘性头部装饰器
        StickyHeaderItemDecoration<ViewHolderRoleRelationshipSeparatorBinding> decoration =
                new StickyHeaderItemDecoration<>(
                        adapter,
                        ViewHolderRoleRelationshipSeparatorBinding::inflate,
                        (binding, data) -> binding.separatorText.setText(data)
                );
        binding.recycler.addItemDecoration(decoration);

        //订阅数据
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        RoleManageViewModel viewModel = new ViewModelProvider(this).get(RoleManageViewModel.class);
        disposable.add(viewModel.getRoleListFlowable(db)
                .subscribe(
                        roleList -> {
                            VisibilityHelper.toggleVisibilityWithFade(binding.loadingIndicator, false);
                            VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, roleList.isEmpty());

                            adapter.submitList(roleList);
                        }
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
                SearchHistoryPreference.KEY_ROLE_INFO,
                keyword -> {
                    RoleManageViewModel viewModel = new ViewModelProvider(this).get(RoleManageViewModel.class);
                    viewModel.executeSearch(keyword.trim());

                    //根据搜索关键词是否为空开启和关闭搜索模式
                    setSearchMode(!keyword.trim().isEmpty());
                },
                null
        );
    }

    /**
     * 显示角色长按菜单
     *
     * @param model  数据模型
     * @param anchor 锚点视图
     */
    private void showRolePopupMenu(RoleUiModel model, View anchor) {
        if (!(model instanceof RoleUiModel.Item)) {
            return;
        }
        RoleUiModel.Item itemModel = (RoleUiModel.Item) model;

        PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_role_edit, popupMenu.getMenu());

        //设置监听
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_role) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete_role)
                        .setMessage("即将删除该角色，所有引用该角色的段落内容都将发生不可逆的变化，确认继续吗？")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            DiaryDatabase db = DiaryDatabase.getInstance(this);
                            disposable.add(RoleService.deleteRole(itemModel.model.getRole(), db)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            () -> Toast.makeText(this, "角色删除成功", Toast.LENGTH_SHORT).show(),
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
}