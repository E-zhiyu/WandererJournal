package com.wanderer.journal.data.save.db.services;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;

import io.reactivex.rxjava3.core.Completable;

public class EmotionTagService {
    /**
     * 添加或更新情绪标签与段落的引用关系
     *
     * @param refEntity 引用关系实体
     * @param db        数据库实例
     */
    public static Completable addOrUpdateEmotionTagRef(EmotionParagraphRefEntity refEntity, DiaryDatabase db) {
        return Completable.defer(() -> {
            EmotionTagDao dao = db.emotionTagDao();
            long emotionId = refEntity.getEmotionId();
            long paragraphId = refEntity.getParagraphId();
            if (dao.isRefExists(emotionId, paragraphId)) {
                return dao.updateEmotionParagraphRefCompletable(refEntity);
            } else {
                return dao.insertEmotionParagraphRefCompletable(refEntity);
            }
        });
    }
}
