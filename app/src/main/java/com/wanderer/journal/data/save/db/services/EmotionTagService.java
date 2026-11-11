package com.wanderer.journal.data.save.db.services;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.text.EmotionType;
import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.composite.ui.EmotionListUiModel;
import com.wanderer.journal.data.save.db.entities.composite.union.UsedEmotionTagUnionModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class EmotionTagService {
    /**
     * 更新情绪标签与段落的引用关系
     *
     * @param emotionTagSelectMap 情绪标签选择信息
     * @param paragraphId         待修改情绪标签的段落编号
     * @param db                  数据库实例
     */
    public static Completable updateEmotionTagRef(Map<Long, Integer> emotionTagSelectMap, long paragraphId, DiaryDb db) {
        return Completable.defer(() -> {
            db.emotionTagDao().updateParagraphEmotionTagRef(emotionTagSelectMap, paragraphId);
            return Completable.complete();
        });
    }

    /**
     * 获取带有分隔符的情绪标签
     *
     * @param db 数据库实例
     * @return 带哟分隔符的情绪标签 Flowable 流
     */
    public static Flowable<List<EmotionListUiModel>> getAllEmotionTagWithSeparator(@NonNull DiaryDb db) {
        EmotionTagDao dao = db.emotionTagDao();
        EmotionType[] types = EmotionType.values();
        return dao.getEmotionTagUseCountModelFlowable()
                .map(emotionTagEntityList -> {
                    List<EmotionListUiModel> resultList = new ArrayList<>();
                    //判空
                    if (emotionTagEntityList.isEmpty()) return resultList;

                    //分组
                    Map<Integer, List<UsedEmotionTagUnionModel>> groupedMap = emotionTagEntityList.stream()
                            .collect(Collectors.groupingBy(
                                    model -> model.getEmotionTag().getType(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

                    //循环添加分隔符和 Item 项
                    for (Map.Entry<Integer, List<UsedEmotionTagUnionModel>> entry : groupedMap.entrySet()) {
                        String separatorText = types[entry.getKey()].getTitle();
                        resultList.add(new EmotionListUiModel.Separator(separatorText));

                        List<EmotionListUiModel.Item> itemList = entry.getValue().stream()
                                .map(EmotionListUiModel.Item::new)
                                .collect(Collectors.toList());
                        resultList.addAll(itemList);
                    }

                    return resultList;
                });
    }
}
