package com.dskochilov.cosmos.feature.marsrover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.dskochilov.cosmos.core.common.result.asAppError
import com.dskochilov.cosmos.core.designsystem.component.ErrorState
import com.dskochilov.cosmos.core.designsystem.component.LoadingState
import com.dskochilov.cosmos.core.designsystem.component.OfflineBanner
import com.dskochilov.cosmos.core.designsystem.component.ScreenPlaceholder
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.ui.cosmosSharedBounds
import com.dskochilov.cosmos.core.ui.toUiMessage
import com.dskochilov.cosmos.feature.marsrover.MarsRoverIntent.SelectCamera
import com.dskochilov.cosmos.feature.marsrover.MarsRoverIntent.SelectRover

@Composable
internal fun MarsRoverScreen(
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MarsRoverViewModel = hiltViewModel()
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val photos = viewModel.photos.collectAsLazyPagingItems()

    MarsRoverContent(
        filter = filter,
        photos = photos,
        onIntent = viewModel::onIntent,
        onPhotoClick = onPhotoClick,
        modifier = modifier
    )
}

@Composable
internal fun MarsRoverContent(
    filter: MarsFeedFilter,
    photos: LazyPagingItems<MarsPhoto>,
    onIntent: (MarsRoverIntent) -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        RoverSelector(filter = filter, onIntent = onIntent)
        CameraSelector(filter = filter, onIntent = onIntent)

        when (val refresh = photos.loadState.refresh) {
            is LoadState.Loading -> LoadingState(modifier = Modifier.weight(1f))

            is LoadState.Error ->
                if (photos.itemCount > 0) {
                    // Refresh failed but the cache has photos: keep the feed usable offline.
                    OfflineBanner()
                    PhotoGrid(
                        photos = photos,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ErrorState(
                        message = refresh.error.asAppError().toUiMessage(),
                        onRetry = photos::retry,
                        modifier = Modifier.weight(1f)
                    )
                }

            is LoadState.NotLoading ->
                if (photos.itemCount == 0) {
                    ScreenPlaceholder(
                        title = stringResource(R.string.feature_marsrover_empty),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    PhotoGrid(
                        photos = photos,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f)
                    )
                }
        }
    }
}

@Composable
private fun RoverSelector(filter: MarsFeedFilter, onIntent: (MarsRoverIntent) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        MarsRover.entries.forEachIndexed { index, rover ->
            SegmentedButton(
                selected = filter.rover == rover,
                onClick = { onIntent(SelectRover(rover)) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = MarsRover.entries.size)
            ) {
                Text(text = rover.displayName)
            }
        }
    }
}

@Composable
private fun CameraSelector(filter: MarsFeedFilter, onIntent: (MarsRoverIntent) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item(key = "all") {
            FilterChip(
                selected = filter.cameraApiName == null,
                onClick = { onIntent(SelectCamera(null)) },
                label = { Text(text = stringResource(R.string.feature_marsrover_all_cameras)) }
            )
        }
        items(filter.rover.cameras, key = { it.apiName }) { camera ->
            FilterChip(
                selected = filter.cameraApiName == camera.apiName,
                onClick = { onIntent(SelectCamera(camera.apiName)) },
                label = { Text(text = camera.displayName) }
            )
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: LazyPagingItems<MarsPhoto>,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(count = photos.itemCount, key = photos.itemKey { it.id }) { index ->
            val photo = photos[index]
            if (photo != null) {
                MarsPhotoCell(photo = photo, onPhotoClick = onPhotoClick)
            }
        }

        when (val append = photos.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.feature_marsrover_append_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = photos::retry) {
                        Text(
                            text = stringResource(
                                com.dskochilov.cosmos.core.designsystem.R.string.core_designsystem_retry
                            )
                        )
                    }
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun MarsPhotoCell(photo: MarsPhoto, onPhotoClick: (String) -> Unit, modifier: Modifier = Modifier) {
    AsyncImage(
        model = photo.thumbnailUrl,
        contentDescription = photo.cameraFullName,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .clipToBounds()
            .cosmosSharedBounds(key = "mars-photo-${photo.id}")
            .clickable { onPhotoClick(photo.id) }
    )
}
