package com.wanderer.journal.data.save.db.daos;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.time.LocalDate;

@Dao
public interface ParagraphDao {
    /**
     * 读取所有段落并支持局部加载
     *
     * @return 可局部加载的日记段落列表
     */
    @Query("SELECT * FROM paragraphs ORDER BY createTime")
    PagingSource<Integer, ParagraphEntity> getAllParagraphPagingSource();

    /**
     * 查询某个日期范围内的
     *
     * @param start 起始日期
     * @param end   结束日期（不包含）
     * @return 在日期范围内的按照日期顺序排序的日记段落分页列表
     */
    @Query("SELECT * FROM paragraphs WHERE createTime >= :start AND createTime < :end ORDER BY createTime")
    PagingSource<Integer, ParagraphEntity> getParagraphPagingSourceInRange(LocalDate start, LocalDate end);

    /**
     * 插入一条日记段落
     *
     * @param paragraph 日记段落实例
     * @return 插入的日记段落自动分配的编号
     */
    @Insert
    Long insertParagraph(ParagraphEntity paragraph);
}
