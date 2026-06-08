package com.wanderer.journal.data.save.db.services;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.classes.DiaryLength;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.helpers.file.FileHelper;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

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

    /**
     * 获取日记平均长度、最大长度
     *
     * @param db 数据库实例
     * @return 包含日记长度等数据的实例
     */
    public static Single<DiaryLength> getDiaryLengthData(@NonNull DiaryDatabase db) {
        ParagraphDao paragraphDao = db.paragraphDao();
        return Single.zip(
                paragraphDao.getMaxDiaryLengthSingle(),
                paragraphDao.getAverageDiaryLengthSingle(),
                DiaryLength::new
        );
    }

    /**
     * 获取匹配搜索的段落的位置
     *
     * @param keyword     搜索关键词
     * @param emotionIds  情绪标签 ID 列表（如果传入空列表，代表不限制情绪，只按关键词搜索）
     * @param filterMedia 是否需要由媒体文件
     * @param db          数据库实例
     * @return 包含所有匹配搜索位置的整数列表（已考虑日期分隔符），支持响应式更新
     */
    @NonNull
    public static Flowable<List<Integer>> getSearchMatchedParagraphPositionsFlowableInternal(
            String keyword,
            List<Long> emotionIds,
            boolean filterMedia,
            @NonNull DiaryDatabase db
    ) {
        ParagraphDao paragraphDao = db.paragraphDao();
        int useEmotionFilter = (emotionIds == null || emotionIds.isEmpty()) ? 0 : 1;
        int useContentFilter = (keyword == null || keyword.isEmpty()) ? 0 : 1;
        int useMediaFilter = filterMedia ? 1 : 0;
        return paragraphDao.getSearchMatchedParagraphPositionsFlowableInternal(
                keyword,
                emotionIds,
                useContentFilter,
                useEmotionFilter,
                useMediaFilter
        );
    }
}
