package com.wanderer.journal.helpers.file.backup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.backup.EntityPojoMapper;
import com.wanderer.journal.data.backup.maps.DiaryDataMap;
import com.wanderer.journal.data.backup.pojo.DiaryPojo;
import com.wanderer.journal.data.backup.pojo.EmotionParagraphRefPojo;
import com.wanderer.journal.data.backup.pojo.EmotionTagPojo;
import com.wanderer.journal.data.backup.pojo.MediaPojo;
import com.wanderer.journal.data.backup.pojo.ParagraphPojo;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.auxiliary.enums.BackupDataType;

import java.util.List;

public class DiaryBackupHelper extends BackupHelperBase<DiaryDatabase, DiaryDataMap> {
    public DiaryBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<DiaryDataMap> getMapClass() {
        return DiaryDataMap.class;
    }

    @Override
    protected DiaryDatabase createHelper(Context context) {
        return DiaryDatabase.getInstance(context);
    }

    @Override
    protected DiaryDataMap getAllDataInMap() {
        //读取日记数据
        List<DiaryEntity> diaryEntityList = db.diaryDao().exportData();
        List<DiaryPojo> diaryPojoList = EntityPojoMapper.INSTANCE.toDiaryPojoList(diaryEntityList);

        //读取段落数据
        List<ParagraphEntity> paragraphEntityList = db.paragraphDao().exportData();
        List<ParagraphPojo> paragraphPojoList = EntityPojoMapper.INSTANCE.toParagraphPojoList(paragraphEntityList);

        //读取媒体数据
        List<MediaEntity> mediaEntityList = db.mediaDao().exportData();
        List<MediaPojo> mediaPojoList = EntityPojoMapper.INSTANCE.toMediaPojoList(mediaEntityList);

        //读取情绪标签数据
        List<EmotionTagEntity> emotionTagWithParagraphList = db.emotionTagDao().exportEmotionData();
        List<EmotionTagPojo> emotionTagPojoList =
                EntityPojoMapper.INSTANCE.toEmotionTagPojoList(emotionTagWithParagraphList);

        //读取情绪标签与段落关系数据
        List<EmotionParagraphRefEntity> emotionParagraphRefEntityList = db.emotionTagDao().exportEmotionRefData();
        List<EmotionParagraphRefPojo> emotionParagraphRefPojoList =
                EntityPojoMapper.INSTANCE.toEmotionParagraphRefPojoList(emotionParagraphRefEntityList);

        //实例化Map类
        DiaryDataMap map = new DiaryDataMap();
        map.setDiaryList(diaryPojoList);
        map.setParagraphList(paragraphPojoList);
        map.setMediaList(mediaPojoList);
        map.setEmotionTagList(emotionTagPojoList);
        map.setEmotionParagraphRefList(emotionParagraphRefPojoList);

        return map;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull DiaryDataMap map) {
        //导入日记数据
        List<DiaryPojo> diaryPojoList = map.getDiaryList();
        db.diaryDao().clear();
        if (diaryPojoList != null && !diaryPojoList.isEmpty()) {
            List<DiaryEntity> diaryEntityList = EntityPojoMapper.INSTANCE.toDiaryEntityList(diaryPojoList);
            db.diaryDao().importData(diaryEntityList);
        }

        //导入段落数据
        List<ParagraphPojo> paragraphPojoList = map.getParagraphList();
        db.paragraphDao().clear();
        if (paragraphPojoList != null && !paragraphPojoList.isEmpty()) {
            List<ParagraphEntity> paragraphEntityList = EntityPojoMapper.INSTANCE.toParagraphEntityList(paragraphPojoList);
            db.paragraphDao().importData(paragraphEntityList);
        }

        //导入媒体数据
        List<MediaPojo> mediaPojoList = map.getMediaList();
        db.mediaDao().clear();
        if (mediaPojoList != null && !mediaPojoList.isEmpty()) {
            List<MediaEntity> mediaEntityList = EntityPojoMapper.INSTANCE.toMediaEntityList(mediaPojoList);
            db.mediaDao().importData(mediaEntityList);
        }

        //导入情绪标签数据
        List<EmotionTagPojo> emotionTagPojoList = map.getEmotionTagList();
        db.emotionTagDao().clearEmotionTag();
        if (emotionTagPojoList != null && !emotionTagPojoList.isEmpty()) {
            List<EmotionTagEntity> emotionTagWithParagraphList =
                    EntityPojoMapper.INSTANCE.toEmotionTagEntityList(emotionTagPojoList);
            db.emotionTagDao().importEmotionTagData(emotionTagWithParagraphList);
        }

        //导入情绪标签与段落关系数据
        List<EmotionParagraphRefPojo> emotionParagraphRefPojoList = map.getEmotionParagraphRefList();
        db.emotionTagDao().clearEmotionParagraphRef();
        if (emotionParagraphRefPojoList != null && !emotionParagraphRefPojoList.isEmpty()) {
            List<EmotionParagraphRefEntity> emotionParagraphRefEntityList =
                    EntityPojoMapper.INSTANCE.toEmotionParagraphRefEntityList(emotionParagraphRefPojoList);
            db.emotionTagDao().importEmotionParagraphRefData(emotionParagraphRefEntityList);
        }
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.DIARY.getFileName();
    }
}
