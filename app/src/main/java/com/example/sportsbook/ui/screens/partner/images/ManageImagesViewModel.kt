package com.example.sportsbook.ui.screens.partner.images

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.CoachImage
import com.example.sportsbook.domain.model.VenueImage
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.ImageRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisplayImage(
    val id: Long,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int
)

data class ManageImagesUiState(
    val entityType: String = "",
    val entityId: Long = 0,
    val entityName: String = "",
    val images: List<DisplayImage> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val actionError: String? = null
)

@HiltViewModel
class ManageImagesViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageImagesUiState())
    val uiState: StateFlow<ManageImagesUiState> = _uiState.asStateFlow()

    fun initialize(entityType: String, entityId: Long) {
        if (_uiState.value.entityId == entityId && _uiState.value.entityType == entityType) return
        _uiState.update { it.copy(entityType = entityType, entityId = entityId) }
        loadImages()
    }

    fun loadImages() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (state.entityType) {
                "venue" -> {
                    venueRepository.getVenueById(state.entityId)
                        .onSuccess { venue ->
                            _uiState.update {
                                it.copy(
                                    entityName = venue.name,
                                    images = venue.images.map { img -> img.toDisplay() },
                                    isLoading = false
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(error = e.message, isLoading = false) }
                        }
                }
                "coach" -> {
                    coachRepository.getCoachById(state.entityId)
                        .onSuccess { coach ->
                            _uiState.update {
                                it.copy(
                                    entityName = coach.name,
                                    images = coach.images.map { img -> img.toDisplay() },
                                    isLoading = false
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(error = e.message, isLoading = false) }
                        }
                }
            }
        }
    }

    fun uploadImage(uri: Uri) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, actionError = null) }

            val timestamp = System.currentTimeMillis()
            val storagePath = "${state.entityType}s/${state.entityId}/${timestamp}.jpg"

            imageRepository.uploadAndGetUrl(uri, storagePath)
                .onSuccess { downloadUrl ->
                    val isPrimary = state.images.isEmpty()
                    val displayOrder = state.images.size

                    val addResult = when (state.entityType) {
                        "venue" -> imageRepository.addVenueImage(
                            state.entityId, downloadUrl, isPrimary, displayOrder
                        ).map { it.toDisplay() }
                        "coach" -> imageRepository.addCoachImage(
                            state.entityId, downloadUrl, isPrimary, displayOrder
                        ).map { it.toDisplay() }
                        else -> Result.failure(IllegalStateException("Unknown entity type"))
                    }

                    addResult
                        .onSuccess { newImage ->
                            _uiState.update {
                                it.copy(
                                    images = it.images + newImage,
                                    isUploading = false
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(actionError = e.message, isUploading = false)
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            actionError = "Upload failed: ${e.message}",
                            isUploading = false
                        )
                    }
                }
        }
    }

    fun deleteImage(imageId: Long) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(actionError = null) }

            val result = when (state.entityType) {
                "venue" -> imageRepository.deleteVenueImage(state.entityId, imageId)
                "coach" -> imageRepository.deleteCoachImage(state.entityId, imageId)
                else -> Result.failure(IllegalStateException("Unknown entity type"))
            }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(images = it.images.filter { img -> img.id != imageId })
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(actionError = e.message) }
                }
        }
    }

    fun setPrimaryImage(imageId: Long) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(actionError = null) }

            val result = when (state.entityType) {
                "venue" -> imageRepository.setVenuePrimaryImage(state.entityId, imageId)
                "coach" -> imageRepository.setCoachPrimaryImage(state.entityId, imageId)
                else -> Result.failure(IllegalStateException("Unknown entity type"))
            }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            images = it.images.map { img ->
                                img.copy(isPrimary = img.id == imageId)
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(actionError = e.message) }
                }
        }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    private fun VenueImage.toDisplay() = DisplayImage(id, imageUrl, isPrimary, displayOrder)
    private fun CoachImage.toDisplay() = DisplayImage(id, imageUrl, isPrimary, displayOrder)
}
