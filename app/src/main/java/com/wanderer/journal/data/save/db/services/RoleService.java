package com.wanderer.journal.data.save.db.services;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.dropdown.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.RoleUiModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class RoleService {
    /**
     * 获取所有角色
     *
     * @param db 数据库实例
     * @return 所有角色的数据，包含分隔符，支持响应式更新
     */
    public static Flowable<List<RoleUiModel>> getAllRoleFlowable(@NonNull DiaryDatabase db, String keyword) {
        RoleDao roleDao = db.roleDao();
        String safeKeyword;
        if (keyword != null) {
            safeKeyword = keyword.replace("/", "//")
                    .replace("%", "/%")
                    .replace("_", "/_");
        } else {
            safeKeyword = "";
        }
        int isSearchFilter = !safeKeyword.isEmpty() ? 1 : 0;
        return roleDao.getAllRoleFlowable(keyword, isSearchFilter)
                .map(rawList -> {
                    List<RoleUiModel> resultList = new ArrayList<>();

                    if (rawList.isEmpty()) {
                        return resultList;
                    }

                    String firstSeparator = RoleRelationship.values()[rawList.get(0).getRole().getRelationship()].getTitle();
                    resultList.add(new RoleUiModel.Separator(firstSeparator));

                    for (int i = 0; i < rawList.size(); i++) {
                        RoleEntityModel currentModel = rawList.get(i);
                        RoleEntityModel nextModel = i < rawList.size() - 1 ? rawList.get(i + 1) : null;

                        //添加角色数据
                        resultList.add(new RoleUiModel.Item(currentModel));

                        //判断关系是否一样
                        boolean isRelationshipSame = nextModel == null ||
                                currentModel.getRole().getRelationship() == nextModel.getRole().getRelationship();

                        //关系不一样时插入分隔符
                        if (!isRelationshipSame) {
                            String separator = RoleRelationship.values()[nextModel.getRole().getRelationship()].getTitle();
                            resultList.add(new RoleUiModel.Separator(separator));
                        }
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
     * @param db       数据库实例
     * @param role     新角色
     * @param aliaList 角色别名列表
     * @return 是否完成
     */
    public static Completable updateRole(@NonNull DiaryDatabase db, RoleEntity role, List<String> aliaList) {
        RoleDao roleDao = db.roleDao();
        return Completable.defer(() -> {
            roleDao.updateRole(role, aliaList);
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
