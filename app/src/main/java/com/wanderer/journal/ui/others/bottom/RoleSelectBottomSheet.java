package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.databinding.BottomSheetRoleSelectBinding;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

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
        //TODO:显示最近常用角色
//        //构造适配器
//        RoleSelectAdapter adapter = new RoleSelectAdapter(
//                role -> {
//                    String roleName = role.getName();
//                    String roleDisplayName = role.getDisplayName();
//                    long roleId = role.getRoleId();
//                    selectListener.onSelected(roleDisplayName.isEmpty() ? roleName : roleDisplayName, roleId);
//                    dismiss();
//                }
//        );
//        binding.roleRecycler.setAdapter(adapter);
//
//        //读取数据
//        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
//        RoleDao roleDao = db.roleDao();
//        disposable.add(roleDao.getAllRoleSingle()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(
//                        adapter::submitList,
//                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
//                )
//        );
    }
}
