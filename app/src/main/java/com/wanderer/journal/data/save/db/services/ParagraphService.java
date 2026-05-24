package com.wanderer.journal.data.save.db.services;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.helpers.file.FileHelper;

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
            @NonNull ParagraphEntity paragraph,
            List<Uri> newMediaList,
            @NonNull DiaryDatabase db
    ) {
        paragraph.setContent(paragraph.getContent().trim());
        return Completable.fromAction(() -> {
            if (!paragraph.getContent().isEmpty()) {
                ParagraphDao paragraphDao = db.paragraphDao();
                paragraphDao.insertParagraph(paragraph, newMediaList, db);
            }
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

    /**
     * 删除段落的同时删除其绑定的媒体文件
     *
     * @param paragraph 待删除的段落
     * @param context   上下文
     * @return {@link Completable}实例，订阅后执行段落删除逻辑
     */
    public static Completable deleteParagraphAndMedia(
            ParagraphEntity paragraph,
            Context context
    ) {
        return Completable.fromAction(() -> {
            DiaryDatabase db = DiaryDatabase.getInstance(context);
            MediaDao mediaDao = db.mediaDao();
            ParagraphDao paragraphDao = db.paragraphDao();

            //删除媒体文件
            List<MediaEntity> mediasToBeDeleted = mediaDao.getMediaByParagraphId(paragraph.getParagraphId());
            for (MediaEntity media : mediasToBeDeleted) {
                Uri mediaUri = media.getFileUri();
                FileHelper.deleteFile(mediaUri, context);
            }

            //删除数据库中的记录
            paragraphDao.deleteParagraph(paragraph);
        });
    }
}
