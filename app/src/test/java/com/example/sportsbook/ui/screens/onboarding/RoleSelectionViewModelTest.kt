package com.example.sportsbook.ui.screens.onboarding

import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.repository.UserRepository
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
class RoleSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: UserRepository

    private val stubUser = User(id = 1L, role = UserRole.PLAYER)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = RoleSelectionViewModel(userRepository)

    @Test
    fun `initial state has no role selected and is not loading`() {
        val vm = buildViewModel()
        val state = vm.uiState.value

        assertNull(state.selectedRole)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `selectRole PLAYER calls repository and sets success`() = runTest {
        coEvery { userRepository.setRole(UserRole.PLAYER, null) } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.selectRole(UserRole.PLAYER)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSuccess)
        assertEquals(UserRole.PLAYER, vm.uiState.value.selectedRole)
        assertFalse(vm.uiState.value.isLoading)

        coVerify { userRepository.setRole(UserRole.PLAYER) }
    }

    @Test
    fun `selectRole PARTNER calls repository and sets success`() = runTest {
        coEvery { userRepository.setRole(UserRole.PARTNER, null) } returns
            Result.success(stubUser.copy(role = UserRole.PARTNER))

        val vm = buildViewModel()
        vm.selectRole(UserRole.PARTNER)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSuccess)
        assertEquals(UserRole.PARTNER, vm.uiState.value.selectedRole)
    }

    @Test
    fun `selectRole resets isLoading after completion`() = runTest {
        coEvery { userRepository.setRole(any(), any()) } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.selectRole(UserRole.PLAYER)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test
    fun `selectRole failure sets error message`() = runTest {
        coEvery {
            userRepository.setRole(any(), any())
        } returns Result.failure(Exception("Network error"))

        val vm = buildViewModel()
        vm.selectRole(UserRole.PLAYER)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSuccess)
        assertEquals("Network error", vm.uiState.value.error)
        assertNull(vm.uiState.value.selectedRole)
    }

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery {
            userRepository.setRole(any(), any())
        } returns Result.failure(Exception("Oops"))

        val vm = buildViewModel()
        vm.selectRole(UserRole.PLAYER)
        advanceUntilIdle()

        assertEquals("Oops", vm.uiState.value.error)

        vm.clearError()
        assertNull(vm.uiState.value.error)
    }
}
