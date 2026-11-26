package com.wanderer.journal.ui.pages.main.settings.sub.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wanderer.journal.auxiliary.classes.file.MediaFileInfo
import com.wanderer.journal.data.save.db.DiaryDb
import com.wanderer.journal.helpers.file.readMediaDir
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 媒体列表项。
 *
 * @param info 扫描到的媒体文件信息
 * @param isUnreferenced 是否未被数据库中的媒体记录引用（即“未关联”文件）
 */
data class MediaListItem(
    val info: MediaFileInfo,
    val isUnreferenced: Boolean,
)

/**
 * 媒体列表界面状态。
 */
data class MediaListUiState(
    val items: List<MediaListItem> = emptyList(),
    val ordering: Ordering = Ordering.TIME,
    val isAscending: Boolean = true,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

/**
 * 媒体列表 ViewModel。
 *
 * 这里通过 `StateFlow` + Kotlin 协程实现排序切换：
 * - 扫描媒体目录、查询数据库都在 IO 调度器上执行；
 * - 切换排序时只重排内存中的列表，不再重复读取文件系统和数据库；
 * - 文件系统或数据库发生变化时通过 [refresh] 重新加载。
 */
class MediaListViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MediaListUiState())
    val uiState: StateFlow<MediaListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /**
     * 重新扫描媒体目录，并查询数据库中被引用的媒体文件。
     */
    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val context = getApplication<Application>()

                // 读取媒体目录（函数内部已切换到 IO 调度器）
                val mediaFiles = readMediaDir(context)

                // 查询数据库中所有被引用的媒体 Uri。
                // 这里只取文件名进行比较：备份恢复后 fileUri 中的包名/路径可能变化，
                // 但同一个媒体目录中的文件名是唯一的，因此按文件名比较更稳健。
                val referencedFileNames = withContext(Dispatchers.IO) {
                    DiaryDb.getInstance(context)
                        .mediaDao()
                        .getAllMediaFileUris()
                        .mapNotNull { uri ->
                            uri.lastPathSegment?.takeIf { name -> name.isNotEmpty() }
                        }
                        .toSet()
                }

                val newItems = mediaFiles.map { info ->
                    MediaListItem(
                        info = info,
                        isUnreferenced = info.name !in referencedFileNames,
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        items = sortItems(newItems, state.ordering, state.isAscending),
                        isLoading = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e) }
            }
        }
    }

    /**
     * 设置排序方式。
     */
    fun setOrdering(ordering: Ordering) {
        _uiState.update { state ->
            state.copy(
                ordering = ordering,
                items = sortItems(state.items, ordering, state.isAscending),
            )
        }
    }

    /**
     * 设置排序方向。
     *
     * @param ascending `true` 表示升序，`false` 表示降序
     */
    fun setAscending(ascending: Boolean) {
        _uiState.update { state ->
            state.copy(
                isAscending = ascending,
                items = sortItems(state.items, state.ordering, ascending),
            )
        }
    }

    /**
     * 清除错误状态。
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 按排序方式和方向排序。
     */
    private fun sortItems(
        items: List<MediaListItem>,
        ordering: Ordering,
        ascending: Boolean,
    ): List<MediaListItem> {
        val comparator: Comparator<MediaListItem> = when (ordering) {
            Ordering.SIZE -> compareBy { it.info.size }
            Ordering.NAME -> compareBy { it.info.name }
            Ordering.TIME -> compareBy { it.info.createdTimeStamp }
        }

        return items.sortedWith(
            if (ascending) comparator else comparator.reversed()
        )
    }
}
