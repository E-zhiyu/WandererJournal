package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import com.wanderer.journal.auxiliary.enums.BackupDataType;
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
    protected void saveDataInMapToDb(LifeNoteDataMap map) {
        db.dataBackupDao().importLifeNoteData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.LIFE_NOTE.getFileName();
    }
}
