package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import com.wanderer.journal.auxiliary.enums.BackupDataType;
import com.wanderer.journal.data.backup.maps.LifeNoteDataMap;
import com.wanderer.journal.data.save.db.DiaryDatabase;

public class LifeNoteBackupHelper extends BackupHelperBase<DiaryDatabase, LifeNoteDataMap> {
    public LifeNoteBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<LifeNoteDataMap> getMapClass() {
        return LifeNoteDataMap.class;
    }

    @Override
    protected DiaryDatabase getDatabase(Context context) {
        return DiaryDatabase.getInstance(context);
    }

    @Override
    protected LifeNoteDataMap getAllDataInMap() {
        return db.dataBackupDao().exportLifeNoteData();
    }

    @Override
    protected void saveDataInMapToDb(LifeNoteDataMap map) {
        db.dataBackupDao().importLifeNoteData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.LIFE_NOTE.getFileName();
    }
}
