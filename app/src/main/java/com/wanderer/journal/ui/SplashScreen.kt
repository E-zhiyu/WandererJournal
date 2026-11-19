package com.wanderer.journal.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.wanderer.journal.R
import kotlin.math.hypot

/**
 * 应用启动界面遮罩
 * @param progress 0f = 洞口最小(包住图标)，1f = 洞口覆盖全屏
 * @param iconAlpha 图标透明度
 */
@Composable
fun ExpandingMask(
    progress: Float,
    iconAlpha: Float,
    maskColor: Color,
    iconSize: Dp,
    iconCornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    // 获取应用图标
    val context = LocalContext.current
    val resource = LocalResources.current
    val drawable = remember {
        try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (_: Exception) {
            ResourcesCompat.getDrawable(resource, R.mipmap.ic_launcher, context.theme)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 1. 遮罩层（中间挖圆角方洞）
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenDiagonal = hypot(size.width, size.height)
            val minHole = iconSize.toPx()          // 洞口初始边长 = 图标尺寸
            val maxHole = screenDiagonal * 2f      // 足够覆盖全屏
            val hole = minHole + (maxHole - minHole) * progress
            val holeRect = Rect(
                left = (size.width - hole) / 2f,
                top = (size.height - hole) / 2f,
                right = (size.width + hole) / 2f,
                bottom = (size.height + hole) / 2f,
            )
            // 圆角半径：初始 = 图标圆角；扩张到最大时可以保持或减小
            val corner = iconCornerRadius.toPx() * (1f - progress * 0.5f)

            val maskPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))               // 全屏
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(holeRect, CornerRadius(corner, corner))
                )                                             // 挖洞
            }
            drawPath(maskPath, maskColor)
        }

        // 2. 图标（在遮罩之上，随扩张淡出）
        if (iconAlpha > 0f && drawable != null) {
            Image(
                bitmap = drawable.toBitmap(config = Bitmap.Config.ARGB_8888).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer { alpha = iconAlpha },
                contentScale = ContentScale.Fit,
            )
        }
    }
}