package com.wanderer.journal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import com.wanderer.journal.auxiliary.classes.Radius

@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    normalRadius: Array<Dp> = Radius.SingleArray,
    pressedCorner: Dp = Radius.Large, // 按下时统一变成的大圆角大小
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    // 1. 马达震动
    LaunchedEffect(isPressed) {
        if (isPressed) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    // 2. 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 3. 四个角的平滑过渡动画
    val animTopStart by animateDpAsState(if (isPressed) pressedCorner else normalRadius[0], label = "ts")
    val animTopEnd by animateDpAsState(if (isPressed) pressedCorner else normalRadius[1], label = "te")
    val animBottomEnd by animateDpAsState(if (isPressed) pressedCorner else normalRadius[2], label = "be")
    val animBottomStart by animateDpAsState(if (isPressed) pressedCorner else normalRadius[3], label = "bs")

    val animatedShape = RoundedCornerShape(
        topStart = animTopStart,
        topEnd = animTopEnd,
        bottomEnd = animBottomEnd,
        bottomStart = animBottomStart
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = animatedShape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        content()
    }
}