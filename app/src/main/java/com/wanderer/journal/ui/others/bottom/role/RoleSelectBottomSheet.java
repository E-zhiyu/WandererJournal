package com.wanderer.journal.ui.others.bottom.role;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayoutMediator;
import com.wanderer.journal.auxiliary.enums.text.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.databinding.BottomSheetRoleSelectBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.FragmentPagerAdapter;
import com.wanderer.journal.ui.others.adapters.role.CommonRoleSelectAdapter;
import com.wanderer.journal.ui.others.bottom.BaseBottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoleSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetRoleSelectBinding binding;       //绑定的 XML 布局
    private final OnRoleSelectListener selectListener;  //角色选择回调
    private final CompositeDisposable disposable = new CompositeDisposable();

    /**
     * @param selectListener 角色选择回调
     */
    public RoleSelectBottomSheet(OnRoleSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public interface OnRoleSelectListener {
        /**
         * 角色选择回调
         *
         * @param name   角色名称
         * @param roleId 角色 ID
         */
        void onSelected(String name, long roleId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetRoleSelectBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
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
    }

    /**
     * 初始化常用角色
     */
    private void initCommonRole() {
        //实例化适配器
        CommonRoleSelectAdapter commonRoleAdapter = new CommonRoleSelectAdapter(
                role -> {
                    String roleName = role.getName();
                    String roleDisplayName = role.getDisplayName();
                    long roleId = role.getRoleId();
                    selectListener.onSelected(roleDisplayName.isEmpty() ? roleName : roleDisplayName, roleId);
                    dismiss();
                }
        );
        binding.commonRoleRecycler.setAdapter(commonRoleAdapter);

        //订阅数据
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        disposable.add(db.roleDao().getCommonRoleFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleList -> {
                            commonRoleAdapter.submitList(roleList);

                            if (roleList.isEmpty()) {
                                binding.commonRoleRecycler.setVisibility(View.GONE);
                                binding.commonRoleTitle.setVisibility(View.GONE);
                            } else {
                                binding.commonRoleRecycler.setVisibility(View.VISIBLE);
                                binding.commonRoleTitle.setVisibility(View.VISIBLE);
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
        //分组 Fragment
        List<RoleGroupFragment> fragmentList = new ArrayList<>(RoleRelationship.values().length + 1);
        for (int i = 0; i < RoleRelationship.values().length + 1; i++) {
            fragmentList.add(new RoleGroupFragment(role -> {
                String roleName = role.getName();
                String roleDisplayName = role.getDisplayName();
                long roleId = role.getRoleId();
                selectListener.onSelected(roleDisplayName.isEmpty() ? roleName : roleDisplayName, roleId);
                dismiss();
            }));
        }
        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(requireActivity(), fragmentList);
        binding.groupPager.setAdapter(pagerAdapter);

        //Tab栏
        new TabLayoutMediator(
                binding.roleGroupTabLayout,
                binding.groupPager,
                (tab, i) -> {
                    if (i != 0) {
                        tab.setText(RoleRelationship.values()[i - 1].getTitle());
                    } else {
                        tab.setText("全部");
                    }
                }
        ).attach();

        //绑定数据
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        disposable.add(db.roleDao().getAllRoleSingle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleList -> {
                            //将角色根据关系程度分组
                            Map<Integer, List<RoleEntity>> groupedRoleMap = roleList.stream()
                                    .collect(Collectors.groupingBy(
                                            RoleEntity::getRelationship,
                                            LinkedHashMap::new,
                                            Collectors.toList()
                                    ));

                            //添加到对应的 Fragment 中
                            fragmentList.get(0).submitRoleList(roleList);   //全部分组
                            int i = 1;
                            for (Map.Entry<Integer, List<RoleEntity>> entry : groupedRoleMap.entrySet()) {
                                fragmentList.get(i).submitRoleList(entry.getValue());
                                i++;
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }
}
