package com.wanderer.journal.data.save.db.services;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.text.EmotionType;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.EmotionListUiModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

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

    /**
     * 获取带有分隔符的情绪标签
     *
     * @param db 数据库实例
     * @return 带哟分隔符的情绪标签 Flowable 流
     */
    public static Flowable<List<EmotionListUiModel>> getAllEmotionTagWithSeparator(@NonNull DiaryDatabase db) {
        EmotionTagDao dao = db.emotionTagDao();
        return Flowable.defer(() -> {
            List<EmotionTagEntity> emotionTagEntityList = dao.getAllEmotionTag();

            //判空
            if (emotionTagEntityList.isEmpty()) return Flowable.just(Collections.emptyList());

            //分组
            Map<Integer, List<EmotionTagEntity>> groupedMap = emotionTagEntityList.stream()
                    .collect(Collectors.groupingBy(
                            EmotionTagEntity::getType,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            //循环添加分隔符和 Item 项
            List<EmotionListUiModel> resultList = new ArrayList<>();
            for (Map.Entry<Integer, List<EmotionTagEntity>> entry : groupedMap.entrySet()) {
                String separatorText = EmotionType.values()[entry.getKey()].getTitle();
                resultList.add(new EmotionListUiModel.Separator(separatorText));

                List<EmotionListUiModel.Item> itemList = entry.getValue().stream()
                        .map(EmotionListUiModel.Item::new)
                        .collect(Collectors.toList());
                resultList.addAll(itemList);
            }

            return Flowable.just(resultList);
        });
    }
}
