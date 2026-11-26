package com.wanderer.journal.ui.pages.main.settings.sub.media

import androidx.annotation.StringRes
import com.wanderer.journal.R

/**
 * 媒体列表的排序方式。
 */
enum class Ordering(@StringRes val titleRes: Int) {
    TIME(R.string.order_by_time),   // 时间排序
    SIZE(R.string.order_by_size),   // 大小排序
    NAME(R.string.order_by_name),   // 名称排序
}
