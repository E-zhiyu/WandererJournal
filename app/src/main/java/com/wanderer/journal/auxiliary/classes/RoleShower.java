package com.wanderer.journal.auxiliary.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.RoleDao;
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
        disposable.add(roleDao.isRoleIdExists(roleId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        isExists -> {
                            if (!isExists) {
                                Toast.makeText(context, "无法读取该角色的信息", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //生成数据包
                            Bundle bundle = new Bundle();
                            bundle.putLong(KeyStrings.ROLE_ID.getS(), roleId);

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
