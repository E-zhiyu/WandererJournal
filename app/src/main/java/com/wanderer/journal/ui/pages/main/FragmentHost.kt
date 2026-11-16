package com.wanderer.journal.ui.pages.main

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commitNow
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

/**
 * 用 AndroidView 承载已有的 Fragment，便于渐进式迁移。
 * 全部重写为 Compose 后可以移除。
 */
@Composable
fun FragmentHost(fragmentFactory: () -> Fragment) {
    val activity = LocalActivity.current as FragmentActivity
    val tag = remember { "frag_${fragmentFactory.hashCode()}" }

    AndroidView(
        factory = { ctx ->
            val container = FragmentContainerView(ctx).apply {
                id = View.generateViewId()
            }
            activity.supportFragmentManager.commitNow {
                replace(container.id, fragmentFactory(), tag)
            }
            container
        }
    )
}