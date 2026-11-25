package com.wanderer.journal.data.backup

import com.wanderer.journal.data.backup.pojo.DiaryPojo
import com.wanderer.journal.data.backup.pojo.EmotionParagraphRefPojo
import com.wanderer.journal.data.backup.pojo.EmotionTagPojo
import com.wanderer.journal.data.backup.pojo.LifeNoteHistoryPojo
import com.wanderer.journal.data.backup.pojo.LifeNotePojo
import com.wanderer.journal.data.backup.pojo.LocationPojo
import com.wanderer.journal.data.backup.pojo.MediaPojo
import com.wanderer.journal.data.backup.pojo.ParagraphPojo
import com.wanderer.journal.data.backup.pojo.RoleAliaPojo
import com.wanderer.journal.data.backup.pojo.RolePojo
import com.wanderer.journal.data.save.db.TypeConverter
import com.wanderer.journal.data.save.db.entities.DiaryEntity
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity
import com.wanderer.journal.data.save.db.entities.LocationEntity
import com.wanderer.journal.data.save.db.entities.MediaEntity
import com.wanderer.journal.data.save.db.entities.ParagraphEntity
import com.wanderer.journal.data.save.db.entities.RoleAliaEntity
import com.wanderer.journal.data.save.db.entities.RoleEntity

// TODO:如果能直接用数据库实体作为备份 POJO 并实现 Uri、LocalDate 等类型的转换逻辑，则考虑将所有 POJO 类删除，改为使用可序列化的数据库实体类

/**
 * 备份 POJO 与数据库实体之间的转换器。
 *
 * 原实现使用 MapStruct 自动生成转换代码；由于备份 POJO 已改为 Kotlin 数据类，
 * 这里改为显式的手写映射，避免继续依赖 Java 注解处理器，同时保持原有的时间戳、Uri 转换规则。
 */
object EntityPojoMapper {

    // ==================== 日记 ====================

    fun toDiaryEntity(pojo: DiaryPojo): DiaryEntity =
        DiaryEntity(TypeConverter.toLocalDate(pojo.diaryDate)).apply {
            diaryId = pojo.diaryId
        }

    fun toDiaryEntityList(pojoList: List<DiaryPojo>): List<DiaryEntity> =
        pojoList.map(::toDiaryEntity)

    fun toDiaryPojo(entity: DiaryEntity): DiaryPojo =
        DiaryPojo(
            diaryId = entity.diaryId,
            diaryDate = TypeConverter.fromLocalDate(entity.diaryDate) ?: 0L,
        )

    fun toDiaryPojoList(entityList: List<DiaryEntity>): List<DiaryPojo> =
        entityList.map(::toDiaryPojo)

    // ==================== 段落 ====================

    fun toParagraphEntity(pojo: ParagraphPojo): ParagraphEntity =
        ParagraphEntity(
            pojo.parentDiaryId,
            pojo.content,
            TypeConverter.toLocalDateTime(pojo.createTime),
        ).apply {
            paragraphId = pojo.paragraphId
            privacyType = pojo.privacyType
        }

    fun toParagraphEntityList(pojoList: List<ParagraphPojo>): List<ParagraphEntity> =
        pojoList.map(::toParagraphEntity)

    fun toParagraphPojo(entity: ParagraphEntity): ParagraphPojo =
        ParagraphPojo(
            paragraphId = entity.paragraphId,
            parentDiaryId = entity.parentDiaryId,
            createTime = TypeConverter.fromLocalDateTime(entity.createTime) ?: 0L,
            content = entity.content,
            privacyType = entity.privacyType,
        )

    fun toParagraphPojoList(entityList: List<ParagraphEntity>): List<ParagraphPojo> =
        entityList.map(::toParagraphPojo)

    // ==================== 媒体 ====================

