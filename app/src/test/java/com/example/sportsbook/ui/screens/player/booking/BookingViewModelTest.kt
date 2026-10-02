package com.example.sportsbook.ui.screens.player.booking

import androidx.lifecycle.SavedStateHandle
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.repository.BookingRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var timeSlotRepository: TimeSlotRepository
    private lateinit var bookingRepository: BookingRepository
    private lateinit var venueRepository: VenueRepository
    private lateinit var coachRepository: CoachRepository

    private val stubVenue = Venue(
        id = 10L,
        name = "City Court",
        sportType = SportType.BASKETBALL,
        pricePerHour = 500.0,
        address = "123 Main St",
    )

    private val stubSlots = listOf(
        TimeSlot(id = 1L, venueId = 10L, slotDate = "2026-11-01", startTime = "09:00", endTime = "10:00", isAvailable = true),
        TimeSlot(id = 2L, venueId = 10L, slotDate = "2026-11-01", startTime = "10:00", endTime = "11:00", isAvailable = true),
        TimeSlot(id = 3L, venueId = 10L, slotDate = "2026-11-01", startTime = "11:00", endTime = "12:00", isAvailable = false),
    )

    private val stubBooking = Booking(
        id = 100L,
        playerId = 1L,
        timeSlotId = 1L,
        venueId = 10L,
        totalPrice = 500.0,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        timeSlotRepository = mockk(relaxed = true)
        bookingRepository = mockk(relaxed = true)
        venueRepository = mockk(relaxed = true)
        coachRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        venueId: Long? = 10L,
        coachId: Long? = null,
        stubSlots: Boolean = true,
    ): BookingViewModel {
        val savedState = SavedStateHandle().apply {
            venueId?.let { set("venueId", it) }
            coachId?.let { set("coachId", it) }
        }

        coEvery { venueRepository.getVenueById(any()) } returns Result.success(stubVenue)
        if (stubSlots) {
            coEvery {
                timeSlotRepository.getAvailableSlots(any(), any(), any(), any())
            } returns Result.success(this.stubSlots)
        }

        return BookingViewModel(timeSlotRepository, bookingRepository, venueRepository, coachRepository, savedState)
    }

    // ── Init ────────────────────────────────────────────────

    @Test
    fun `init sets venueId and loads slots for today`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        assertEquals(10L, vm.uiState.value.venueId)
        assertTrue(vm.uiState.value.selectedDate.isNotBlank())
        assertEquals(3, vm.uiState.value.availableSlots.size)
    }

    @Test
    fun `init loads venue details`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        assertEquals("City Court", vm.uiState.value.venue?.name)
        assertEquals("City Court", vm.uiState.value.entityName)
    }

    // ── Date change ─────────────────────────────────────────

    @Test
    fun `onDateChange updates date and reloads slots`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onDateChange("2026-11-15")
        advanceUntilIdle()

        assertEquals("2026-11-15", vm.uiState.value.selectedDate)
        assertNull(vm.uiState.value.selectedSlotId)
        assertNull(vm.uiState.value.selectedSlot)
        coVerify(atLeast = 2) {
            timeSlotRepository.getAvailableSlots(venueId = 10L, coachId = null, any(), any())
        }
    }

    @Test
    fun `onDateChange clears previously selected slot`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(1L)
        assertEquals(1L, vm.uiState.value.selectedSlotId)

        vm.onDateChange("2026-11-20")
        assertNull(vm.uiState.value.selectedSlotId)
    }

    // ── Slot selection ──────────────────────────────────────

    @Test
    fun `onSlotSelected sets selectedSlotId and selectedSlot`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(2L)
        assertEquals(2L, vm.uiState.value.selectedSlotId)
        assertEquals("10:00", vm.uiState.value.selectedSlot?.startTime)
    }

    @Test
    fun `onSlotSelected with unknown id sets selectedSlot to null`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(999L)
        assertEquals(999L, vm.uiState.value.selectedSlotId)
        assertNull(vm.uiState.value.selectedSlot)
    }

    // ── Notes ───────────────────────────────────────────────

    @Test
    fun `onNotesChange updates notes`() = runTest {
        val vm = buildViewModel()
        vm.onNotesChange("Bring extra balls")
        assertEquals("Bring extra balls", vm.uiState.value.notes)
    }

    // ── Confirm booking ─────────────────────────────────────

    @Test
    fun `confirmBooking succeeds and sets bookingSuccess true`() = runTest {
        coEvery {
            bookingRepository.createBooking(any(), any())
        } returns Result.success(stubBooking)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(1L)
        vm.confirmBooking()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.bookingSuccess)
        assertEquals(100L, vm.uiState.value.createdBookingId)
        assertFalse(vm.uiState.value.isBookingLoading)
    }

    @Test
    fun `confirmBooking passes notes to repository`() = runTest {
        coEvery {
            bookingRepository.createBooking(any(), any())
        } returns Result.success(stubBooking)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(1L)
        vm.onNotesChange("Please prepare the court")
        vm.confirmBooking()
        advanceUntilIdle()

        coVerify { bookingRepository.createBooking(1L, "Please prepare the court") }
    }

    @Test
    fun `confirmBooking with blank notes passes null`() = runTest {
        coEvery {
            bookingRepository.createBooking(any(), any())
        } returns Result.success(stubBooking)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(1L)
        vm.onNotesChange("")
        vm.confirmBooking()
        advanceUntilIdle()

        coVerify { bookingRepository.createBooking(1L, null) }
    }

    @Test
    fun `confirmBooking does nothing when no slot selected`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.confirmBooking()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.bookingSuccess)
        coVerify(exactly = 0) { bookingRepository.createBooking(any(), any()) }
    }

    @Test
    fun `confirmBooking failure sets error`() = runTest {
        coEvery {
            bookingRepository.createBooking(any(), any())
        } returns Result.failure(Exception("Slot unavailable"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(1L)
        vm.confirmBooking()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.bookingSuccess)
        assertEquals("Slot unavailable", vm.uiState.value.error)
    }

    @Test
    fun `confirmBooking resets isBookingLoading after completion`() = runTest {
        coEvery {
            bookingRepository.createBooking(any(), any())
        } returns Result.success(stubBooking)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onSlotSelected(1L)
        vm.confirmBooking()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isBookingLoading)
        assertTrue(vm.uiState.value.bookingSuccess)
    }

    // ── Slot loading errors ─────────────────────────────────

    @Test
    fun `loadSlots failure sets error message`() = runTest {
        coEvery {
            timeSlotRepository.getAvailableSlots(any(), any(), any(), any())
        } returns Result.failure(Exception("Network error"))

        val vm = buildViewModel(stubSlots = false)
        advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        assertTrue(vm.uiState.value.availableSlots.isEmpty())
    }

    // ── Clear error ─────────────────────────────────────────

    @Test
    fun `clearError resets error`() = runTest {
        coEvery {
            timeSlotRepository.getAvailableSlots(any(), any(), any(), any())
        } returns Result.failure(Exception("Fail"))

        val vm = buildViewModel(stubSlots = false)
        advanceUntilIdle()

        assertEquals("Fail", vm.uiState.value.error)
        vm.clearError()
        assertNull(vm.uiState.value.error)
    }

    // ── loadSlotById ────────────────────────────────────────

    @Test
    fun `loadSlotById populates slot and loads entity`() = runTest {
        val slot = TimeSlot(id = 5L, venueId = 10L, slotDate = "2026-11-05", startTime = "14:00", endTime = "15:00")
        coEvery { timeSlotRepository.getSlotById(5L) } returns Result.success(slot)

        val vm = buildViewModel(venueId = null)
        advanceUntilIdle()

        vm.loadSlotById(5L)
        advanceUntilIdle()

        assertEquals(5L, vm.uiState.value.selectedSlotId)
        assertEquals("14:00", vm.uiState.value.selectedSlot?.startTime)
        assertEquals(10L, vm.uiState.value.venueId)
    }
}
