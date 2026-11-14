package com.wanderer.journal.ui.pages.main.settings.sub

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.ui.theme.MaterialTheme

class TroubleshootingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                TroubleshootingScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TroubleshootingScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.troubleshooting)) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(Interval.SidePadding),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                TroubleshootingItem(
                    questionResId = R.string.floating_notification_question,
                    answerResId = R.string.floating_notification_answer
                )
            }
            item {
                TroubleshootingItem(
                    questionResId = R.string.power_consumption_question,
                    answerResId = R.string.power_consumption_answer
                )
            }
            item {
                TroubleshootingItem(
                    questionResId = R.string.privacy_data_question,
                    answerResId = R.string.privacy_data_answer
                )
            }
            item {
                TroubleshootingItem(
                    questionResId = R.string.data_save_question,
                    answerResId = R.string.data_save_answer
                )
            }
            item {
                TroubleshootingItem(
                    questionResId = R.string.ad_integration_question,
                    answerResId = R.string.ad_integration_answer
                )
            }
            item {
                TroubleshootingItem(
                    questionResId = R.string.update_question,
                    answerResId = R.string.update_answer
                )
            }
        }
    }
}

/**
 * 封装的问答条目组件，避免代码重复
 */
@Composable
fun TroubleshootingItem(
    questionResId: Int,
    answerResId: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = questionResId),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(id = answerResId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
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
fun TroubleshootingScreenPreview() {
    MaterialTheme {
        TroubleshootingScreen()
    }
}