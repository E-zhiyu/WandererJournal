package com.wanderer.journal.ui.pages.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wanderer.journal.R

enum class MainDestination(
    val route: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
) {
    DIARY("nav_diary", R.string.diary, R.drawable.baseline_menu_book_24),
    HOME("nav_home", R.string.home_page, R.drawable.baseline_home_24),
    SETTINGS("nav_settings", R.string.settings, R.drawable.baseline_settings_24);

    companion object {
        fun fromIndex(index: Int): MainDestination = entries.getOrElse(index) { HOME }
    }
}