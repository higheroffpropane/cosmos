package com.dskochilov.cosmos.feature.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.dskochilov.cosmos.core.common.result.asAppError
import com.dskochilov.cosmos.core.designsystem.component.ErrorState
import com.dskochilov.cosmos.core.designsystem.component.LoadingState
import com.dskochilov.cosmos.core.designsystem.component.ScreenPlaceholder
import com.dskochilov.cosmos.core.domain.search.MediaItem
import com.dskochilov.cosmos.core.ui.toUiMessage

@Composable
internal fun SearchScreen(modifier: Modifier = Modifier, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsLazyPagingItems()

    SearchContent(
        query = query,
        history = history,
        results = results,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
internal fun SearchContent(
    query: String,
    history: List<String>,
    results: LazyPagingItems<MediaItem>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchField(query = query, onIntent = onIntent)

        if (query.trim().length < MIN_QUERY_LENGTH) {
            HistorySection(history = history, onIntent = onIntent, modifier = Modifier.weight(1f))
        } else {
            ResultsSection(results = results, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SearchField(query: String, onIntent: (SearchIntent) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
        singleLine = true,
        placeholder = { Text(text = stringResource(R.string.feature_search_placeholder)) },
        leadingIcon = {
            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onIntent(SearchIntent.ClearQuery) }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = stringResource(R.string.feature_search_clear_query)
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun HistorySection(history: List<String>, onIntent: (SearchIntent) -> Unit, modifier: Modifier = Modifier) {
    if (history.isEmpty()) {
        ScreenPlaceholder(
            title = stringResource(R.string.feature_search_hint),
            modifier = modifier
        )
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.feature_search_recent),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onIntent(SearchIntent.ClearHistory) }) {
                    Text(text = stringResource(R.string.feature_search_clear_history))
                }
            }
        }
        items(history, key = { it }) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(SearchIntent.HistorySelected(entry)) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = entry,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultsSection(results: LazyPagingItems<MediaItem>, modifier: Modifier = Modifier) {
    when (val refresh = results.loadState.refresh) {
        is LoadState.Loading -> LoadingState(modifier = modifier)

        is LoadState.Error -> ErrorState(
            message = refresh.error.asAppError().toUiMessage(),
            onRetry = results::retry,
            modifier = modifier
        )

        is LoadState.NotLoading ->
            if (results.itemCount == 0) {
                ScreenPlaceholder(
                    title = stringResource(R.string.feature_search_empty),
                    modifier = modifier
                )
            } else {
                ResultsGrid(results = results, modifier = modifier)
            }
    }
}

@Composable
private fun ResultsGrid(results: LazyPagingItems<MediaItem>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(count = results.itemCount, key = results.itemKey { it.nasaId }) { index ->
            val item = results[index]
            if (item != null) {
                MediaResultCell(item = item)
            }
        }

        when (results.loadState.append) {
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
                        text = stringResource(R.string.feature_search_append_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = results::retry) {
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
private fun MediaResultCell(item: MediaItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
