package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderer.journal.auxiliary.classes.ImportResult;
import com.wanderer.journal.auxiliary.enums.types.BackupDataType;
import com.wanderer.journal.auxiliary.enums.types.ImportDataVersionType;
import com.wanderer.journal.data.backup.maps.LifeNoteDataMap;
import com.wanderer.journal.data.save.db.DiaryDb;

public class LifeNoteBackupHelper extends BackupHelperBase<DiaryDb, LifeNoteDataMap> {
    public LifeNoteBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<LifeNoteDataMap> getMapClass() {
        return LifeNoteDataMap.class;
    }

    @Override
    protected DiaryDb getDatabase(Context context) {
        return DiaryDb.getInstance(context);
    }

    @Override
    protected LifeNoteDataMap getAllDataInMap() {
        return db.dataBackupDao().exportLifeNoteData();
    }

    @Override
    protected ImportResult saveDataInMapToDb(Context context, LifeNoteDataMap map) {
        return db.dataBackupDao().importLifeNoteData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.LIFE_NOTE.getLatestVersionFileName();
    }

    @Override
    protected LifeNoteDataMap getDataMap(String json, ImportDataVersionType versionType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, mapClass);
    }
}
