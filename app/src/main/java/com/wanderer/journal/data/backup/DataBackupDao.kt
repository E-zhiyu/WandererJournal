package com.wanderer.journal.data.backup

import android.content.Context
import com.wanderer.journal.auxiliary.classes.ImportResult
import com.wanderer.journal.data.backup.maps.DiaryDataMap
import com.wanderer.journal.data.backup.maps.LifeNoteDataMap
import com.wanderer.journal.data.backup.maps.RoleDataMap
import com.wanderer.journal.data.save.db.DiaryDb
import com.wanderer.journal.data.save.db.daos.BackupDao
import java.util.Locale
import java.util.concurrent.Callable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 备份数据的导入导出器。
 *
 * 原先的 {@code DataBackupDao} 是一个带 {@code default} 方法的 Room DAO 接口，
 * 依靠 Room 的 {@code @Transaction} 保证导入导出操作的原子性。
 * 由于备份 POJO 与导入导出逻辑已迁移到 Kotlin，而项目使用的是 javac 的 Room
 * annotationProcessor（不是 KSP/KAPT），Room 无法为 Kotlin DAO 生成实现，
 * 因此原始 SQL 访问保留在 Java 的 [BackupDao] 中，本类负责事务、实体转换等编排逻辑。
 */
class DataBackupDao(private val db: DiaryDb) {
    private val dao: BackupDao by lazy { db.backupDao() }

    /**
     * 导出“日记数据”选项对应的数据。
     */
    suspend fun exportDiaryData(): DiaryDataMap = withContext(Dispatchers.IO) {
        inTransaction {
            DiaryDataMap(
                diaryList = EntityPojoMapper.toDiaryPojoList(dao.readDiaryData()),
                paragraphList = EntityPojoMapper.toParagraphPojoList(dao.readParagraphData()),
                mediaList = EntityPojoMapper.toMediaPojoList(dao.readMediaData()),
                locationList = EntityPojoMapper.toLocationPojoList(dao.readLocationData()),
                emotionTagList = EntityPojoMapper.toEmotionTagPojoList(dao.readEmotionData()),
                emotionParagraphRefList = EntityPojoMapper.toEmotionParagraphRefPojoList(
                    dao.readEmotionRefData()
                ),
            )
        }
    }

