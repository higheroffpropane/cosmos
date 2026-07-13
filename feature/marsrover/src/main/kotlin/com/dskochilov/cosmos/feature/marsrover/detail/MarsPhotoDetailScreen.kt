package com.dskochilov.cosmos.feature.marsrover.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dskochilov.cosmos.core.designsystem.component.ErrorState
import com.dskochilov.cosmos.core.designsystem.component.LoadingState
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.ui.cosmosSharedBounds
import com.dskochilov.cosmos.core.ui.toUiMessage
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailIntent
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailUiState
import com.dskochilov.cosmos.feature.marsrover.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

@Composable
internal fun MarsPhotoDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MarsPhotoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    MarsPhotoDetailContent(
        state = uiState,
        isFavorite = isFavorite,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
internal fun MarsPhotoDetailContent(
    state: MarsPhotoDetailUiState,
    isFavorite: Boolean,
    onIntent: (MarsPhotoDetailIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .safeDrawingPadding()
    ) {
        when (state) {
            MarsPhotoDetailUiState.Loading -> LoadingState()

            is MarsPhotoDetailUiState.Error -> ErrorState(
                message = state.error.toUiMessage(),
                onRetry = { onIntent(MarsPhotoDetailIntent.Retry) }
            )

            is MarsPhotoDetailUiState.Content -> ZoomablePhoto(photo = state.photo)
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.feature_marsrover_back),
                tint = Color.White
            )
        }

        if (state is MarsPhotoDetailUiState.Content) {
            IconButton(
                onClick = { onIntent(MarsPhotoDetailIntent.ToggleFavorite) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) {
                            R.string.feature_marsrover_remove_favorite
                        } else {
                            R.string.feature_marsrover_save_favorite
                        }
                    ),
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }
    }
}

@Composable
private fun ZoomablePhoto(photo: MarsPhoto, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
        offset = if (scale > MIN_ZOOM) offset + panChange else Offset.Zero
    }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = photo.imageUrl,
            contentDescription = photo.cameraFullName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .cosmosSharedBounds(key = "mars-photo-${photo.id}")
                .transformable(transformableState)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            Text(
                text = photo.cameraFullName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "${photo.earthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} · " +
                    stringResource(R.string.feature_marsrover_sol, photo.sol),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
