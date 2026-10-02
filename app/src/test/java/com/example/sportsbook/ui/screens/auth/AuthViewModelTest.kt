package com.example.sportsbook.ui.screens.auth

import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.repository.AuthRepository
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    private val stubUser = User(
        id = 1L,
        firebaseUid = "uid-123",
        email = "test@test.com",
        displayName = "Test User",
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): AuthViewModel {
        return AuthViewModel(authRepository).also { viewModel = it }
    }

    // ── Input field updates ─────────────────────────────────

    @Test
    fun `onEmailChange updates email in uiState`() {
        val vm = buildViewModel()
        vm.onEmailChange("hello@example.com")
        assertEquals("hello@example.com", vm.uiState.value.email)
    }

    @Test
    fun `onPasswordChange updates password in uiState`() {
        val vm = buildViewModel()
        vm.onPasswordChange("secret123")
        assertEquals("secret123", vm.uiState.value.password)
    }

    @Test
    fun `onDisplayNameChange updates displayName in uiState`() {
        val vm = buildViewModel()
        vm.onDisplayNameChange("John")
        assertEquals("John", vm.uiState.value.displayName)
    }

    // ── Sign In ─────────────────────────────────────────────

    @Test
    fun `signIn resets isLoading after completion`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.onEmailChange("test@test.com")
        vm.onPasswordChange("password")
        vm.signIn()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test
    fun `signIn with valid credentials sets isSuccess true`() = runTest {
        coEvery { authRepository.signInWithEmail("test@test.com", "pass") } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.onEmailChange("test@test.com")
        vm.onPasswordChange("pass")
        vm.signIn()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSuccess)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `signIn with invalid credentials sets error message`() = runTest {
        coEvery {
            authRepository.signInWithEmail(any(), any())
        } returns Result.failure(Exception("Invalid password"))

        val vm = buildViewModel()
        vm.onEmailChange("test@test.com")
        vm.onPasswordChange("wrong")
        vm.signIn()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSuccess)
        assertEquals("Invalid password", vm.uiState.value.error)
    }

    @Test
    fun `signIn calls repository with correct email and password`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.onEmailChange("user@app.com")
        vm.onPasswordChange("mypass")
        vm.signIn()
        advanceUntilIdle()

        coVerify { authRepository.signInWithEmail("user@app.com", "mypass") }
    }

    // ── Sign Up ─────────────────────────────────────────────

    @Test
    fun `signUp with valid data sets isSuccess true`() = runTest {
        coEvery {
            authRepository.signUpWithEmail(any(), any(), any())
        } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.onEmailChange("new@test.com")
        vm.onPasswordChange("pass123")
        vm.onDisplayNameChange("New User")
        vm.signUp()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSuccess)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `signUp failure sets error message`() = runTest {
        coEvery {
            authRepository.signUpWithEmail(any(), any(), any())
        } returns Result.failure(Exception("Email already in use"))

        val vm = buildViewModel()
        vm.onEmailChange("existing@test.com")
        vm.onPasswordChange("pass")
        vm.onDisplayNameChange("Dup")
        vm.signUp()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSuccess)
        assertEquals("Email already in use", vm.uiState.value.error)
    }

    @Test
    fun `signUp calls repository with displayName`() = runTest {
        coEvery { authRepository.signUpWithEmail(any(), any(), any()) } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.onEmailChange("e@t.com")
        vm.onPasswordChange("p")
        vm.onDisplayNameChange("MyName")
        vm.signUp()
        advanceUntilIdle()

        coVerify { authRepository.signUpWithEmail("e@t.com", "p", "MyName") }
    }

    // ── Google Sign In ──────────────────────────────────────

    @Test
    fun `signInWithGoogle success sets isSuccess true`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } returns Result.success(stubUser)

        val vm = buildViewModel()
        vm.signInWithGoogle("google-id-token")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test
    fun `signInWithGoogle failure sets error`() = runTest {
        coEvery {
            authRepository.signInWithGoogle(any())
        } returns Result.failure(Exception("Google sign in failed"))

        val vm = buildViewModel()
        vm.signInWithGoogle("bad-token")
        advanceUntilIdle()

        assertEquals("Google sign in failed", vm.uiState.value.error)
    }

    // ── Clear error ─────────────────────────────────────────

    @Test
    fun `clearError resets error to null`() = runTest {
        coEvery {
            authRepository.signInWithEmail(any(), any())
        } returns Result.failure(Exception("Oops"))

        val vm = buildViewModel()
        vm.signIn()
        advanceUntilIdle()

        assertEquals("Oops", vm.uiState.value.error)

        vm.clearError()
        assertNull(vm.uiState.value.error)
    }
}
