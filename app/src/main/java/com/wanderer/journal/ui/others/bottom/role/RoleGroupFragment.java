package com.wanderer.journal.ui.others.bottom.role;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleGroupUiModel;
import com.wanderer.journal.databinding.FragmentRoleGroupBinding;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.ui.others.adapters.role.GroupRoleSelectAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoleGroupFragment extends Fragment {
    private FragmentRoleGroupBinding binding;       //绑定的 XML 布局
    private GroupRoleSelectAdapter adapter;         //根据拼音分组的适配器
    private final OnClickListener clickListener;
    private final List<RoleEntity> roleList;

    public RoleGroupFragment(@NonNull OnClickListener clickListener, @NonNull List<RoleEntity> roleList) {
        this.clickListener = clickListener;
        this.roleList = roleList;
    }

    public interface OnClickListener {
        void onClick(RoleEntity role);
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
        adapter = new GroupRoleSelectAdapter(clickListener::onClick);
        binding.recycler.setAdapter(adapter);
        submitRoleList(roleList);
    }

    /**
     * 更新角色列表
     *
     * @param roleList 角色列表
     */
    private void submitRoleList(@NonNull List<RoleEntity> roleList) {
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
