package com.wanderer.journal.auxiliary.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.RoleEntityModel;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.pages.role.RoleInputActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoleShower {
    public static void showRoleDetail(Context context, CompositeDisposable disposable, long roleId) {
        if (roleId <= 0) {
            Toast.makeText(context, "该角色已失效", Toast.LENGTH_SHORT).show();
            return;
        }

        RoleDao roleDao = DiaryDatabase.getInstance(context).roleDao();
        disposable.add(roleDao.getRoleAndAliasSingleById(roleId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleEntityModelMaybe -> {
                            if (roleEntityModelMaybe.isEmpty()) {
                                Toast.makeText(context,"无法读取该角色的信息",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            RoleEntityModel model = roleEntityModelMaybe.get();

                            //解析数据
                            RoleEntity role = model.getRole();
                            String roleName = role.getName();
                            String identity = role.getIdentity();
                            String impression = role.getImpression();
                            int relationship = role.getRelationship();
                            String[] alias = model.getRoleAliaList().stream()
                                    .map(RoleAliaEntity::getAlia)
                                    .toArray(String[]::new);

                            //生成数据包
                            Bundle bundle = new Bundle();
                            bundle.putLong(KeyStrings.ROLE_ID.getS(), roleId);
                            bundle.putString(KeyStrings.ROLE_NAME.getS(), roleName);
                            bundle.putString(KeyStrings.ROLE_IDENTITY.getS(), identity);
                            bundle.putString(KeyStrings.ROLE_IMPRESSION.getS(), impression);
                            bundle.putInt(KeyStrings.ROLE_RELATIONSHIP.getS(), relationship);
                            bundle.putStringArray(KeyStrings.ROLE_ALIAS.getS(), alias);

                            //跳转界面
                            Intent skip2RoleInput = new Intent(context, RoleInputActivity.class);
                            skip2RoleInput.putExtras(bundle);
                            context.startActivity(skip2RoleInput);
                        },
                        e -> ExceptionHelper.showExceptionDialog(context, e)
                )
        );
    }
}
