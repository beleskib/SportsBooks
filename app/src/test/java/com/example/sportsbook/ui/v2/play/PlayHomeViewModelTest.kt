package com.example.sportsbook.ui.v2.play

import com.example.sportsbook.domain.model.v2.FriendAvailability
import com.example.sportsbook.domain.model.v2.HomeFeedSnapshot
import com.example.sportsbook.domain.model.v2.PlaySearchResult
import com.example.sportsbook.domain.model.v2.PlaySuggestion
import com.example.sportsbook.domain.model.v2.RebookSuggestion
import com.example.sportsbook.domain.repository.V2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ============================================================
// v2-practical-ux: Unit tests for PlayHomeViewModel
// Tests focus on state transitions that would break if the
// backend contract changed (loading flags, success data, errors).
// ============================================================

@OptIn(ExperimentalCoroutinesApi::class)
class PlayHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var v2Repository: V2Repository

    private val stubFeed = HomeFeedSnapshot(
        displayName = "Alex",
        reliabilityScore = 0.87,
        totalAttended = 24,
        recentBookings = listOf(
            RebookSuggestion(
                bookingId = 1L,
                venueId = 10L,
                venueName = "City Court",
                coachId = null,
                coachName = null,
                sportType = "tennis",
                lastPlayedAt = "2026-04-10T19:00:00Z",
                lastSlotStart = "2026-04-10T19:00:00Z",
                price = 20.0,
                timesBooked = 5
            )
        ),
        suggestedPlay = listOf(
            PlaySuggestion(
                type = "lobby",
                id = 100L,
                title = "Evening Tennis",
                sportType = "tennis",
                startAt = "2026-04-25T19:00:00Z",
                venueName = "City Court",
                distanceKm = 1.2,
                currentPlayers = 2,
                maxPlayers = 4,
                skillLevelMin = 2,
                skillLevelMax = 4,
                price = 15.0
            )
        ),
        friendsAvailable = listOf(
            FriendAvailability(
                userId = 50L,
                displayName = "Sara",
                photoUrl = null,
                sportType = "tennis",
                skillLevel = 3,
                availableUntil = null,
                distanceKm = 0.8
            )
        ),
        upcoming = emptyList()
    )

    private val stubSearchResult = PlaySearchResult(
        results = listOf(
            PlaySuggestion(
                type = "open_slot",
                id = 200L,
                title = "Open slot at Arena",
                sportType = "basketball",
                startAt = "2026-04-25T10:00:00Z",
                venueName = "Arena",
                distanceKm = null,
                currentPlayers = 0,
                maxPlayers = 1,
                skillLevelMin = null,
                skillLevelMax = null,
                price = 30.0
            )
        ),
        lobbies = 3,
        openSlots = 12,
        availablePlayers = 7
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        v2Repository = mockk()
        // Default stubs
        coEvery { v2Repository.getHomeFeed() } returns Result.success(stubFeed)
        coEvery { v2Repository.searchPlay(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.success(stubSearchResult)
        coEvery { v2Repository.rebook(any(), any(), any()) } returns Result.success(999L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = PlayHomeViewModel(v2Repository)

    // ----------------------------------------------------------
    // Feed loading
    // ----------------------------------------------------------

    @Test
    fun `init loads feed successfully and clears loading flag`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse("feedLoading should be false after success", state.feedLoading)
        assertNull("feedError should be null on success", state.feedError)
        assertNotNull("feed should be populated", state.feed)
        assertEquals("Alex", state.feed?.displayName)
        assertEquals(0.87, state.feed?.reliabilityScore ?: 0.0, 0.001)
    }

    @Test
    fun `loadFeed sets feedError when repository fails`() = runTest {
        coEvery { v2Repository.getHomeFeed() } returns Result.failure(RuntimeException("network error"))

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.feedLoading)
        assertEquals("network error", state.feedError)
        assertNull(state.feed)
    }

    @Test
    fun `initial state has feedLoading true before coroutine runs`() = runTest {
        val vm = buildViewModel()
        // Before advancing, loading should still be true
        assertTrue(vm.uiState.value.feedLoading)
    }

    // ----------------------------------------------------------
    // Search
    // ----------------------------------------------------------

    @Test
    fun `search transitions to hasSearched and populates searchResult`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle() // finish feed load

        vm.search()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.searching)
        assertTrue(state.hasSearched)
        assertNotNull(state.searchResult)
        assertEquals(3, state.searchResult?.lobbies)
        assertEquals(12, state.searchResult?.openSlots)
        assertEquals(1, state.searchResult?.results?.size)
    }

    @Test
    fun `search sets searchError on failure and keeps hasSearched true`() = runTest {
        coEvery { v2Repository.searchPlay(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("search failed"))

        val vm = buildViewModel()
        advanceUntilIdle() // feed

        vm.search()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.searching)
        assertTrue(state.hasSearched)
        assertEquals("search failed", state.searchError)
        assertNull(state.searchResult)
    }

    @Test
    fun `search passes sportType and skill filters to repository`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSportTypeChanged("tennis")
        vm.onSkillMinChanged(2)
        vm.onSkillMaxChanged(4)
        vm.search()
        advanceUntilIdle()

        coVerify {
            v2Repository.searchPlay(
                from = any(),
                to = any(),
                sportType = "tennis",
                latitude = null,
                longitude = null,
                radiusKm = null,
                skillLevelMin = 2,
                skillLevelMax = 4,
                onlyEligible = null
            )
        }
    }

    @Test
    fun `empty sportType is passed as null to repository`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSportTypeChanged("") // any sport
        vm.search()
        advanceUntilIdle()

        coVerify {
            v2Repository.searchPlay(
                from = any(),
                to = any(),
                sportType = null, // empty string → null
                latitude = any(),
                longitude = any(),
                radiusKm = any(),
                skillLevelMin = any(),
                skillLevelMax = any(),
                onlyEligible = any()
            )
        }
    }

    // ----------------------------------------------------------
    // Rebook
    // ----------------------------------------------------------

    @Test
    fun `rebook removes bookingId from rebookingIds on success`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle() // feed

        vm.rebook(1L)
        advanceUntilIdle()

        assertFalse(1L in vm.uiState.value.rebookingIds)
        assertNull(vm.uiState.value.rebookError)
    }

    @Test
    fun `rebook triggers a feed refresh on success`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.rebook(1L)
        advanceUntilIdle()

        // getHomeFeed should have been called twice: once in init, once after rebook
        coVerify(atLeast = 2) { v2Repository.getHomeFeed() }
    }

    @Test
    fun `rebook sets rebookError on failure`() = runTest {
        coEvery { v2Repository.rebook(any(), any(), any()) } returns Result.failure(RuntimeException("slot taken"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.rebook(1L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("slot taken", state.rebookError)
        assertFalse(1L in state.rebookingIds)
    }

    // ----------------------------------------------------------
    // Error clearing
    // ----------------------------------------------------------

    @Test
    fun `clearRebookError resets rebookError to null`() = runTest {
        coEvery { v2Repository.rebook(any(), any(), any()) } returns Result.failure(RuntimeException("err"))

        val vm = buildViewModel()
        advanceUntilIdle()
        vm.rebook(1L)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.rebookError)
        vm.clearRebookError()
        assertNull(vm.uiState.value.rebookError)
    }

    @Test
    fun `clearSearchError resets searchError to null`() = runTest {
        coEvery { v2Repository.searchPlay(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("fail"))

        val vm = buildViewModel()
        advanceUntilIdle()
        vm.search()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.searchError)
        vm.clearSearchError()
        assertNull(vm.uiState.value.searchError)
    }
}
