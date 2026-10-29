package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderer.journal.auxiliary.classes.ImportResult;
import com.wanderer.journal.auxiliary.enums.types.ImportDataVersionType;
import com.wanderer.journal.data.backup.maps.DiaryDataMap;
import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.auxiliary.enums.types.BackupDataType;

public class DiaryBackupHelper extends BackupHelperBase<DiaryDb, DiaryDataMap> {
    public DiaryBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<DiaryDataMap> getMapClass() {
        return DiaryDataMap.class;
    }

    @Override
    protected DiaryDb getDatabase(Context context) {
        return DiaryDb.getInstance(context);
    }

    @Override
    protected DiaryDataMap getAllDataInMap() {
        return db.dataBackupDao().exportDiaryData();
    }

    @Override
    protected ImportResult saveDataInMapToDb(Context context, @NonNull DiaryDataMap map) {
        return db.dataBackupDao().importDiaryData(context, map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.DIARY.getLatestVersionFileName();
    }

    @Override
    protected DiaryDataMap getDataMap(String json, ImportDataVersionType versionType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, mapClass);
    }
}
