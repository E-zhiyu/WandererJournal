package com.wanderer.journal.ui.others.bottom.role;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleGroupUiModel;
import com.wanderer.journal.databinding.FragmentRoleGroupBinding;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.ui.others.adapters.role.GroupRoleSelectAdapter;
import com.wanderer.journal.ui.others.viewmodel.RoleSelectViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoleGroupFragment extends Fragment {
    private FragmentRoleGroupBinding binding;       //绑定的 XML 布局
    private GroupRoleSelectAdapter adapter;         //根据拼音分组的适配器
    private int groupKey;                           //该 Fragment 对应的分组关键字

    @NonNull
    public static RoleGroupFragment newInstance(int groupKey) {
        RoleGroupFragment fragment = new RoleGroupFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KeyStrings.KEY_ROLE_GROUP.getS(), groupKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //取出需要获取的分组关键字
            groupKey = getArguments().getInt(KeyStrings.KEY_ROLE_GROUP.getS());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRoleGroupBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            binding.recycler.setPadding(0, 0, 0, systemBars.bottom);

            return insets;
        });

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        RoleSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(RoleSelectViewModel.class);
        adapter = new GroupRoleSelectAdapter(viewModel::setSelectedRole);
        binding.recycler.setAdapter(adapter);
        viewModel.getGroupedMap().observe(getViewLifecycleOwner(), groupedMap -> {
            List<RoleEntity> roleList = groupedMap.get(groupKey);
            submitRoleList(roleList);
        });
    }

    /**
     * 更新角色列表
     *
     * @param roleList 角色列表
     */
    private void submitRoleList(@Nullable List<RoleEntity> roleList) {
        if (roleList == null || roleList.isEmpty()) return;

        //根据拼音分组为 Map
        Map<String, List<RoleEntity>> pinyinGroupedRoleMap = roleList.stream()
                .sorted(Comparator.comparing(role -> {  //先将列表中的元素按照字母顺序排序
                    String finalDisplay = role.getDisplayName().isEmpty() ?
                            role.getName() :
                            role.getDisplayName();
                    return TextHelper.getPinyinFirstLetter(finalDisplay);
                }))
                .collect(Collectors.groupingBy(
                        role -> {
                            String finalDisplay = role.getDisplayName().isEmpty() ?
                                    role.getName() :
                                    role.getDisplayName();
                            return TextHelper.getPinyinFirstLetter(finalDisplay);
                        },
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        //转换为 UiModel 列表
        List<RoleGroupUiModel> uiModelList = new ArrayList<>();
        for (Map.Entry<String, List<RoleEntity>> entry : pinyinGroupedRoleMap.entrySet()) {
            //添加分隔符
            RoleGroupUiModel.Separator separator = new RoleGroupUiModel.Separator(entry.getKey());
            uiModelList.add(separator);

            //添加 Item
            RoleGroupUiModel.Item item = new RoleGroupUiModel.Item(entry.getValue());
            uiModelList.add(item);
        }

        //提交到适配器中
        adapter.submitList(uiModelList, () -> {
            binding.loadingIndicator.setVisibility(View.GONE);

            if (uiModelList.isEmpty()) {
                binding.emptyText.setVisibility(View.VISIBLE);
            } else {
                binding.emptyText.setVisibility(View.GONE);
            }
        });
    }
}
