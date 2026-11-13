package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.wanderer.journal.R
import com.wanderer.journal.WandererJournal
import com.wanderer.journal.auxiliary.enums.settings.RepositoryAddress
import androidx.core.net.toUri
import com.wanderer.journal.helpers.AboutHelper
import com.wanderer.journal.ui.theme.WandererJournalTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WandererJournalTheme {
                AboutScreen(
                    onNavigateBack = { finish() },
                    onNavigateToTroubleshooting = {
                        // 跳转到疑难解答页面（如果 TroubleshootingActivity 也重构了，可以换成 Navigation 导航）
                        val intent = Intent(this, TroubleshootingActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// 1. 浅色模式预览
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
// 2. 深色模式预览
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AboutScreenPreview() {
    WandererJournalTheme {
        AboutScreen(
            onNavigateBack = {},
            onNavigateToTroubleshooting = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTroubleshooting: () -> Unit
) {
    //控制仓库地址弹窗的显示状态
    var showRepoDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    //获取版本名称
    val versionName = remember {
        try {
            "v${AboutHelper.getVersionName(context)}"
        } catch (_: Exception) {
            ""
        }
    }

    //获取应用图标
    val errDrawable = ResourcesCompat.getDrawable(
        LocalResources.current,
        R.drawable.outline_error_24,
        context.theme
    )
    val drawable = remember {
        try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (_: Exception) {
            errDrawable
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.about_software)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // App 图标
            if (drawable != null) {
                Image(
                    bitmap = drawable.toBitmap(config = Bitmap.Config.ARGB_8888).asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // App 名称
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )

            // 版本名称
            if (versionName.isNotEmpty()) {
                Text(
                    text = versionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. App 描述卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Text(
                    text = stringResource(id = R.string.app_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 2. 作者卡片 (模拟顶部圆角)
            SettingItemCard(
                title = stringResource(id = R.string.author_name),
                subtitle = stringResource(id = R.string.author_of_app),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 4.dp
                ),
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
                    val intent = Intent(Intent.ACTION_VIEW, "https://github.com/E-zhiyu".toUri())
                    context.startActivity(intent)
                }
            )

            // 3. 项目地址卡片 (中间方形圆角)
            SettingItemCard(
                title = stringResource(id = R.string.project_address),
                subtitle = stringResource(id = R.string.click_skip_to_repository),
                shape = RoundedCornerShape(4.dp),
                onClick = { showRepoDialog = true }
            )

            // 4. 疑难解答卡片 (模拟底部圆角)
            SettingItemCard(
                title = stringResource(id = R.string.troubleshooting),
                subtitle = stringResource(id = R.string.help_solve_some_common_problems),
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                onClick = onNavigateToTroubleshooting
            )
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
    shape: RoundedCornerShape,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
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