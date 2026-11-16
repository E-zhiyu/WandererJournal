package com.wanderer.journal.ui.pages.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wanderer.journal.R

enum class MainDestination(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
) {
//    DIARY(R.string.diary, R.drawable.baseline_menu_book_24),
    HOME(R.string.home_page, R.drawable.baseline_home_24),
    SETTINGS(R.string.settings, R.drawable.baseline_settings_24);

    companion object {
        fun fromIndex(index: Int): MainDestination = entries.getOrElse(index) { HOME }
    }
}