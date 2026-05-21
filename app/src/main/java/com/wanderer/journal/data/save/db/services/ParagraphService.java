package com.wanderer.journal.data.save.db.services;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public class ParagraphService {
    /**
     * 插入新日记段落并插入新添加的媒体
     *
     * @param paragraph    新段落实体
     * @param newMediaList 新媒体文件的Uri列表
     * @param db           数据库实例
     * @return {@link Completable}实例，订阅后执行段落插入逻辑
     */
    public static Completable insertParagraphWithMedia(
            ParagraphEntity paragraph,
            List<Uri> newMediaList,
            @NonNull DiaryDatabase db
    ) {
        return Completable.fromAction(() -> {
            ParagraphDao paragraphDao = db.paragraphDao();
            paragraphDao.insertParagraph(paragraph, newMediaList, db);
        });
    }

    /**
     * 更新段落并插入新添加的媒体
     *
     * @param paragraph    更新后的段落
     * @param newMediaList 新媒体文件的 Uri 列表
     * @param db           数据库实例
     * @return {@link Completable}实例，订阅后执行段落插入逻辑
     */
    public static Completable updateParagraphWithMedia(
            ParagraphEntity paragraph,
            List<Uri> newMediaList,
            DiaryDatabase db
    ) {
        return Completable.fromAction(() -> {
            ParagraphDao paragraphDao = db.paragraphDao();
            paragraphDao.updateParagraph(paragraph, newMediaList, db);
        });
    }
}
