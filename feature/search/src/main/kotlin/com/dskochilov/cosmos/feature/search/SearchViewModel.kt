package com.dskochilov.cosmos.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dskochilov.cosmos.core.domain.search.ClearSearchHistoryUseCase
import com.dskochilov.cosmos.core.domain.search.MediaItem
import com.dskochilov.cosmos.core.domain.search.ObserveSearchHistoryUseCase
import com.dskochilov.cosmos.core.domain.search.SaveSearchQueryUseCase
import com.dskochilov.cosmos.core.domain.search.SearchMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal const val SEARCH_DEBOUNCE_MS = 350L
internal const val MIN_QUERY_LENGTH = 2

@HiltViewModel
class SearchViewModel @Inject constructor(
    searchMedia: SearchMediaUseCase,
    observeSearchHistory: ObserveSearchHistoryUseCase,
    private val saveSearchQuery: SaveSearchQueryUseCase,
    private val clearSearchHistory: ClearSearchHistoryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Raw text-field value; survives process death via SavedStateHandle. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val history: StateFlow<List<String>> = observeSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /**
     * Debounced search pipeline: typing pauses [SEARCH_DEBOUNCE_MS] before a
     * query fires; identical queries do not re-fire; fired queries are recorded
     * in history.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: Flow<PagingData<MediaItem>> = query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce { if (it.length < MIN_QUERY_LENGTH) 0L else SEARCH_DEBOUNCE_MS }
        .flatMapLatest { term ->
            if (term.length < MIN_QUERY_LENGTH) {
                flowOf(PagingData.empty())
            } else {
                saveSearchQuery(term)
                searchMedia(term)
            }
        }
        .cachedIn(viewModelScope)

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> savedStateHandle[KEY_QUERY] = intent.query
            is SearchIntent.HistorySelected -> savedStateHandle[KEY_QUERY] = intent.query
            SearchIntent.ClearQuery -> savedStateHandle[KEY_QUERY] = ""
            SearchIntent.ClearHistory -> viewModelScope.launch { clearSearchHistory() }
        }
    }

    companion object {
        internal const val KEY_QUERY = "query"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