    fun toMediaEntity(pojo: MediaPojo): MediaEntity =
        MediaEntity(
            pojo.parentParagraphId,
            TypeConverter.toUri(pojo.fileUri),
        ).apply {
            mediaId = pojo.mediaId
        }

    fun toMediaEntityList(pojoList: List<MediaPojo>): List<MediaEntity> =
        pojoList.map(::toMediaEntity)

    fun toMediaPojo(entity: MediaEntity): MediaPojo =
        MediaPojo(
            mediaId = entity.mediaId,
            parentParagraphId = entity.parentParagraphId,
            fileUri = TypeConverter.fromUri(entity.fileUri),
        )

    fun toMediaPojoList(entityList: List<MediaEntity>): List<MediaPojo> =
        entityList.map(::toMediaPojo)

    // ==================== 情绪标签 ====================

    fun toEmotionTagEntity(pojo: EmotionTagPojo): EmotionTagEntity =
        EmotionTagEntity(
            pojo.name,
            pojo.description,
            pojo.type,
        ).apply {
            emotionId = pojo.emotionId
        }

    fun toEmotionTagEntityList(pojoList: List<EmotionTagPojo>): List<EmotionTagEntity> =
        pojoList.map(::toEmotionTagEntity)

    fun toEmotionTagPojo(entity: EmotionTagEntity): EmotionTagPojo =
        EmotionTagPojo(
            emotionId = entity.emotionId,
            name = entity.name,
            description = entity.description,
            type = entity.type,
        )

    fun toEmotionTagPojoList(entityList: List<EmotionTagEntity>): List<EmotionTagPojo> =
        entityList.map(::toEmotionTagPojo)

    // ==================== 情绪标签与段落关系 ====================

    fun toEmotionParagraphRefEntity(pojo: EmotionParagraphRefPojo): EmotionParagraphRefEntity =
        EmotionParagraphRefEntity(
            pojo.emotionId,
            pojo.paragraphId,
            pojo.degree,
        )

    fun toEmotionParagraphRefEntityList(
        pojoList: List<EmotionParagraphRefPojo>,
    ): List<EmotionParagraphRefEntity> = pojoList.map(::toEmotionParagraphRefEntity)

    fun toEmotionParagraphRefPojo(entity: EmotionParagraphRefEntity): EmotionParagraphRefPojo =
        EmotionParagraphRefPojo(
            paragraphId = entity.paragraphId,
            emotionId = entity.emotionId,
            degree = entity.degree,
        )

    fun toEmotionParagraphRefPojoList(
        entityList: List<EmotionParagraphRefEntity>,
    ): List<EmotionParagraphRefPojo> = entityList.map(::toEmotionParagraphRefPojo)

    // ==================== 角色 ====================

    fun toRoleEntity(pojo: RolePojo): RoleEntity =
        RoleEntity(
            pojo.name,
            pojo.displayName,
            pojo.identity,
            pojo.impression,
            pojo.relationship,
        ).apply {
            roleId = pojo.roleId
            useCount = pojo.useCount
            latestUseTime = TypeConverter.toLocalDateTime(pojo.latestUseTime)
        }

    fun toRoleEntityList(pojoList: List<RolePojo>): List<RoleEntity> =
        pojoList.map(::toRoleEntity)

    fun toRolePojo(entity: RoleEntity): RolePojo =
        RolePojo(
            roleId = entity.roleId,
            name = entity.name,
            displayName = entity.displayName ?: "",
            identity = entity.identity,
            impression = entity.impression,
            relationship = entity.relationship,
            useCount = entity.useCount,
            latestUseTime = TypeConverter.fromLocalDateTime(entity.latestUseTime) ?: 0L,
        )

    fun toRolePojoList(entityList: List<RoleEntity>): List<RolePojo> =
        entityList.map(::toRolePojo)

    // ==================== 角色别名 ====================

