package com.example.sportsbook.ui.screens.player.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val myCommunities: List<Community> = emptyList(),
    val publicCommunities: List<Community> = emptyList(),
    val selectedTab: CommunityTab = CommunityTab.MY,
    val sportTypeFilter: String? = null,
    val error: String? = null
) {
    val displayedCommunities: List<Community>
        get() = when (selectedTab) {
            CommunityTab.MY -> myCommunities
            CommunityTab.DISCOVER -> publicCommunities
        }
}

enum class CommunityTab { MY, DISCOVER }

@HiltViewModel
class CommunityListViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityListUiState())
    val uiState: StateFlow<CommunityListUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun selectTab(tab: CommunityTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == CommunityTab.DISCOVER && _uiState.value.publicCommunities.isEmpty()) {
            loadPublic()
        }
    }

    fun setSportTypeFilter(sportType: String?) {
        _uiState.update { it.copy(sportTypeFilter = sportType) }
        loadPublic()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadAll(isRefresh = true)
    }

    private fun loadAll(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) _uiState.update { it.copy(isLoading = true, error = null) }
            val myResult = communityRepository.getMyCommunities()
            val publicResult = communityRepository.getPublicCommunities(_uiState.value.sportTypeFilter)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    myCommunities = myResult.getOrElse { state.myCommunities },
                    publicCommunities = publicResult.getOrElse { state.publicCommunities },
                    error = myResult.exceptionOrNull()?.message
                        ?: publicResult.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun loadPublic() {
        viewModelScope.launch {
            communityRepository.getPublicCommunities(_uiState.value.sportTypeFilter)
                .onSuccess { list ->
                    _uiState.update { it.copy(publicCommunities = list) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
