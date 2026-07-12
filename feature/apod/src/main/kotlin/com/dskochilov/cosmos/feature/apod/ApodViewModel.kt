package com.dskochilov.cosmos.feature.apod

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dskochilov.cosmos.core.common.result.fold
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.apod.GetApodUseCase
import com.dskochilov.cosmos.core.domain.favorites.ObserveIsFavoriteUseCase
import com.dskochilov.cosmos.core.domain.favorites.ToggleApodFavoriteUseCase
import com.dskochilov.cosmos.core.domain.favorites.favoriteId
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ApodViewModel @Inject constructor(
    private val getApod: GetApodUseCase,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleApodFavorite: ToggleApodFavoriteUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ApodUiState>(ApodUiState.Loading)
    val uiState: StateFlow<ApodUiState> = _uiState.asStateFlow()

    /** Whether the currently shown APOD entry is saved locally. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = _uiState
        .flatMapLatest { state ->
            when (state) {
                is ApodUiState.Content -> observeIsFavorite(state.apod.favoriteId())
                else -> flowOf(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    private val _effects = Channel<ApodEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<ApodEffect> = _effects.receiveAsFlow()

    init {
        loadApod()
    }

    fun onIntent(intent: ApodIntent) {
        when (intent) {
            is ApodIntent.SelectDate -> {
                // Survives process death: the date is restored through SavedStateHandle.
                savedStateHandle[KEY_SELECTED_DATE] = intent.date.toString()
                loadApod()
            }

            ApodIntent.Retry -> loadApod()

            ApodIntent.OpenVideo -> openVideo()

            ApodIntent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun loadApod() {
        viewModelScope.launch {
            _uiState.value = ApodUiState.Loading
            val date = savedStateHandle.get<String>(KEY_SELECTED_DATE)?.let(LocalDate::parse)
            _uiState.value = getApod(date).fold(
                onSuccess = { ApodUiState.Content(it) },
                onFailure = { ApodUiState.Error(it) }
            )
        }
    }

    private fun openVideo() {
        val media = (uiState.value as? ApodUiState.Content)?.apod?.media
        if (media is ApodMedia.Video) {
            _effects.trySend(ApodEffect.OpenUrl(media.videoUrl))
        }
    }

    private fun toggleFavorite() {
        val apod = (uiState.value as? ApodUiState.Content)?.apod ?: return
        viewModelScope.launch { toggleApodFavorite(apod) }
    }

    companion object {
        internal const val KEY_SELECTED_DATE = "selectedDate"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
