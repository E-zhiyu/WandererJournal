package com.wanderer.journal.ui.pages.role;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleUiModel;
import com.wanderer.journal.data.save.db.services.RoleService;
import com.wanderer.journal.databinding.ActivityRoleManageBinding;
import com.wanderer.journal.databinding.ViewHolderRoleRelationshipSeparatorBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoleManageActivity extends AppCompatActivity {
    private ActivityRoleManageBinding binding;  //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();

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
        ViewEdgeHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFab);

        //初始化 RecyclerView
        initRecyclerView();
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
                    bundle.putString(KeyStrings.ROLE_IDENTITY.getS(), identity);
                    bundle.putString(KeyStrings.ROLE_IMPRESSION.getS(), impression);
                    bundle.putInt(KeyStrings.ROLE_RELATIONSHIP.getS(), relationship);
                    bundle.putStringArray(KeyStrings.ROLE_ALIAS.getS(), alias);

                    //跳转界面
                    Intent skip2RoleInput = new Intent(this, RoleInputActivity.class);
                    skip2RoleInput.putExtras(bundle);
                    startActivity(skip2RoleInput);
                },
                (model, anchor) -> {
                    //TODO:长按监听
                }
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
        disposable.add(RoleService.getAllRoleFlowable(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleList -> {
                            if (roleList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }

                            adapter.submitList(roleList);
                        }
                )
        );
    }
}