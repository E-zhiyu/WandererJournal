package com.wanderer.journal.auxiliary.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.RoleDao;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.dialogs.MarkdownDialogBuilder;
import com.wanderer.journal.ui.pages.role.RoleInputActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 显示各种信息的帮助器
 */
public class InfoShower {
    /**
     * 显示角色信息
     *
     * @param context    上下文
     * @param disposable 任务订阅列表
     * @param roleId     角色 ID
     */
    public static void showRole(Context context, CompositeDisposable disposable, long roleId) {
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

    /**
     * 显示人生笔记修改历史记录
     *
     * @param context       上下文
     * @param historyEntity 需要展示的人生笔记修改历史记录
     */
    public static void showLifeNoteHistory(@NonNull Context context, @NonNull LifeNoteHistoryEntity historyEntity) {
        //解析数据
        String insight = historyEntity.getInsight();
        String elaboration = historyEntity.getElaboration();
        LocalDateTime updateDateTime = historyEntity.getUpdateDateTime();

        //生成 Markdown 文本
        StringBuilder builder = new StringBuilder();

        //插入洞见
        builder.append("**").append(context.getString(R.string.life_note_insight)).append(":** ");
        builder.append(insight);
        builder.append("\n\n");

        //插入阐述
        builder.append("**").append(context.getString(R.string.life_note_elaboration)).append(":** ");
        builder.append(elaboration.isEmpty() ? "<无阐述>" : elaboration);
        builder.append("\n\n");

        //插入时间
        builder.append("**").append(context.getString(R.string.time)).append(":** ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        builder.append(updateDateTime.format(formatter));
        builder.append("\n\n");

        //显示对话框
        MarkdownDialogBuilder dialogBuilder = new MarkdownDialogBuilder(context, "修改历史", builder.toString());
        dialogBuilder
                .setNegativeButton("关闭", null)
                .show();
    }
}
