package com.dskochilov.cosmos.feature.search

/**
 * MVI contract of the media search screen. Results are exposed separately as
 * `Flow<PagingData<MediaItem>>` (Paging owns its own load-state machine);
 * the query and history are regular MVI state.
 */
sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent

    data class HistorySelected(val query: String) : SearchIntent

    data object ClearQuery : SearchIntent

    data object ClearHistory : SearchIntent
}
