package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.BackupDataType;
import com.wanderer.journal.data.backup.EntityPojoMapper;
import com.wanderer.journal.data.backup.maps.RoleDataMap;
import com.wanderer.journal.data.backup.pojo.RoleAliaPojo;
import com.wanderer.journal.data.backup.pojo.RolePojo;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity;
import com.wanderer.journal.data.save.db.entities.RoleEntity;

import java.util.List;

public class RoleBackupHelper extends BackupHelperBase<DiaryDatabase, RoleDataMap> {
    public RoleBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<RoleDataMap> getMapClass() {
        return RoleDataMap.class;
    }

    @Override
    protected DiaryDatabase getDatabase(Context context) {
        return DiaryDatabase.getInstance(context);
    }

    @Override
    protected RoleDataMap getAllDataInMap() {
        //读取角色数据
        List<RoleEntity> roleEntityList = db.roleDao().exportRoleData();
        List<RolePojo> rolePojoList = EntityPojoMapper.INSTANCE.toRolePojoList(roleEntityList);

        //读取角色别名数据
        List<RoleAliaEntity> roleAliaEntityList = db.roleDao().exportRoleAliaData();
        List<RoleAliaPojo> roleAliaPojoList = EntityPojoMapper.INSTANCE.toRoleAliaPojoList(roleAliaEntityList);

        RoleDataMap map = new RoleDataMap();
        map.setRoleList(rolePojoList);
        map.setRoleAliaList(roleAliaPojoList);
        return map;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull RoleDataMap map) {
        //导入角色数据
        List<RolePojo> rolePojoList = map.getRoleList();
        db.roleDao().clearRole();
        if (rolePojoList != null && !rolePojoList.isEmpty()) {
            List<RoleEntity> roleEntityList = EntityPojoMapper.INSTANCE.toRoleEntityList(rolePojoList);
            db.roleDao().importRoleData(roleEntityList);
        }

        //导入角色别名数据
        List<RoleAliaPojo> roleAliaPojoList = map.getRoleAliaList();
        db.roleDao().clearRoleAlia();
        if (roleAliaPojoList != null && !roleAliaPojoList.isEmpty()) {
            List<RoleAliaEntity> roleAliaEntityList = EntityPojoMapper.INSTANCE.toRoleAliaEntityList(roleAliaPojoList);
            db.roleDao().importRoleAliaList(roleAliaEntityList);
        }
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.ROLE.getFileName();
    }
}
