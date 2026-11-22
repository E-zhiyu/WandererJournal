package com.wanderer.journal.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.wanderer.journal.auxiliary.classes.Radius

@Composable
fun PressableCard(
    modifier: Modifier = Modifier,
    onClick: (Offset) -> Unit,
    onLongClick: ((Offset) -> Unit)? = null,
    normalRadius: Array<Dp> = Radius.SingleArray,
    pressedCorner: Dp = Radius.Large, // 按下时统一变成的大圆角大小
    colors: CardColors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerHighest),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    // 马达震动
    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
    }

    // 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 四个角的平滑过渡动画
    val animTopStart by animateDpAsState(
        if (isPressed) pressedCorner else normalRadius[0],
        label = "ts"
    )
    val animTopEnd by animateDpAsState(
        if (isPressed) pressedCorner else normalRadius[1],
        label = "te"
    )
    val animBottomStart by animateDpAsState(
        if (isPressed) pressedCorner else normalRadius[2],
        label = "bs"
    )
    val animBottomEnd by animateDpAsState(
        if (isPressed) pressedCorner else normalRadius[3],
        label = "be"
    )

    // 计算缩放后的形状
    val animatedShape = RoundedCornerShape(
        topStart = animTopStart,
        topEnd = animTopEnd,
        bottomEnd = animBottomEnd,
        bottomStart = animBottomStart
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(animatedShape)
            .indication(interactionSource, ripple())    // 绘制按压涟漪
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // 手动发送按压/释放事件，触发涟漪绘制
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                    },
                    onTap = { offset -> onClick(offset) },
                    onLongPress = { offset ->
                        if (onLongClick != null) {
                            onLongClick(offset)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                )
            },
        shape = animatedShape,
        colors = colors
    ) {
        content()
    }
}