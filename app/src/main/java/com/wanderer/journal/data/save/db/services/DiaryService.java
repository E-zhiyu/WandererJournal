package com.wanderer.journal.data.save.db.services;

import android.content.Context;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;

import java.time.LocalDate;

import io.reactivex.rxjava3.core.Completable;
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
}
