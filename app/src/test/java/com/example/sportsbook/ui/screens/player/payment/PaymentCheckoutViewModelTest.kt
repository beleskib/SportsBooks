package com.example.sportsbook.ui.screens.player.payment

import androidx.lifecycle.SavedStateHandle
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.enums.PaymentStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.domain.model.PaymentIntentResponse
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.FriendshipRepository
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.domain.repository.PaymentRepository
import com.example.sportsbook.domain.repository.RedeemXpResponse
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
class PaymentCheckoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var gamificationRepository: GamificationRepository
    private lateinit var bookingRepository: BookingRepository
    private lateinit var friendshipRepository: FriendshipRepository
    private lateinit var apiService: ApiService

    private val stubBooking = Booking(
        id = 50L,
        playerId = 1L,
        timeSlotId = 10L,
        venueId = 5L,
        status = BookingStatus.APPROVED,
        totalPrice = 1000.0,
        venue = Venue(
            id = 5L,
            name = "Star Arena",
            sportType = SportType.BASKETBALL,
            pricePerHour = 1000.0,
            address = "100 Court St"
        ),
        timeSlot = TimeSlot(
            id = 10L,
            venueId = 5L,
            slotDate = "2026-11-10",
            startTime = "14:00",
            endTime = "15:00",
        ),
    )

    private val stubLevel = PlayerLevel(
        id = 1L,
        userId = 1L,
        totalXp = 500,
        currentLevel = 3,
        xpToNextLevel = 400,
    )

    private val stubFriends = listOf(
        Friendship(id = 1L, friendId = 10L, friendName = "Alice"),
        Friendship(id = 2L, friendId = 11L, friendName = "Bob"),
        Friendship(id = 3L, friendId = 12L, friendName = "Charlie"),
    )

    private val stubIntentResponse = PaymentIntentResponse(
        clientSecret = "dev_secret_123",
        bookingId = 50L,
        paymentId = 99L,
        amount = 1000.0,
        currency = "MKD",
    )

    private val stubPayment = Payment(
        id = 99L,
        bookingId = 50L,
        payerId = 1L,
        amount = 1000.0,
        status = PaymentStatus.COMPLETED,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        paymentRepository = mockk(relaxed = true)
        gamificationRepository = mockk(relaxed = true)
        bookingRepository = mockk(relaxed = true)
        friendshipRepository = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(bookingId: Long = 50L): PaymentCheckoutViewModel {
        val savedState = SavedStateHandle().apply {
            set("bookingId", bookingId)
        }

        coEvery { bookingRepository.getBookingById(any()) } returns Result.success(stubBooking)
        coEvery { gamificationRepository.getMyLevel() } returns Result.success(stubLevel)
        coEvery { friendshipRepository.getMyFriends() } returns Result.success(stubFriends)

        return PaymentCheckoutViewModel(
            paymentRepository,
            gamificationRepository,
            bookingRepository,
            friendshipRepository,
            apiService,
            savedState,
        )
    }

    // ── Init / Loading ─────────────────────────────────────────

    @Test
    fun `init loads booking details`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(50L, state.bookingId)
        assertEquals(1000.0, state.amount, 0.01)
        assertEquals("Star Arena", state.venueName)
        assertEquals("2026-11-10", state.slotDate)
        assertFalse(state.isLoadingBooking)
    }

    @Test
    fun `init loads XP balance`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        assertEquals(500, vm.uiState.value.availableXp)
        assertEquals(3, vm.uiState.value.currentLevel)
    }

    @Test
    fun `init loads friends list`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        assertEquals(3, vm.uiState.value.friends.size)
        assertEquals(3, vm.uiState.value.filteredFriends.size)
        assertFalse(vm.uiState.value.isLoadingFriends)
    }

    @Test
    fun `loadBookingDetails failure sets error`() = runTest {
        coEvery { bookingRepository.getBookingById(any()) } returns Result.failure(Exception("Not found"))
        coEvery { gamificationRepository.getMyLevel() } returns Result.success(stubLevel)
        coEvery { friendshipRepository.getMyFriends() } returns Result.success(stubFriends)

        val savedState = SavedStateHandle().apply { set("bookingId", 50L) }
        val vm = PaymentCheckoutViewModel(paymentRepository, gamificationRepository, bookingRepository, friendshipRepository, apiService, savedState)
        advanceUntilIdle()

        assertEquals("Not found", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoadingBooking)
    }

    // ── XP Redemption ──────────────────────────────────────────

    @Test
    fun `onXpSliderChange updates xpToRedeem and xpDiscount`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onXpSliderChange(200)
        assertEquals(200, vm.uiState.value.xpToRedeem)
        assertEquals(2.0, vm.uiState.value.xpDiscount, 0.01)
    }

    @Test
    fun `setQuickXp clamps to available XP`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.setQuickXp(9999)
        assertEquals(500, vm.uiState.value.xpToRedeem)
        assertEquals(5.0, vm.uiState.value.xpDiscount, 0.01)
    }

    @Test
    fun `redeemXp success updates state`() = runTest {
        coEvery {
            gamificationRepository.redeemXp(any(), any())
        } returns Result.success(
            RedeemXpResponse(xpSpent = 200, discountAmount = 2.0, remainingXp = 300, newLevel = 3)
        )

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onXpSliderChange(200)
        vm.redeemXp()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.xpRedeemed)
        assertEquals(2.0, state.xpDiscount, 0.01)
        assertEquals(200, state.xpToRedeem)
        assertEquals(300, state.availableXp)
        assertEquals(3, state.currentLevel)
        assertFalse(state.isRedeemingXp)
    }

    @Test
    fun `redeemXp failure sets error`() = runTest {
        coEvery {
            gamificationRepository.redeemXp(any(), any())
        } returns Result.failure(Exception("Insufficient XP"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onXpSliderChange(200)
        vm.redeemXp()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.xpRedeemed)
        assertEquals("Insufficient XP", vm.uiState.value.error)
    }

    @Test
    fun `redeemXp does nothing when xpToRedeem is 0`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.redeemXp()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.xpRedeemed)
        coVerify(exactly = 0) { gamificationRepository.redeemXp(any(), any()) }
    }

    @Test
    fun `redeemXp does nothing when already redeemed`() = runTest {
        coEvery {
            gamificationRepository.redeemXp(any(), any())
        } returns Result.success(
            RedeemXpResponse(xpSpent = 100, discountAmount = 1.0, remainingXp = 400, newLevel = 3)
        )

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onXpSliderChange(100)
        vm.redeemXp()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.xpRedeemed)

        vm.onXpSliderChange(50)
        vm.redeemXp()
        advanceUntilIdle()

        coVerify(exactly = 1) { gamificationRepository.redeemXp(any(), any()) }
    }

    // ── Split Payment ──────────────────────────────────────────

    @Test
    fun `toggleSplit enables split mode`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.toggleSplit(true)
        assertTrue(vm.uiState.value.splitEnabled)

        vm.toggleSplit(false)
        assertFalse(vm.uiState.value.splitEnabled)
        assertTrue(vm.uiState.value.selectedFriends.isEmpty())
    }

    @Test
    fun `toggleFriendSelection adds and removes friends`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.toggleSplit(true)
        vm.toggleFriendSelection(stubFriends[0])
        assertEquals(1, vm.uiState.value.selectedFriends.size)
        assertTrue(vm.isFriendSelected(stubFriends[0]))

        vm.toggleFriendSelection(stubFriends[1])
        assertEquals(2, vm.uiState.value.selectedFriends.size)

        vm.toggleFriendSelection(stubFriends[0])
        assertEquals(1, vm.uiState.value.selectedFriends.size)
        assertFalse(vm.isFriendSelected(stubFriends[0]))
    }

    @Test
    fun `yourShare calculates correctly for split`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.toggleSplit(true)
        vm.toggleFriendSelection(stubFriends[0])
        val share = vm.uiState.value.yourShare
        assertEquals(500.0, share, 0.01)
    }

    @Test
    fun `yourShare includes XP discount before splitting`() = runTest {
        coEvery {
            gamificationRepository.redeemXp(any(), any())
        } returns Result.success(
            RedeemXpResponse(xpSpent = 200, discountAmount = 2.0, remainingXp = 300, newLevel = 3)
        )

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onXpSliderChange(200)
        vm.redeemXp()
        advanceUntilIdle()

        vm.toggleSplit(true)
        vm.toggleFriendSelection(stubFriends[0])

        val share = vm.uiState.value.yourShare
        assertEquals(499.0, share, 0.01)
    }

    @Test
    fun `onFriendSearchQueryChange filters friends`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onFriendSearchQueryChange("Ali")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.filteredFriends.size)
        assertEquals("Alice", vm.uiState.value.filteredFriends[0].friendName)
    }

    @Test
    fun `onFriendSearchQueryChange with blank shows all friends`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onFriendSearchQueryChange("Ali")
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.filteredFriends.size)

        vm.onFriendSearchQueryChange("")
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.filteredFriends.size)
    }

    // ── Payment Flow ───────────────────────────────────────────

    @Test
    fun `initiatePayment creates intent and confirms`() = runTest {
        coEvery { paymentRepository.createPaymentIntent(50L) } returns Result.success(stubIntentResponse)
        coEvery { paymentRepository.confirmPayment(99L) } returns Result.success(stubPayment)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.initiatePayment()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.paymentSuccess)
        assertEquals(99L, state.paymentId)
        assertFalse(state.isProcessingPayment)
        assertFalse(state.isCreatingIntent)
    }

    @Test
    fun `initiatePayment createIntent failure sets error`() = runTest {
        coEvery {
            paymentRepository.createPaymentIntent(any())
        } returns Result.failure(Exception("Payment gateway error"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.initiatePayment()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.paymentSuccess)
        assertEquals("Payment gateway error", vm.uiState.value.error)
    }

    @Test
    fun `initiatePayment confirmPayment failure sets error`() = runTest {
        coEvery { paymentRepository.createPaymentIntent(50L) } returns Result.success(stubIntentResponse)
        coEvery {
            paymentRepository.confirmPayment(99L)
        } returns Result.failure(Exception("Confirmation failed"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.initiatePayment()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.paymentSuccess)
        assertEquals("Confirmation failed", vm.uiState.value.error)
    }

    // ── Clear error ────────────────────────────────────────────

    @Test
    fun `clearError resets error`() = runTest {
        coEvery { bookingRepository.getBookingById(any()) } returns Result.failure(Exception("Err"))
        coEvery { gamificationRepository.getMyLevel() } returns Result.success(stubLevel)
        coEvery { friendshipRepository.getMyFriends() } returns Result.success(stubFriends)

        val savedState = SavedStateHandle().apply { set("bookingId", 50L) }
        val vm = PaymentCheckoutViewModel(paymentRepository, gamificationRepository, bookingRepository, friendshipRepository, apiService, savedState)
        advanceUntilIdle()

        assertEquals("Err", vm.uiState.value.error)
        vm.clearError()
        assertNull(vm.uiState.value.error)
    }
}
