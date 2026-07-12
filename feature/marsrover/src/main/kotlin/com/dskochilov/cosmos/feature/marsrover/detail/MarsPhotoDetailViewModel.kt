package com.dskochilov.cosmos.feature.marsrover.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dskochilov.cosmos.core.common.result.fold
import com.dskochilov.cosmos.core.domain.favorites.ObserveIsFavoriteUseCase
import com.dskochilov.cosmos.core.domain.favorites.ToggleMarsPhotoFavoriteUseCase
import com.dskochilov.cosmos.core.domain.favorites.favoriteId
import com.dskochilov.cosmos.core.domain.mars.GetMarsPhotoUseCase
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailIntent
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MarsPhotoDetailViewModel @Inject constructor(
    private val getMarsPhoto: GetMarsPhotoUseCase,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleMarsPhotoFavorite: ToggleMarsPhotoFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Type-safe navigation stores route properties by name in the SavedStateHandle.
    private val photoId: String = checkNotNull(savedStateHandle[ARG_PHOTO_ID])

    private val _uiState = MutableStateFlow<MarsPhotoDetailUiState>(MarsPhotoDetailUiState.Loading)
    val uiState: StateFlow<MarsPhotoDetailUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = _uiState
        .flatMapLatest { state ->
            when (state) {
                is MarsPhotoDetailUiState.Content -> observeIsFavorite(state.photo.favoriteId())
                else -> flowOf(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    init {
        loadPhoto()
    }

    fun onIntent(intent: MarsPhotoDetailIntent) {
        when (intent) {
            MarsPhotoDetailIntent.Retry -> loadPhoto()
            MarsPhotoDetailIntent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun loadPhoto() {
        viewModelScope.launch {
            _uiState.value = MarsPhotoDetailUiState.Loading
            _uiState.value = getMarsPhoto(photoId).fold(
                onSuccess = { MarsPhotoDetailUiState.Content(it) },
                onFailure = { MarsPhotoDetailUiState.Error(it) }
            )
        }
    }

    private fun toggleFavorite() {
        val photo = (uiState.value as? MarsPhotoDetailUiState.Content)?.photo ?: return
        viewModelScope.launch { toggleMarsPhotoFavorite(photo) }
    }

    companion object {
        internal const val ARG_PHOTO_ID = "photoId"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
