package com.wanderer.journal.data.save.db.services;

import android.content.Context;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;

import java.time.LocalDate;

import io.reactivex.rxjava3.core.Single;

public class DiaryService {
    public static Single<Long> getOrCreateDiaryIdByDate(LocalDate date, Context context) {
        DiaryDao diaryDao = DiaryDatabase.getInstance(context).diaryDao();
        return Single.defer(() -> Single.just(diaryDao.getOrCreateDiaryIdByDate(date)));
    }
}
