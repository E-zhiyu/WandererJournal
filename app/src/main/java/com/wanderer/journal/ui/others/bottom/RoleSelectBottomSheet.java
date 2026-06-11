package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.composite.RoleUiModel;
import com.wanderer.journal.data.save.db.services.RoleService;
import com.wanderer.journal.databinding.BottomSheetRoleSelectBinding;
import com.wanderer.journal.databinding.ViewHolderRoleRelationshipSeparatorBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.wanderer.journal.ui.pages.role.RoleAdapter;

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
        //构造适配器
        RoleAdapter adapter = new RoleAdapter(
                model -> {
                    if (!(model instanceof RoleUiModel.Item)) return;

                    RoleUiModel.Item item = (RoleUiModel.Item) model;
                    String roleName = item.model.getRole().getName();
                    long roleId = item.model.getRole().getRoleId();
                    selectListener.onSelected(roleName, roleId);
                },
                (model, anchor) -> {
                    if (!(model instanceof RoleUiModel.Item)) return;

                    RoleUiModel.Item item = (RoleUiModel.Item) model;
                    String roleName = item.model.getRole().getName();
                    long roleId = item.model.getRole().getRoleId();
                    selectListener.onSelected(roleName, roleId);
                }
        );
        binding.roleRecycler.setAdapter(adapter);

        //添加装饰器
        StickyHeaderItemDecoration<ViewHolderRoleRelationshipSeparatorBinding> decoration =
                new StickyHeaderItemDecoration<>(
                        adapter,
                        ViewHolderRoleRelationshipSeparatorBinding::inflate,
                        (binding1, data) -> binding1.separatorText.setText(data)
                );
        binding.roleRecycler.addItemDecoration(decoration);

        //读取数据
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        disposable.add(RoleService.getAllRoleFlowable(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        adapter::submitList,
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }
}
