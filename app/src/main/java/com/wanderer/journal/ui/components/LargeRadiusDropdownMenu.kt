package com.wanderer.journal.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.wanderer.journal.auxiliary.classes.Radius

@Composable
fun LargeRadiusDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(Radius.Large),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = shape,
        containerColor =containerColor,
        offset = offset
    ) {
        content()
    }
}