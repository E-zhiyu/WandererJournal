package com.wanderer.journal.ui.others.bottom.role;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.text.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.preference.TipPreference;
import com.wanderer.journal.databinding.BottomSheetRoleSelectBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.ui.others.adapters.role.CommonRoleSelectAdapter;
import com.wanderer.journal.ui.others.adapters.role.RolePagerAdapter;
import com.wanderer.journal.ui.others.bottom.BaseBottomSheetDialogFragment;
import com.wanderer.journal.ui.others.viewmodel.RoleSelectViewModel;
import com.wanderer.journal.ui.pages.role.RoleInputActivity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoleSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetRoleSelectBinding binding;       //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();
    private TabLayoutMediator tabLayoutMediator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetRoleSelectBinding.inflate(inflater, container, false);

        initViews();
        observeLiveData();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            // 1. 捞出系统的底座容器
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                // 2. 强行把底座的高度设置为固定值，防止 ViewPager2 高度不同而突变
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int desiredHeight = (int) (screenHeight * 0.75); // 70% 屏幕高

                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = desiredHeight;
                bottomSheet.setLayoutParams(layoutParams);

                // 3. 配置展开状态：一探头就直接进入完全展开状态，不给它留半折腾的空间
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // 往下滑直接关闭，不允许停留在半高状态

                // 4. 设置默认的起跳高度，防止高度坍塌
                behavior.setPeekHeight(desiredHeight);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //最近常用角色
        initCommonRole();

        //分组角色
        initRoleGroup();

        //角色添加按钮
        binding.addRoleBtn.setOnClickListener(view -> {
            Intent skip2RoleInput = new Intent(requireContext(), RoleInputActivity.class);
            startActivity(skip2RoleInput);
        });
    }

    /**
     * 观察 ViewModel 的
     */
    private void observeLiveData() {
        RoleSelectViewModel roleSelectViewModel = new ViewModelProvider(requireActivity()).get(RoleSelectViewModel.class);
        roleSelectViewModel.getSelectedRoleEvent().observe(getViewLifecycleOwner(), role -> {
            disposable.clear();

            //添加角色使用次数
            DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
            disposable.add(db.roleDao().addRoleUseCount(role.getRoleId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            this::dismiss,
                            e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                    )
            );
        });
    }

    /**
     * 初始化常用角色
     */
    private void initCommonRole() {
        RoleSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(RoleSelectViewModel.class);

        //实例化适配器
        CommonRoleSelectAdapter commonRoleAdapter = new CommonRoleSelectAdapter(
                viewModel::setSelectedRole,
                role -> {
                    DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
                    disposable.add(db.roleDao().clearRoleUseCount(role.getRoleId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                    },
                                    e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                            )
                    );
                }
        );
        binding.commonRoleRecycler.setAdapter(commonRoleAdapter);

        //订阅数据
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        final int MAX_COMMON_ROLE_COUNT = 7;
        disposable.add(db.roleDao().getCommonRoleFlowable(MAX_COMMON_ROLE_COUNT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleList -> {
                            if (roleList.isEmpty()) {
                                binding.commonRoleTitle.setVisibility(View.GONE);
                                VisibilityHelper.toggleViewExpansion(
                                        binding.getRoot(),
                                        binding.commonRoleRecycler,
                                        false,
                                        Gravity.TOP,
                                        () -> commonRoleAdapter.submitList(roleList)
                                );
                            } else {
                                binding.commonRoleTitle.setVisibility(View.VISIBLE);
                                VisibilityHelper.toggleViewExpansion(
                                        binding.getRoot(),
                                        binding.commonRoleRecycler,
                                        true,
                                        Gravity.TOP,
                                        () -> {
                                            commonRoleAdapter.submitList(roleList);
                                            TipPreference.showTip(
                                                    binding.commonRoleTitle,
                                                    Gravity.END,
                                                    "长按可以移除常用角色",
                                                    TipPreference.KEY_CLEAR_ROLE_USE_COUNT,
                                                    1
                                            );
                                        }
                                );
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }

    /**
     * 初始化角色分组
     */
    private void initRoleGroup() {
        // ViewPager2 翻页器
        RolePagerAdapter pagerAdapter = new RolePagerAdapter(getParentFragmentManager(), getLifecycle());
        binding.groupPager.setAdapter(pagerAdapter);
        binding.groupPager.setOffscreenPageLimit(2);

        //绑定数据
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        disposable.add(db.roleDao().getAllRoleFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleList -> {
                            Log.d(LogTags.ROLE_SELECT_BOTTOM_SHEET.n(), "角色数量：" + roleList.size());

                            //将角色根据关系程度分组
                            Map<Integer, List<RoleEntity>> groupedRoleMap = new LinkedHashMap<>();
                            groupedRoleMap.put(-1, roleList);   //第一个先放入“全部”分组
                            groupedRoleMap.putAll(roleList.stream().collect(Collectors.groupingBy(
                                    RoleEntity::getRelationship,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            )));
                            List<String> groupTitleList = groupedRoleMap.keySet().stream()
                                    .map(key -> {
                                        if (key == -1) {
                                            return "全部";
                                        } else {
                                            return RoleRelationship.values()[key].getTitle();
                                        }
                                    })
                                    .collect(Collectors.toList());

                            //更新 ViewModel中的分组
                            RoleSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(RoleSelectViewModel.class);
                            viewModel.setGroupedMap(groupedRoleMap);

                            //更新 ViewPager2 和 TabLayout
                            if (tabLayoutMediator != null) {
                                tabLayoutMediator.detach();
                            }
                            pagerAdapter.updateData(groupedRoleMap);
                            tabLayoutMediator = new TabLayoutMediator(
                                    binding.roleGroupTabLayout,
                                    binding.groupPager,
                                    (tab, position) -> tab.setText(groupTitleList.get(position))
                            );
                            tabLayoutMediator.attach();
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }
}
