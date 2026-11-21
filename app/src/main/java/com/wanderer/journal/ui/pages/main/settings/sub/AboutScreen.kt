package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.wanderer.journal.R
import com.wanderer.journal.WandererJournal
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.enums.settings.RepositoryAddress
import com.wanderer.journal.helpers.AboutHelper
import com.wanderer.journal.ui.components.PressableCard
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTroubleshooting: () -> Unit
) {
    // 控制仓库地址弹窗的显示状态
    var showRepoDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val resource = LocalResources.current

    // 获取版本名称
    val versionName = remember {
        try {
            "v${AboutHelper.getVersionName(context)}"
        } catch (_: Exception) {
            ""
        }
    }

    // 获取应用图标
    val drawable = remember {
        try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (_: Exception) {
            ResourcesCompat.getDrawable(resource, R.mipmap.ic_launcher, context.theme)
        }
    }

    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.about_software)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollState
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
                .padding(horizontal = Interval.SidePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding(),
                top = Interval.DefaultPadding
            )
        ) {
            // App 图标
            item {
                if (drawable != null) {
                    Image(
                        bitmap = drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
                            .asImageBitmap(),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            // App 名称
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // 版本名称
            if (versionName.isNotEmpty()) {
                item {
                    Text(
                        text = versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // App 描述卡片
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.Single
                ) {
                    Text(
                        text = stringResource(id = R.string.app_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Interval.DefaultPadding)
                    )
                }
            }

            // 作者卡片
            item {
                Spacer(modifier = Modifier.height(15.dp))
                SettingItemCard(
                    title = stringResource(id = R.string.author_name),
                    subtitle = stringResource(id = R.string.author_of_app),
                    normalRadius = Radius.SingleArray,
                    leadingContent = {
                        Image(
                            painter = painterResource(id = R.drawable.author),
                            contentDescription = "Author Head",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    },
                    onClick = {
                        WandererJournal.lockLifecycleObserver(true)
                        val intent =
                            Intent(Intent.ACTION_VIEW, "https://github.com/E-zhiyu".toUri())
                        context.startActivity(intent)
                    }
                )
            }

            // 3. 项目地址卡片
            item {
                Spacer(modifier = Modifier.height(15.dp))
                SettingItemCard(
                    title = stringResource(id = R.string.project_address),
                    subtitle = stringResource(id = R.string.click_skip_to_repository),
                    normalRadius = Radius.TopArray,
                    onClick = { showRepoDialog = true }
                )
            }

            // 4. 疑难解答卡片
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingItemCard(
                    title = stringResource(id = R.string.troubleshooting),
                    subtitle = stringResource(id = R.string.help_solve_some_common_problems),
                    normalRadius = Radius.BottomArray,
                    onClick = onNavigateToTroubleshooting
                )
            }
        }
    }

    // 项目仓库地址单选弹窗
    if (showRepoDialog) {
        RepositoryDialog(
            onDismiss = { showRepoDialog = false },
            onSelected = { address ->
                WandererJournal.lockLifecycleObserver(true)
                val intent = Intent(Intent.ACTION_VIEW, address.toUri())
                context.startActivity(intent)
                showRepoDialog = false
            }
        )
    }
}

/**
 * 封装的通用设置列表卡片项（对应你原有的卡片布局）
 */
@Composable
fun SettingItemCard(
    title: String,
    subtitle: String,
    normalRadius: Array<Dp> = Radius.SingleArray,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    PressableCard(
        onClick = { _, _ -> onClick() },
        modifier = Modifier.fillMaxWidth(),
        normalRadius = normalRadius
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 仓库地址选择弹窗
 */
@Composable
fun RepositoryDialog(
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    val addresses = remember { RepositoryAddress.entries.toTypedArray() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择项目仓库") },
        text = {
            Column {
                addresses.forEach { repo ->
                    TextButton(
                        onClick = { onSelected(repo.address) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = repo.title,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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
private fun Preview() {
    MaterialAppTheme {
        AboutScreen(
            onNavigateBack = {},
            onNavigateToTroubleshooting = {}
        )
    }
}