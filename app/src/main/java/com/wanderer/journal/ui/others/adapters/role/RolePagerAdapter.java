package com.wanderer.journal.ui.others.adapters.role;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.ui.others.bottom.role.RoleGroupFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RolePagerAdapter extends FragmentStateAdapter {
    private final List<Integer> idList = new ArrayList<>();

    public RolePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    /**
     * 当数据库发射新数据时，调用此方法更新适配器内部的数据源
     */
    public void updateData(@NonNull Map<Integer, List<RoleEntity>> newData) {
        int oldCount = idList.size();
        idList.clear();
        notifyItemRangeRemoved(0, oldCount);

        idList.addAll(newData.keySet());
        // 通知 ViewPager2 整体结构发生了改变
        notifyItemRangeInserted(0, idList.size());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return RoleGroupFragment.newInstance(idList.get(position));
    }

    @Override
    public int getItemCount() {
        return idList.size();
    }

    // ==================== 【生死宣判的两行重写】 ====================

    @Override
    public long getItemId(int position) {
        // 【核心】不要返回 position！返回该分类字符串的 hashCode（或者分类的唯一ID）
        // 这样 Android 就能通过这个 Long 型指纹精准识别这个页面到底是谁
        return idList.get(position);
    }

    @Override
    public boolean containsItem(long itemId) {
        // 【核心】当数据集刷新时，ViewPager2 会拿着旧 Fragment 的 id 来询问：
        // “这个页面在新的分组里还存在吗？” 我们通过遍历最新的数据集来告诉它存在还是不存在
        for (int category : idList) {
            if (category == itemId) {
                return true;
            }
        }
        return false;
    }
}