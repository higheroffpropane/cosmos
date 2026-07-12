package com.dskochilov.cosmos.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dskochilov.cosmos.core.designsystem.component.LoadingState
import com.dskochilov.cosmos.core.designsystem.component.ScreenPlaceholder
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.favorites.Favorite
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun FavoritesScreen(modifier: Modifier = Modifier, viewModel: FavoritesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesContent(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
internal fun FavoritesContent(
    state: FavoritesUiState,
    onIntent: (FavoritesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.feature_favorites_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when (state) {
            FavoritesUiState.Loading -> LoadingState(modifier = Modifier.weight(1f))

            FavoritesUiState.Empty -> ScreenPlaceholder(
                title = stringResource(R.string.feature_favorites_empty),
                modifier = Modifier.weight(1f)
            )

            is FavoritesUiState.Content -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.favorites, key = { it.id }) { favorite ->
                    FavoriteCard(
                        favorite = favorite,
                        onRemove = { onIntent(FavoritesIntent.Remove(favorite.id)) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(favorite: Favorite, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = favorite.thumbnail(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = favorite.title(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = favorite.subtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = stringResource(R.string.feature_favorites_remove),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun Favorite.thumbnail(): String = when (this) {
    is Favorite.ApodEntry -> when (val media = apod.media) {
        is ApodMedia.Image -> media.url
        is ApodMedia.Video -> media.thumbnailUrl.orEmpty()
        ApodMedia.Unsupported -> ""
    }

    is Favorite.MarsPhotoEntry -> photo.thumbnailUrl
}

private fun Favorite.title(): String = when (this) {
    is Favorite.ApodEntry -> apod.title
    is Favorite.MarsPhotoEntry -> photo.cameraFullName
}

@Composable
private fun Favorite.subtitle(): String = when (this) {
    is Favorite.ApodEntry -> stringResource(
        R.string.feature_favorites_subtitle_apod,
        apod.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    )

    is Favorite.MarsPhotoEntry -> stringResource(
        R.string.feature_favorites_subtitle_mars,
        photo.rover.displayName,
        photo.sol
    )
}
