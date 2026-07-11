package com.dskochilov.cosmos.feature.apod

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dskochilov.cosmos.core.designsystem.component.ErrorState
import com.dskochilov.cosmos.core.designsystem.component.LoadingState
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.ui.toUiMessage
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun ApodScreen(modifier: Modifier = Modifier, viewModel: ApodViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ApodEffect.OpenUrl ->
                    // No browser/YouTube app installed is not worth crashing over.
                    runCatching { uriHandler.openUri(effect.url) }
            }
        }
    }

    ApodContent(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
internal fun ApodContent(state: ApodUiState, onIntent: (ApodIntent) -> Unit, modifier: Modifier = Modifier) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.feature_apod_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Rounded.DateRange,
                    contentDescription = stringResource(R.string.feature_apod_pick_date)
                )
            }
        }

        when (state) {
            ApodUiState.Loading -> LoadingState(modifier = Modifier.weight(1f))

            is ApodUiState.Error -> ErrorState(
                message = state.error.toUiMessage(),
                onRetry = { onIntent(ApodIntent.Retry) },
                modifier = Modifier.weight(1f)
            )

            is ApodUiState.Content -> ApodDetails(
                apod = state.apod,
                onIntent = onIntent,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showDatePicker) {
        val currentDate = (state as? ApodUiState.Content)?.apod?.date
        ApodDatePickerDialog(
            initialDate = currentDate,
            onConfirm = { date ->
                showDatePicker = false
                onIntent(ApodIntent.SelectDate(date))
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun ApodDetails(apod: Apod, onIntent: (ApodIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ApodMediaBlock(apod = apod, onIntent = onIntent)

        Text(
            text = apod.title,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = apod.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        apod.copyright?.let { copyright ->
            Text(
                text = stringResource(R.string.feature_apod_copyright, copyright),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = apod.explanation,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ApodMediaBlock(apod: Apod, onIntent: (ApodIntent) -> Unit, modifier: Modifier = Modifier) {
    when (val media = apod.media) {
        is ApodMedia.Image -> AsyncImage(
            model = media.url,
            contentDescription = apod.title,
            contentScale = ContentScale.FillWidth,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
        )

        is ApodMedia.Video -> Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clickable { onIntent(ApodIntent.OpenVideo) }
        ) {
            AsyncImage(
                model = media.thumbnailUrl,
                contentDescription = apod.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.feature_apod_play_video),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                )
            }
        }

        ApodMedia.Unsupported -> Text(
            text = stringResource(R.string.feature_apod_unsupported_media),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}
