package com.wanderer.journal.data.save.db.services;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.wanderer.journal.auxiliary.classes.DiaryLength;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.MediaDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.helpers.file.FileHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ParagraphService {
    /**
     * 插入新日记段落并插入新添加的媒体
     *
     * @param startDate    写日记界面的起始日期
     * @param paragraph    新段落实体
     * @param newMediaList 新媒体文件的Uri列表
     * @param db           数据库实例
     * @return {@link Completable}实例，订阅后执行段落插入逻辑
     */
    public static Single<Integer> insertParagraphWithMedia(
            LocalDate startDate,
            @NonNull ParagraphEntity paragraph,
            List<Uri> newMediaList,
            @NonNull DiaryDatabase db
    ) {
        paragraph.setContent(paragraph.getContent().trim());
        return Single.fromCallable(() -> {
            if (!paragraph.getContent().isEmpty()) {
                ParagraphDao paragraphDao = db.paragraphDao();
                return paragraphDao.insertParagraph(startDate, paragraph, newMediaList, db);
            } else {
                return -1;
            }
        });
    }

    /**
     * 更新段落并插入新添加的媒体
     *
     * @param paragraph    更新后的段落
     * @param newMediaList 新媒体文件的 Uri 列表
     * @param db           数据库实例
     * @return {@link Completable}实例，订阅后执行段落插入逻辑
     */
    public static Completable updateParagraphWithMedia(
            ParagraphEntity paragraph,
            List<Uri> newMediaList,
            DiaryDatabase db
    ) {
        return Completable.fromAction(() -> {
            ParagraphDao paragraphDao = db.paragraphDao();
            paragraphDao.updateParagraph(paragraph, newMediaList, db);
        });
    }

    /**
     * 删除段落的同时删除其绑定的媒体文件
     *
     * @param paragraph 待删除的段落
     * @param context   上下文
     * @return {@link Completable}实例，订阅后执行段落删除逻辑
     */
    public static Completable deleteParagraphAndMedia(
            ParagraphEntity paragraph,
            Context context
    ) {
        return Completable.fromAction(() -> {
            DiaryDatabase db = DiaryDatabase.getInstance(context);
            MediaDao mediaDao = db.mediaDao();
            ParagraphDao paragraphDao = db.paragraphDao();

            //删除媒体文件
            List<MediaEntity> mediasToBeDeleted = mediaDao.getMediaByParagraphId(paragraph.getParagraphId());
            for (MediaEntity media : mediasToBeDeleted) {
                Uri mediaUri = media.getFileUri();
                FileHelper.deleteFile(mediaUri, context);
            }

            //删除数据库中的记录
            paragraphDao.deleteParagraph(paragraph);
        });
    }

    /**
     * 获取日记平均长度、最大长度
     *
     * @param db 数据库实例
     * @return 包含日记长度等数据的实例
     */
    public static Single<DiaryLength> getDiaryLengthData(@NonNull DiaryDatabase db) {
        ParagraphDao paragraphDao = db.paragraphDao();
        return Single.zip(
                paragraphDao.getMaxDiaryLengthSingle(),
                paragraphDao.getAverageDiaryLengthSingle(),
                DiaryLength::new
        );
    }

    /**
     * 获取匹配搜索的段落的位置
     *
     * @param keywordList    搜索关键词列表
     * @param emotionIds     情绪标签 ID 集合（如果传入空集合，代表不限制情绪，只按关键词搜索）
     * @param useMediaFilter 是否需要由媒体文件
     * @param db             数据库实例
     * @param isAndMode      多词搜索模式是否为“与”模式
     * @return 包含所有匹配搜索位置的整数列表（已考虑日期分隔符），支持响应式更新
     */
    @NonNull
    public static Flowable<List<Integer>> getSearchMatchedParagraphPositionsFlowableInternal(
            List<String> keywordList,
            Set<Long> emotionIds,
            boolean useMediaFilter,
            @NonNull DiaryDatabase db,
            boolean isAndMode
    ) {
        ParagraphDao paragraphDao = db.paragraphDao();
        // 1. 基础 SQL 骨架（保留你原本完美的位置计算逻辑）
        StringBuilder sql = new StringBuilder(
                "SELECT (pure_paragraph_position + date_separator_count) FROM (" +
                        "    SELECT " +
                        "        paragraphId, " +
                        "        content, " +
                        "        (ROW_NUMBER() OVER(ORDER BY createTime ASC) - 1) AS pure_paragraph_position," +
                        "        (SELECT COUNT(*) FROM diaries d_sub WHERE d_sub.diaryDate <= d.diaryDate) AS date_separator_count" +
                        "    FROM paragraphs " +
                        "    INNER JOIN diaries d ON parentDiaryId = d.diaryId" +
                        ") WHERE 1=1 " // 1=1 是为了方便后面直接拼接 AND
        );

        List<Object> args = getSearchArgs(keywordList, emotionIds, sql, isAndMode);

        // 4. 动态拼接媒体过滤
        if (useMediaFilter) {
            sql.append(" AND paragraphId IN (SELECT parentParagraphId FROM medias)");
        }

        // 5. 封装成 Room 需要的 SimpleSQLiteQuery 对象
        SimpleSQLiteQuery rawQuery = new SimpleSQLiteQuery(sql.toString(), args.toArray());

        // 6. 调用 DAO 返回响应式 Flowable
        return paragraphDao.getSearchMatchedParagraphPositionsRaw(rawQuery);
    }

    /**
     * 获取搜索参数
     *
     * @param keywordList 搜索关键词列表
     * @param emotionIds  情绪标签 ID 集合
     * @param sql         SQL 语句构建器
     * @param isAndMode   多词搜索模式是否为“与”模式
     * @return 匹配 SQL 中参数通配符的参数列表
     */
    @NonNull
    private static List<Object> getSearchArgs(List<String> keywordList, Set<Long> emotionIds, StringBuilder sql, boolean isAndMode) {
        List<Object> args = new ArrayList<>();

        // 2. 动态拼接多词内容过滤 (LIKE 方案)
        boolean useContentFilter = keywordList != null && !keywordList.isEmpty();
        if (useContentFilter) {
            sql.append(" AND (");   //用括号闭合内容搜索
            String joiner = isAndMode ? " AND " : " OR ";

            // 按空格拆分出多个关键词
            int index = 0;
            for (String keyword : keywordList) {
                if (!keyword.isEmpty()) {
                    // 转义处理，防止用户输入 % 或 _ 导致逻辑崩溃
                    String safeKeyword = keyword.replace("/", "//").replace("%", "/%").replace("_", "/_");

                    // 每一个关键词都生成一个独立的 AND LIKE 条件，确保“同时包含”
                    sql.append(" content LIKE ? ESCAPE '/'");
                    args.add("%" + safeKeyword + "%");

                    if (index < keywordList.size() - 1) {
                        sql.append(joiner);
                    }
                }

                index++;
            }

            sql.append(")");
        }

        // 3. 动态拼接情绪标签过滤
        boolean useEmotionFilter = emotionIds != null && !emotionIds.isEmpty();
        if (useEmotionFilter) {
            sql.append(" AND paragraphId IN (SELECT paragraphId FROM emotionParagraphCrossRef WHERE emotionId IN (");
            int i = 0;
            for (long emotionId : emotionIds) {
                sql.append("?");
                args.add(emotionId);
                if (i < emotionIds.size() - 1) sql.append(",");
                i++;
            }
            sql.append("))");
        }
        return args;
    }
}
