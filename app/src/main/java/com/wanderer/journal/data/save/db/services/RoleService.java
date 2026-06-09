package com.wanderer.journal.data.save.db.services;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.dropdown.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;
import com.wanderer.journal.data.save.db.entities.composite.RoleUiModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public class RoleService {
    /**
     * 获取所有角色
     *
     * @param db 数据库实例
     * @return 所有角色的数据，包含分隔符，支持响应式更新
     */
    public static Flowable<List<RoleUiModel>> getAllRoleFlowable(@NonNull DiaryDatabase db) {
        RoleDao roleDao = db.roleDao();
        return roleDao.getAllRoleFlowable()
                .map(rawList -> {
                    List<RoleUiModel> resultList = new ArrayList<>();

                    if (rawList.isEmpty()) {
                        return resultList;
                    }

                    String firstSeparator = RoleRelationship.values()[rawList.get(0).getRole().getRelationship()].getTitle();
                    resultList.add(new RoleUiModel.Separator(firstSeparator));

                    for (int i = 0; i < rawList.size() - 1; i++) {
                        RoleEntityModel currentModel = rawList.get(i);
                        RoleEntityModel nextModel = i < rawList.size() - 2 ? rawList.get(i + 1) : null;

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
}
