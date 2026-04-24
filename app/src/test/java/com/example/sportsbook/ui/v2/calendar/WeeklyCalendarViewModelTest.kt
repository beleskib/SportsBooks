package com.example.sportsbook.ui.v2.calendar

import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.example.sportsbook.domain.repository.VenueRepository
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
// v2-practical-ux: Unit tests for WeeklyCalendarViewModel
// ============================================================

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyCalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var venueRepository: VenueRepository
    private lateinit var coachRepository: CoachRepository
    private lateinit var timeSlotRepository: TimeSlotRepository

    private val stubVenue = Venue(id = 10L, name = "City Court")
    private val stubCoach = Coach(id = 20L, name = "Coach Mike")
    private val stubSlots = listOf(
        TimeSlot(
            id = 1L, venueId = 10L, slotDate = "2026-04-21",
            startTime = "10:00", endTime = "11:00", isAvailable = true
        ),
        TimeSlot(
            id = 2L, venueId = 10L, slotDate = "2026-04-22",
            startTime = "14:00", endTime = "15:00", isAvailable = false
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        venueRepository = mockk()
        coachRepository = mockk()
        timeSlotRepository = mockk()

        coEvery { venueRepository.getMyVenues() } returns Result.success(listOf(stubVenue))
        coEvery { coachRepository.getMyCoachProfile() } returns Result.success(null)
        coEvery {
            timeSlotRepository.getAvailableSlots(
                venueId = any(), coachId = any(), dateFrom = any(), dateTo = any()
            )
        } returns Result.success(stubSlots)
        coEvery { timeSlotRepository.generateSlots(any(), any(), any(), any(), any(), any(), any()) } returns
            Result.success(emptyList())
        coEvery { timeSlotRepository.deleteSlot(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() =
        WeeklyCalendarViewModel(venueRepository, coachRepository, timeSlotRepository)

    // ----------------------------------------------------------
    // Entity loading
    // ----------------------------------------------------------

    @Test
    fun `init loads venues and defaults selection to first venue`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertEquals(listOf(stubVenue), state.venues)
        assertEquals(V2EntityType.VENUE, state.entityType)
        assertEquals(stubVenue.id, state.selectedEntityId)
    }

    @Test
    fun `init defaults to COACH type when no venues available`() = runTest {
        coEvery { venueRepository.getMyVenues() } returns Result.success(emptyList())
        coEvery { coachRepository.getMyCoachProfile() } returns Result.success(stubCoach)

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(V2EntityType.COACH, state.entityType)
        assertEquals(stubCoach.id, state.selectedEntityId)
    }

    @Test
    fun `init loads slots after entities are resolved`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        coVerify {
            timeSlotRepository.getAvailableSlots(
                venueId = stubVenue.id,
                coachId = null,
                dateFrom = any(),
                dateTo = any()
            )
        }

        assertEquals(2, vm.uiState.value.slots.size)
    }

    @Test
    fun `init loading starts true and clears on completion`() = runTest {
        val vm = buildViewModel()
        assertTrue(vm.uiState.value.loading)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.loading)
    }

    // ----------------------------------------------------------
    // Entity selector
    // ----------------------------------------------------------

    @Test
    fun `selectEntityType COACH sets entityType and reloads slots with coach id`() = runTest {
        coEvery { coachRepository.getMyCoachProfile() } returns Result.success(stubCoach)
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.selectEntityType(V2EntityType.COACH)
        advanceUntilIdle()

        assertEquals(V2EntityType.COACH, vm.uiState.value.entityType)
        coVerify {
            timeSlotRepository.getAvailableSlots(
                venueId = null,
                coachId = stubCoach.id,
                dateFrom = any(),
                dateTo = any()
            )
        }
    }

    @Test
    fun `selectEntity updates selectedEntityId and reloads slots`() = runTest {
        val venue2 = Venue(id = 11L, name = "Arena")
        coEvery { venueRepository.getMyVenues() } returns Result.success(listOf(stubVenue, venue2))
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.selectEntity(11L)
        advanceUntilIdle()

        assertEquals(11L, vm.uiState.value.selectedEntityId)
        coVerify {
            timeSlotRepository.getAvailableSlots(
                venueId = 11L,
                coachId = null,
                dateFrom = any(),
                dateTo = any()
            )
        }
    }

    // ----------------------------------------------------------
    // Week navigation
    // ----------------------------------------------------------

    @Test
    fun `prevWeek moves weekStart back 7 days`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        val before = vm.uiState.value.weekStart

        vm.prevWeek()
        advanceUntilIdle()

        assertEquals(before.minusDays(7), vm.uiState.value.weekStart)
    }

    @Test
    fun `nextWeek moves weekStart forward 7 days`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        val before = vm.uiState.value.weekStart

        vm.nextWeek()
        advanceUntilIdle()

        assertEquals(before.plusDays(7), vm.uiState.value.weekStart)
    }

    @Test
    fun `goToToday resets weekStart to current Monday`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.nextWeek()
        advanceUntilIdle()

        vm.goToToday()
        advanceUntilIdle()

        val state = vm.uiState.value
        // weekStart should be this week's Monday
        assertEquals(java.time.DayOfWeek.MONDAY, state.weekStart.dayOfWeek)
    }

    // ----------------------------------------------------------
    // Cell interactions
    // ----------------------------------------------------------

    @Test
    fun `addSlot calls generateSlots with correct params`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.addSlot("2026-04-25", 10)
        advanceUntilIdle()

        coVerify {
            timeSlotRepository.generateSlots(
                venueId = stubVenue.id,
                coachId = null,
                dateFrom = "2026-04-25",
                dateTo = "2026-04-25",
                startHour = 10,
                endHour = 11,
                daysOfWeek = null
            )
        }
    }

    @Test
    fun `addSlot generates endHour as startHour plus one`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.addSlot("2026-04-25", 14)
        advanceUntilIdle()

        coVerify {
            timeSlotRepository.generateSlots(
                venueId = any(),
                coachId = any(),
                dateFrom = any(),
                dateTo = any(),
                startHour = 14,
                endHour = 15,
                daysOfWeek = any()
            )
        }
    }

    @Test
    fun `removeSlot calls deleteSlot and reloads`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.removeSlot(1L)
        advanceUntilIdle()

        coVerify { timeSlotRepository.deleteSlot(1L) }
        // Reload was triggered — getAvailableSlots called at least twice (init + after remove)
        coVerify(atLeast = 2) {
            timeSlotRepository.getAvailableSlots(any(), any(), any(), any())
        }
    }

    @Test
    fun `addSlot sets error when generateSlots fails`() = runTest {
        coEvery { timeSlotRepository.generateSlots(any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("conflict"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.addSlot("2026-04-25", 10)
        advanceUntilIdle()

        assertEquals("conflict", vm.uiState.value.error)
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery { timeSlotRepository.generateSlots(any(), any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("err"))

        val vm = buildViewModel()
        advanceUntilIdle()
        vm.addSlot("2026-04-25", 10)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        vm.clearError()
        assertNull(vm.uiState.value.error)
    }
}
