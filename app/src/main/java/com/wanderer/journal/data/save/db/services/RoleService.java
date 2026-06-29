package com.wanderer.journal.data.save.db.services;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.text.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.ui.RoleUiModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class RoleService {
    /**
     * 获取所有匹配搜索项的角色
     *
     * @param db      数据库实例
     * @param keyword 角色信息的搜索关键词，为空时表示不进行搜索
     * @return 符合搜索结果的角色数据，包含分隔符，支持响应式更新
     */
    public static Flowable<List<RoleUiModel>> getAllRoleFlowable(@NonNull DiaryDatabase db, String keyword) {
        RoleDao roleDao = db.roleDao();
        String safeKeyword = "";

        if (keyword != null && !keyword.trim().isEmpty()) {
            safeKeyword = keyword.replace("/", "//")
                    .replace("%", "/%")
                    .replace("_", "/_");
        }

        int isSearchFilter = !safeKeyword.isEmpty() ? 1 : 0;

        // 提前把枚举的 values 数组拿出来缓存，避免在 for 循环中高频触发内存抖动
        final RoleRelationship[] relations = RoleRelationship.values();

        return roleDao.getAllRoleWithSearchFlowable(safeKeyword, isSearchFilter)
                .map(rawList -> {
                    List<RoleUiModel> resultList = new ArrayList<>(rawList.size() + RoleRelationship.values().length); //给予合理的初始容量，减少 ArrayList 扩容开销

                    //判空
                    if (rawList.isEmpty()) {
                        return resultList;
                    }

                    //通过关系远近进行分组
                    Map<Integer, List<RoleEntityModel>> groupedMap = rawList.stream()
                            .collect(Collectors.groupingBy(
                                    model -> model.getRole().getRelationship(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //循环插入分隔符和 Item
                    for (Map.Entry<Integer, List<RoleEntityModel>> entry : groupedMap.entrySet()) {
                        String separatorText = relations[entry.getKey()].getTitle();
                        resultList.add(new RoleUiModel.Separator(separatorText));

                        List<RoleUiModel.Item> itemList = entry.getValue().stream()
                                .map(RoleUiModel.Item::new)
                                .collect(Collectors.toList());
                        resultList.addAll(itemList);
                    }

                    return resultList;
                });
    }

    /**
     * 添加角色
     *
     * @param db       数据库实例
     * @param role     新角色
     * @param aliaList 角色别名列表
     * @return 是否完成
     */
    public static Completable addRole(@NonNull DiaryDatabase db, RoleEntity role, List<String> aliaList) {
        RoleDao roleDao = db.roleDao();
        return Completable.defer(() -> {
            roleDao.addRole(role, aliaList);
            return Completable.complete();
        });
    }

    /**
     * 更新角色
     *
     * @param role     新角色
     * @param aliaList 角色别名列表
     * @return 是否完成
     */
    public static Completable updateRole(@NonNull DiaryDatabase db, RoleEntity role, List<String> aliaList) {
        RoleDao roleDao = db.roleDao();
        return Completable.defer(() -> {
            roleDao.updateRoleAndAlia(role, aliaList);
            return Completable.complete();
        });
    }

    /**
     * 删除角色并将角色引用文本冲刷为普通文本
     *
     * @param role 需要删除的角色
     * @return 是否完成
     */
    public static Completable deleteRole(RoleEntity role, @NonNull DiaryDatabase db) {
        RoleDao roleDao = db.roleDao();
        return Completable.defer(() -> {
            roleDao.deleteRoleAndWash(role);
            return Completable.complete();
        });
    }
}
