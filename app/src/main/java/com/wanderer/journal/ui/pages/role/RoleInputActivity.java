package com.wanderer.journal.ui.pages.role;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.dropdown.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.services.RoleService;
import com.wanderer.journal.databinding.ActivityRoleInputBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.NoFilteringArrayAdapter;
import com.wanderer.journal.ui.others.dialogs.EditTextDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoleInputActivity extends AppCompatActivity {
    private ActivityRoleInputBinding binding;   //绑定的 XML 布局
    private Bundle initBundle;                  //包含初始化数据的数据包
    private RoleRelationship relationship;      //角色关系程度
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleInputBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
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
                int keyboardHeight = Math.max(systemBars.bottom, imeInsets.bottom);
                binding.getRoot().setPadding(systemBars.left, 0, systemBars.right, keyboardHeight);

                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                super.onEnd(animation);
            }
        });

        initBundle = getIntent().getExtras();
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
        //工具栏
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_role);
        }
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //名称
        String initName = initBundle != null ? initBundle.getString(KeyStrings.ROLE_NAME.getS()) : "";
        binding.nameInput.setText(initName);
        binding.nameInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.nameLayout.setError(null);
            } else {
                String input = String.valueOf(binding.nameInput.getText());
                if (input.isEmpty()) {
                    binding.nameLayout.setError("名称不能为空");
                }
            }
        });

        //身份
        String initIdentity = initBundle != null ? initBundle.getString(KeyStrings.ROLE_IDENTITY.getS()) : "";
        binding.identityInput.setText(initIdentity);

        //印象
        String initImpression = initBundle != null ? initBundle.getString(KeyStrings.ROLE_IMPRESSION.getS()) : "";
        binding.impressionInput.setText(initImpression);

        //关系
        relationship = initBundle != null ?
                RoleRelationship.values()[initBundle.getInt(KeyStrings.ROLE_RELATIONSHIP.getS())] :
                RoleRelationship.NORMAL;
        binding.relationshipInput.setText(relationship.getTitle());
        String[] relationships = Arrays.stream(RoleRelationship.values())
                .map(RoleRelationship::getTitle)
                .toArray(String[]::new);
        NoFilteringArrayAdapter<String> adapter = new NoFilteringArrayAdapter<>(this, relationships);
        binding.relationshipInput.setAdapter(adapter);
        binding.relationshipInput.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i >= 0 && i < RoleRelationship.values().length) {
                relationship = RoleRelationship.values()[i];
            }
        });

        //添加别名
        binding.addAliaChip.setOnClickListener(view -> new EditTextDialogBuilder(this, "输入别名", "别名")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", inputStr -> {
                    if (inputStr.trim().isEmpty()) {
                        Toast.makeText(this, "输入的内容不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (binding.aliaRecycler.getAdapter() instanceof RoleAliasAdapter) {
                        RoleAliasAdapter aliasAdapter = (RoleAliasAdapter) binding.aliaRecycler.getAdapter();
                        List<String> aliaList = new ArrayList<>(aliasAdapter.getCurrentList());
                        aliaList.add(inputStr);
                        aliasAdapter.submitList(aliaList);
                        Toast.makeText(this, "别名添加成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "别名添加失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .show()
        );

        //别名列表
        RoleAliasAdapter aliasAdapter = new RoleAliasAdapter();
        binding.aliaRecycler.setAdapter(aliasAdapter);
        String[] initAlias = initBundle != null ?
                initBundle.getStringArray(KeyStrings.ROLE_ALIAS.getS()) :
                new String[0];
        if (initAlias == null) initAlias = new String[0];
        List<String> initAliaList = Arrays.stream(initAlias)
                .collect(Collectors.toList());
        aliasAdapter.submitList(initAliaList);

        //确认按钮
        binding.confirmButton.setOnClickListener(view -> {
            String err = verifyInput();
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                return;
            }

            onConfirm();
        });

        //取消按钮
        binding.cancelButton.setOnClickListener(view -> finish());
    }

    /**
     * 校验输入的内容
     *
     * @return 错误提示，无错误则返回 null
     */
    @Nullable
    private String verifyInput() {
        String err = null;

        if (String.valueOf(binding.nameInput.getText()).isEmpty()) {
            err = "名称不能为空";
            binding.nameLayout.setError(err);
        }

        return err;
    }

    /**
     * 确认按钮点击回调
     */
    private void onConfirm() {
        //获取输入内容
        String name = String.valueOf(binding.nameInput.getText());
        String identity = String.valueOf(binding.identityInput.getText());
        String impression = String.valueOf(binding.impressionInput.getText());
        int relationship = this.relationship.ordinal();
        List<String> aliaList;
        if (binding.aliaRecycler.getAdapter() instanceof RoleAliasAdapter) {
            RoleAliasAdapter aliasAdapter = (RoleAliasAdapter) binding.aliaRecycler.getAdapter();
            aliaList = aliasAdapter.getCurrentList();
        } else {
            aliaList = new ArrayList<>();
        }

        //插入数据
        RoleEntity role = new RoleEntity(name, identity, impression, relationship);
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        if (initBundle == null) {
            disposable.add(RoleService.addRole(db, role, aliaList)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "角色新建成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            role.setRoleId(initBundle.getLong(KeyStrings.ROLE_ID.getS()));
            disposable.add(RoleService.updateRole(db, role, aliaList)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "角色修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
    }
}