    fun toRoleAliaEntity(pojo: RoleAliaPojo): RoleAliaEntity =
        RoleAliaEntity(
            pojo.alia,
            pojo.roleId,
        ).apply {
            aliaId = pojo.aliaId
        }

    fun toRoleAliaEntityList(pojoList: List<RoleAliaPojo>): List<RoleAliaEntity> =
        pojoList.map(::toRoleAliaEntity)

    fun toRoleAliaPojo(entity: RoleAliaEntity): RoleAliaPojo =
        RoleAliaPojo(
            aliaId = entity.aliaId,
            roleId = entity.roleId,
            alia = entity.alia,
        )

    fun toRoleAliaPojoList(entityList: List<RoleAliaEntity>): List<RoleAliaPojo> =
        entityList.map(::toRoleAliaPojo)

    // ==================== 人生笔记 ====================

    fun toLifeNoteEntity(pojo: LifeNotePojo): LifeNoteEntity =
        LifeNoteEntity(
            pojo.insight,
            pojo.elaboration,
            TypeConverter.toLocalDateTime(pojo.dateTime),
        ).apply {
            noteId = pojo.noteId
        }

    fun toLifeNoteEntityList(pojoList: List<LifeNotePojo>): List<LifeNoteEntity> =
        pojoList.map(::toLifeNoteEntity)

    fun toLifeNotePojo(entity: LifeNoteEntity): LifeNotePojo =
        LifeNotePojo(
            noteId = entity.noteId,
            insight = entity.insight,
            elaboration = entity.elaboration,
            dateTime = TypeConverter.fromLocalDateTime(entity.dateTime) ?: 0L,
        )

    fun toLifeNotePojoList(entityList: List<LifeNoteEntity>): List<LifeNotePojo> =
        entityList.map(::toLifeNotePojo)

    // ==================== 人生笔记历史 ====================

    fun toLifeNoteHistoryEntity(pojo: LifeNoteHistoryPojo): LifeNoteHistoryEntity =
        LifeNoteHistoryEntity(
            pojo.noteId,
            pojo.insight,
            pojo.elaboration,
            TypeConverter.toLocalDateTime(pojo.updateDateTime),
        ).apply {
            historyId = pojo.historyId
        }

    fun toLifeNoteHistoryEntityList(
        pojoList: List<LifeNoteHistoryPojo>,
    ): List<LifeNoteHistoryEntity> = pojoList.map(::toLifeNoteHistoryEntity)

    fun toLifeNoteHistoryPojo(entity: LifeNoteHistoryEntity): LifeNoteHistoryPojo =
        LifeNoteHistoryPojo(
            historyId = entity.historyId,
            noteId = entity.noteId,
            insight = entity.insight,
            elaboration = entity.elaboration,
            updateDateTime = TypeConverter.fromLocalDateTime(entity.updateDateTime) ?: 0L,
        )

    fun toLifeNoteHistoryPojoList(
        entityList: List<LifeNoteHistoryEntity>,
    ): List<LifeNoteHistoryPojo> = entityList.map(::toLifeNoteHistoryPojo)

    // ==================== 位置 ====================

    fun toLocationEntity(pojo: LocationPojo): LocationEntity =
        LocationEntity(
            pojo.paragraphId,
            pojo.latitude,
            pojo.longitude,
            pojo.altitude,
            pojo.isFineLocation,
            pojo.provider,
        )

    fun toLocationEntityList(pojoList: List<LocationPojo>): List<LocationEntity> =
        pojoList.map(::toLocationEntity)

    fun toLocationPojo(entity: LocationEntity): LocationPojo =
        LocationPojo(
            paragraphId = entity.paragraphId,
            latitude = entity.latitude,
            longitude = entity.longitude,
            altitude = entity.altitude,
            isFineLocation = entity.isFineLocation,
            provider = entity.provider ?: "",
        )

    fun toLocationPojoList(entityList: List<LocationEntity>): List<LocationPojo> =
        entityList.map(::toLocationPojo)
}
