package com.wanderer.journal.ui.others.bottom.role;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleGroupUiModel;
import com.wanderer.journal.databinding.FragmentRoleGroupBinding;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.ui.others.adapters.role.GroupRoleSelectAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoleGroupFragment extends Fragment {
    private FragmentRoleGroupBinding binding;       //绑定的 XML 布局
    private final GroupRoleSelectAdapter adapter;   //根据拼音分组的适配器

    public RoleGroupFragment(@NonNull OnClickListener clickListener) {
        adapter = new GroupRoleSelectAdapter(clickListener::onClick);
    }

    public interface OnClickListener {
        void onClick(RoleEntity role);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRoleGroupBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        binding.recycler.setAdapter(adapter);
    }

    /**
     * 更新角色列表
     *
     * @param roleList 角色列表
     */
    public void submitRoleList(@NonNull List<RoleEntity> roleList) {
        //根据拼音分组为 Map
        Map<String, List<RoleEntity>> pinyinGroupedRoleMap = roleList.stream()
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
        adapter.submitList(uiModelList);
    }
}
