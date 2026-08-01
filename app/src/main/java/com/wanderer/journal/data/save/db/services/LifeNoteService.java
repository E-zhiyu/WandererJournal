package com.wanderer.journal.data.save.db.services;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.daos.LifeNoteDao;
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class LifeNoteService {
    /**
     * 获取所有复合搜索条件的人生笔记
     *
     * @param db      数据库实例
     * @param keyword 搜索关键词
     * @return 复合搜索条件的人生笔记列表
     */
    public static Flowable<List<LifeNoteEntity>> getAllLifeNoteFlowable(@NonNull DiaryDb db, String keyword) {
        LifeNoteDao dao = db.lifeNoteDao();
        String safeKeyword = "";

        if (keyword != null && !keyword.trim().isEmpty()) {
            safeKeyword = keyword.replace("/", "//")
                    .replace("%", "/%")
                    .replace("_", "/_");
        }

        int isSearchFilter = !safeKeyword.isEmpty() ? 1 : 0;
        return dao.getAllLifeNoteFlowable(safeKeyword, isSearchFilter);
    }

    /**
     * 更新人生笔记
     *
     * @param db          数据库实例
     * @param newLifeNote 新人生笔记数据
     * @return 是否完成
     */
    public static Completable modifyLifeNoteCompletable(@NonNull DiaryDb db, LifeNoteEntity newLifeNote) {
        LifeNoteDao dao = db.lifeNoteDao();
        return Completable.defer(() -> {
            dao.modifyLifeNote(newLifeNote);
            return Completable.complete();
        });
    }
}
