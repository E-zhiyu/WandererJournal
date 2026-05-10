package com.wanderer.journal.helpers.file.backup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.backup.EntityPojoMapper;
import com.wanderer.journal.data.backup.maps.DiaryDataMap;
import com.wanderer.journal.data.backup.pojo.DiaryPojo;
import com.wanderer.journal.data.backup.pojo.MediaPojo;
import com.wanderer.journal.data.backup.pojo.ParagraphPojo;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.enums.BackupDataType;

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

        //实例化Map类
        DiaryDataMap map = new DiaryDataMap();
        map.setDiaryList(diaryPojoList);
        map.setParagraphList(paragraphPojoList);
        map.setMediaList(mediaPojoList);

        return map;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull DiaryDataMap map) {
        //导入日记数据
        List<DiaryPojo> diaryPojoList = map.getDiaryList();
        List<DiaryEntity> diaryEntityList = EntityPojoMapper.INSTANCE.toDiaryEntityList(diaryPojoList);
        db.diaryDao().clear();
        db.diaryDao().importData(diaryEntityList);

        //导入段落数据
        List<ParagraphPojo> paragraphPojoList = map.getParagraphList();
        List<ParagraphEntity> paragraphEntityList = EntityPojoMapper.INSTANCE.toParagraphEntityList(paragraphPojoList);
        db.paragraphDao().clear();
        db.paragraphDao().importData(paragraphEntityList);

        //导入媒体数据
        List<MediaPojo> mediaPojoList = map.getMediaList();
        List<MediaEntity> mediaEntityList = EntityPojoMapper.INSTANCE.toMediaEntityList(mediaPojoList);
        db.mediaDao().clear();
        db.mediaDao().importData(mediaEntityList);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.DIARY.getFileName();
    }
}
