package com.wanderer.journal.data.save.db.services;

import android.content.Context;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.DiaryParagraphCountModel;
import com.wanderer.journal.helpers.file.FileHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class DiaryService {
    /**
     * 通过日期获取日记编号，没有则创建日记
     *
     * @param date    日记日期
     * @param context 上下文
     * @return 日记编号
     */
    public static Single<Long> getOrCreateDiaryIdByDate(LocalDate date, Context context) {
        DiaryDao diaryDao = DiaryDatabase.getInstance(context).diaryDao();
        return Single.defer(() -> Single.just(diaryDao.getOrCreateDiaryIdByDate(date)));
    }

    /**
     * 更新日记日期
     *
     * @param diaryId 需要更新日期的日记
     * @param date    更新后的日期
     * @param context 上下文
     * @return 受影响的段落数量
     */
    public static Completable updateDiaryDate(long diaryId, LocalDate date, Context context) {
        DiaryDatabase db = DiaryDatabase.getInstance(context);
        ParagraphDao paragraphDao = db.paragraphDao();
        DiaryDao diaryDao = db.diaryDao();
        return Completable.defer(() -> {
            diaryDao.modifyDiaryDate(diaryId, date, paragraphDao);
            return Completable.complete();
        });
    }

    /**
     * 删除日记并删除该日记中段落的媒体文件
     *
     * @param diary   待删除的日记对象
     * @param context 上下文
     * @return {@link Completable}对象
     */
    public static Completable deleteDiaryAndParagraphMedias(
            DiaryEntity diary,
            Context context
    ) {
        return Completable.fromAction(() -> {
            DiaryDatabase db = DiaryDatabase.getInstance(context);
            ParagraphDao paragraphDao = db.paragraphDao();
            DiaryDao diaryDao = db.diaryDao();
            MediaDao mediaDao = db.mediaDao();

            //获取所有需要删除的媒体数据
            List<MediaEntity> mediasToBeDeleted = new ArrayList<>();
            List<ParagraphEntity> paragraphListInDiary = paragraphDao.getParagraphByDiaryId(diary.getDiaryId());
            for (ParagraphEntity paragraph : paragraphListInDiary) {
                mediasToBeDeleted.addAll(mediaDao.getMediaByParagraphId(paragraph.getParagraphId()));
            }

            //删除媒体文件
            for (MediaEntity media : mediasToBeDeleted) {
                FileHelper.deleteFile(media.getFileUri(), context);
            }

            //删除数据库中的记录
            diaryDao.deleteDiary(diary);
        });
    }

    /**
     * 获取记忆像素数据
     *
     * @param start 起始日期（包含）
     * @param end   截止日期（包含）
     * @return 一个{@link Flowable}实例，包含能够直接提交给适配器的数据模型列表
     */
    public static Flowable<List<DiaryParagraphCountModel>> getMemeryPixelData(
            LocalDate start,
            LocalDate end,
            DiaryDatabase db
    ) {
        return Flowable.defer(() -> {
            DiaryDao diaryDao = db.diaryDao();

            //获取有日记的天
            List<DiaryParagraphCountModel> withDiaryModelList = diaryDao.getDiaryParagraphWordCount(start, end);

            //将数据放到哈希表中
            HashMap<LocalDate, Integer> dateMap = new HashMap<>();
            for (DiaryParagraphCountModel model : withDiaryModelList) {
                dateMap.put(model.getDiaryDate(), model.getParagraphWordCount());
            }

            List<DiaryParagraphCountModel> resultList = new ArrayList<>();

            //填充头部 null 对象，使开始日期对齐（例如：星期一对齐到下标为0）
            int dayOfWeekValue = start.getDayOfWeek().getValue();
            int nullCount = dayOfWeekValue - 1;
            for (int i = 0; i < nullCount; i++) {
                resultList.add(null);
            }

            //遍历哈希表填充没有记日记的天
            int nowDayOfYear = end.getDayOfYear();
            for (int i = 0; i < nowDayOfYear - 1; i++) {
                LocalDate date = start.plusDays(i);

                Integer paragraphCount;
                if (dateMap.containsKey(date) && (paragraphCount = dateMap.get(date)) != null) {
                    resultList.add(new DiaryParagraphCountModel(date, paragraphCount));
                } else {
                    resultList.add(new DiaryParagraphCountModel(date, 0));
                }
            }

            return Flowable.just(resultList);
        });
    }
}