    /**
     * 导入“日记数据”选项对应的数据。
     */
    suspend fun importDiaryData(context: Context, data: DiaryDataMap?): ImportResult =
        withContext(Dispatchers.IO) {
            if (data == null) return@withContext ImportResult.missed()

            inTransaction {
                val exceptionList = mutableListOf<Throwable>()

                // 清空旧数据
                dao.clearEmotionParagraphRef()
                dao.clearEmotionTag()
                dao.clearMediaData()
                dao.clearLocationData()
                dao.clearParagraphData()
                dao.clearDiaryData()

                // 导入日记数据
                try {
                    data.diaryList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeDiaryData(EntityPojoMapper.toDiaryEntityList(it))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 导入段落数据
                try {
                    data.paragraphList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeParagraphData(EntityPojoMapper.toParagraphEntityList(it))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 导入媒体数据
                try {
                    data.mediaList?.takeIf { it.isNotEmpty() }?.let { mediaPojoList ->
                        // 替换包名为当前包名
                        val replacement = String.format(
                            Locale.getDefault(),
                            "Android/data/%s/files/",
                            context.packageName,
                        )
                        val packagePathRegex = Regex("Android/data/([^/]+)/files/")
                        val uriConvertedPojoList = mediaPojoList.map { media ->
                            media.copy(
                                fileUri = media.fileUri?.replace(packagePathRegex, replacement)
                            )
                        }

                        dao.writeMediaData(EntityPojoMapper.toMediaEntityList(uriConvertedPojoList))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 导入位置数据
                try {
                    data.locationList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeLocationData(EntityPojoMapper.toLocationEntityList(it))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 导入情绪标签数据
                try {
                    data.emotionTagList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeEmotionTagData(EntityPojoMapper.toEmotionTagEntityList(it))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 导入情绪标签与段落关系数据
                try {
                    data.emotionParagraphRefList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeEmotionParagraphRefData(
                            EntityPojoMapper.toEmotionParagraphRefEntityList(it)
                        )
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 返回导入结果
                if (exceptionList.isEmpty()) {
                    ImportResult.success()
                } else {
                    ImportResult.partlySuccess(exceptionList)
                }
            }
        }

    /**
     * 导出所有角色数据。
     */
    suspend fun exportRoleData(): RoleDataMap = withContext(Dispatchers.IO) {
        inTransaction {
            RoleDataMap(
                roleList = EntityPojoMapper.toRolePojoList(dao.readRoleData()),
                roleAliaList = EntityPojoMapper.toRoleAliaPojoList(dao.readRoleAliaData()),
            )
        }
    }

    /**
     * 导入所有角色数据。
     */
    suspend fun importRoleData(data: RoleDataMap?): ImportResult = withContext(Dispatchers.IO) {
        if (data == null) return@withContext ImportResult.missed()

        inTransaction {
            val exceptionList = mutableListOf<Throwable>()

            // 清空旧数据
            dao.clearRoleAlia()
            dao.clearRole()

            // 导入角色数据
            try {
                data.roleList?.takeIf { it.isNotEmpty() }?.let {
                    dao.writeRoleData(EntityPojoMapper.toRoleEntityList(it))
                }
            } catch (e: Exception) {
                exceptionList.add(e)
            }

            // 导入角色别名数据
            try {
                data.roleAliaList?.takeIf { it.isNotEmpty() }?.let {
                    dao.writeRoleAliaList(EntityPojoMapper.toRoleAliaEntityList(it))
                }
            } catch (e: Exception) {
                exceptionList.add(e)
            }

            // 返回导入结果
            if (exceptionList.isEmpty()) {
                ImportResult.success()
            } else {
                ImportResult.partlySuccess(exceptionList)
            }
        }
    }

    /**
     * 导出所有人生笔记数据。
     */
    suspend fun exportLifeNoteData(): LifeNoteDataMap = withContext(Dispatchers.IO) {
        inTransaction {
            LifeNoteDataMap(
                lifeNoteList = EntityPojoMapper.toLifeNotePojoList(dao.readLifeNoteData()),
                lifeNoteHistoryList = EntityPojoMapper.toLifeNoteHistoryPojoList(
                    dao.readLifeNoteHistoryData()
                ),
            )
        }
    }

    /**
     * 导入所有人生笔记数据。
     */
    suspend fun importLifeNoteData(data: LifeNoteDataMap?): ImportResult =
        withContext(Dispatchers.IO) {
            if (data == null) return@withContext ImportResult.missed()

            inTransaction {
                val exceptionList = mutableListOf<Throwable>()

                // 清空旧数据
                dao.clearLifeNote()
                dao.clearLifeNoteHistory()

                // 导入人生笔记数据
                try {
                    data.lifeNoteList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeLifeNote(EntityPojoMapper.toLifeNoteEntityList(it))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 导入人生笔记历史记录数据
                try {
                    data.lifeNoteHistoryList?.takeIf { it.isNotEmpty() }?.let {
                        dao.writeLifeNoteHistory(EntityPojoMapper.toLifeNoteHistoryEntityList(it))
                    }
                } catch (e: Exception) {
                    exceptionList.add(e)
                }

                // 返回导入结果
                if (exceptionList.isEmpty()) {
                    ImportResult.success()
                } else {
                    ImportResult.partlySuccess(exceptionList)
                }
            }
        }

    /**
     * 在数据库事务中执行 [block]。
     */
    private fun <T> inTransaction(block: () -> T): T =
        db.runInTransaction(Callable { block() })
}
