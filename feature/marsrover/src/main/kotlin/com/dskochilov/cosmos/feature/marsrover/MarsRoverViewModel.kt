package com.dskochilov.cosmos.feature.marsrover

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.domain.mars.ObserveMarsPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MarsRoverViewModel @Inject constructor(
    observeMarsPhotos: ObserveMarsPhotosUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Current rover/camera filter; survives process death via SavedStateHandle. */
    val filter: StateFlow<MarsFeedFilter> = combine(
        savedStateHandle.getStateFlow(KEY_ROVER, MarsRover.CURIOSITY.name),
        savedStateHandle.getStateFlow<String?>(KEY_CAMERA, null)
    ) { roverName, cameraApiName ->
        MarsFeedFilter(rover = MarsRover.valueOf(roverName), cameraApiName = cameraApiName)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MarsFeedFilter())

    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: Flow<PagingData<MarsPhoto>> = filter
        .flatMapLatest { observeMarsPhotos(it) }
        .cachedIn(viewModelScope)

    fun onIntent(intent: MarsRoverIntent) {
        when (intent) {
            is MarsRoverIntent.SelectRover -> {
                savedStateHandle[KEY_ROVER] = intent.rover.name
                // Cameras are rover-specific; a stale selection would filter everything out.
                savedStateHandle[KEY_CAMERA] = null
            }

            is MarsRoverIntent.SelectCamera -> savedStateHandle[KEY_CAMERA] = intent.cameraApiName
        }
    }

    companion object {
        internal const val KEY_ROVER = "rover"
        internal const val KEY_CAMERA = "camera"
    }
}
