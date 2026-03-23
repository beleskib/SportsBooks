package com.example.sportsbook.ui.screens.player.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Favorite
import com.example.sportsbook.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<Favorite> = emptyList(),
    val selectedTab: Int = 0, // 0=Venues, 1=Coaches, 2=Matches
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            favoriteRepository.getMyFavorites()
                .onSuccess { favorites ->
                    _uiState.update { it.copy(favorites = favorites, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun removeFavorite(entityType: String, entityId: Long) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(entityType, entityId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            favorites = state.favorites.filter {
                                !(it.entityType.value == entityType && it.entityId == entityId)
                            }
                        )
                    }
                }
        }
    }
}
