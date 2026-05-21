package com.wanderer.journal.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.MediaEntity;

import java.util.List;

@Dao
public interface MediaDao {
    /**
     * 插入媒体
     *
     * @param mediaEntityList 新媒体实例列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMedia(List<MediaEntity> mediaEntityList);

    /**
     * 读取所有数据用于导出
     *
     * @return 媒体实体列表
     */
    @Query("SELECT * FROM medias")
    List<MediaEntity> exportData();

    /**
     * 删除所有数据，准备导入新数据
     */
    @Query("DELETE FROM medias")
    void clear();

    /**
     * 导入新数据
     *
     * @param mediaEntityList 新数据实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importData(List<MediaEntity> mediaEntityList);
}
