package com.dskochilov.cosmos.feature.apod

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dskochilov.cosmos.core.domain.apod.ApodDates
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Date picker constrained to the APOD archive range (1995-06-16 .. today, UTC).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ApodDatePickerDialog(initialDate: LocalDate?, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (initialDate ?: LocalDate.now(ZoneOffset.UTC))
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli(),
        yearRange = ApodDates.FIRST_ENTRY.year..LocalDate.now(ZoneOffset.UTC).year,
        selectableDates = ApodSelectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) {
                Text(text = stringResource(R.string.feature_apod_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.feature_apod_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private object ApodSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return !date.isBefore(ApodDates.FIRST_ENTRY) && !date.isAfter(LocalDate.now(ZoneOffset.UTC))
    }

    override fun isSelectableYear(year: Int): Boolean =
        year in ApodDates.FIRST_ENTRY.year..LocalDate.now(ZoneOffset.UTC).year
}
