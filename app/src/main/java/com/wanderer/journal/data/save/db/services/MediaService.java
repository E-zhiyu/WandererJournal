package com.wanderer.journal.data.save.db.services;

import android.content.Context;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.entities.MediaEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public class MediaService {
    /**
     * 同时删除数据库和文件系统中的媒体文件
     *
     * @param mediaList 需要删除的媒体列表
     * @param context   数据库实例
     * @return 是否完成
     */
    public static Completable deleteMedia(List<MediaEntity> mediaList, Context context) {
        MediaDao mediaDao = DiaryDatabase.getInstance(context).mediaDao();
        return Completable.fromAction(() -> mediaDao.deleteMediasAndFiles(mediaList, context));
    }
}
