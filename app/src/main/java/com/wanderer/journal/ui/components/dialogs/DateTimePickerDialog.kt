package com.wanderer.journal.ui.components.dialogs

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.wanderer.journal.data.save.db.converters.DateTimeConverter
import java.time.LocalDate

/**
 * 日期选择对话框
 *
 * @param initialDate 初始选中的日期
 * @param selectableDates 判断哪些日期可以选择，形参为日期选择器中每天0点的时间戳，传递 null 则不限制日期范围
 * @param yearRange 日期选择器可选的年份范围，传递 null 则不限制可选年份
 * @param onDismiss 对话框消失回调
 * @param onConfirm 对话框确认回调
 */
@Composable
fun DatePickerDialogWithFilter(
    initialDate: LocalDate?,
    selectableDates: ((Long) -> Boolean)? = null,
    yearRange: IntRange? = null,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.let {
            DateTimeConverter.fromLocalDate(it)
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return selectableDates == null || selectableDates(utcTimeMillis)
            }
        },
        yearRange = yearRange ?: IntRange(Int.MIN_VALUE, Int.MAX_VALUE)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onConfirm(DateTimeConverter.toLocalDate(it))
                }
                onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    ) {
        // TODO:修复充满屏幕宽度的BUG
        DatePicker(state = datePickerState)
    }
}