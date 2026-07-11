package com.dskochilov.cosmos.feature.marsrover.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dskochilov.cosmos.core.common.result.fold
import com.dskochilov.cosmos.core.domain.mars.GetMarsPhotoUseCase
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailIntent
import com.dskochilov.cosmos.feature.marsrover.MarsPhotoDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MarsPhotoDetailViewModel @Inject constructor(
    private val getMarsPhoto: GetMarsPhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Type-safe navigation stores route properties by name in the SavedStateHandle.
    private val photoId: Long = checkNotNull(savedStateHandle[ARG_PHOTO_ID])

    private val _uiState = MutableStateFlow<MarsPhotoDetailUiState>(MarsPhotoDetailUiState.Loading)
    val uiState: StateFlow<MarsPhotoDetailUiState> = _uiState.asStateFlow()

    init {
        loadPhoto()
    }

    fun onIntent(intent: MarsPhotoDetailIntent) {
        when (intent) {
            MarsPhotoDetailIntent.Retry -> loadPhoto()
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

    companion object {
        internal const val ARG_PHOTO_ID = "photoId"
    }
}
