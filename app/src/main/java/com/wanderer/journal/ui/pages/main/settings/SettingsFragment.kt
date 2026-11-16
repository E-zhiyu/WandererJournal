package com.wanderer.journal.ui.pages.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.wanderer.journal.ui.theme.MaterialAppTheme
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SettingsFragment : Fragment() {
    // 多线程任务列表
    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 使用 ComposeView 承载重构后的 Compose 设置界面
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialAppTheme {
                    SettingsScreen(disposable)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
    }
}