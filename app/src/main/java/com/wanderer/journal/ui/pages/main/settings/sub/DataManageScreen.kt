package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wanderer.journal.R
import com.wanderer.journal.data.save.store.viewmodels.AutoBackupViewModel
import com.wanderer.journal.data.save.store.viewmodels.dsAutoBackupViewModel
import com.wanderer.journal.ui.components.LazyLargeTopAppBarScaffold
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun DataManageScreen(
    onNavigateBack: () -> Unit,
    vm: AutoBackupViewModel = dsAutoBackupViewModel()
) {
    LazyLargeTopAppBarScaffold(
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.data_manage)
    ) {
        //TODO:完成内容
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DataManageScreenPreview() {
    MaterialAppTheme {
        DataManageScreen(
            {}
        )
    }
}