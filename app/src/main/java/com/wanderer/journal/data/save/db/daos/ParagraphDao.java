package com.wanderer.journal.data.save.db.daos;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import java.time.LocalDate;

import io.reactivex.rxjava3.core.Completable;

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

    /**
     * 更新段落
     *
     * @param paragraph 修改后的段落
     * @return 是否成功
     */
    @Update
    Completable updateParagraphContent(ParagraphEntity paragraph);

    /**
     * 删除段落
     * @param paragraph 待删除的段落实例
     * @return 是否成功
     */
    @Delete
    Completable deleteParagraph(ParagraphEntity paragraph);
}
