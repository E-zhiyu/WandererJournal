package com.wanderer.journal.data.save.db.daos;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

@Dao
public interface ParagraphDao {
    /**
     * 读取所有段落并支持局部加载
     *
     * @return 可局部加载的日记段落列表
     */
    @Query("SELECT * FROM paragraphs ORDER BY createTime")
    PagingSource<Integer, ParagraphEntity> getAllParagraphPagingSource();
}
