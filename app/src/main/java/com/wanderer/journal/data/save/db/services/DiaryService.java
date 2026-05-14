package com.wanderer.journal.data.save.db.services;

import android.content.Context;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
     * @param diary   需要更新日期的日记
     * @param date    更新后的日期
     * @param context 上下文
     * @return 受影响的段落数量
     */
    public static Single<Integer> updateDiaryDate(DiaryEntity diary, LocalDate date, Context context) {
        DiaryDatabase db = DiaryDatabase.getInstance(context);
        DiaryDao diaryDao = db.diaryDao();
        ParagraphDao paragraphDao = db.paragraphDao();
        return Single.defer(() -> {
            //取该日期的段落列表
            List<ParagraphEntity> paragraphEntityList = paragraphDao.getParagraphByDiaryId(diary.getDiaryId());

            //修改日记的日期并写入
            diary.setDiaryDate(date);
            diaryDao.updateDiaryDate(diary);

            //修改段落时间并写入
            for (ParagraphEntity paragraph : paragraphEntityList) {
                LocalDateTime time = paragraph.getCreateTime();
                LocalDateTime modifiedTime = time.withYear(date.getYear())
                        .withMonth(date.getMonthValue())
                        .withDayOfMonth(date.getDayOfMonth());
                paragraph.setCreateTime(modifiedTime);
            }
            paragraphDao.updateParagraph(paragraphEntityList);

            return Single.just(paragraphEntityList.size());
        });
    }
}
