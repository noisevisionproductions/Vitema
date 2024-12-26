package com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.noisevisionsoftware.fitapplication.HiltTestApplication_Application
import com.noisevisionsoftware.fitapplication.MainDispatcherRule
import com.noisevisionsoftware.fitapplication.domain.auth.AuthRepository
import com.noisevisionsoftware.fitapplication.domain.model.User
import com.noisevisionsoftware.fitapplication.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.fitapplication.ui.common.UiEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@HiltAndroidTest
@Config(application = HiltTestApplication_Application::class)
@RunWith(RobolectricTestRunner::class)
class AuthViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var repository: AuthRepository
    private lateinit var networkManager: NetworkConnectivityManager

    private val email = "test@example.com"
    private val password = "password123"
    private val nickname = "testUser"
    private val userId = "testUserId"
    private val testUser = User(
        id = userId,
        email = email,
        nickname = nickname,
        createdAt = 123456789
    )

    @Before
    fun setUp() {
        hiltRule.inject()

        repository = mockk(relaxed = false)
        networkManager = mockk(relaxed = true)

        coEvery { networkManager.isNetworkConnected } returns flowOf(true)
        every { networkManager.isCurrentlyConnected() } returns true

        viewModel = AuthViewModel(repository, networkManager)
    }

    @Test
    fun login_ShouldUpdateStateToSuccess_WhenCredentialsAreValid() = runTest {
        coEvery { repository.login(email, password) } returns Result.success(testUser)

        val authStateJob = launch {
            viewModel.authState.test {
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Success(testUser))
                cancelAndIgnoreRemainingEvents()
            }
        }

        val uiEventJob = launch {
            viewModel.uiEvent.test {
                assertThat(awaitItem()).isEqualTo(UiEvent.ShowSuccess("Zalogowano pomyślnie"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        viewModel.login(email, password)

        authStateJob.join()
        uiEventJob.join()
    }

    @Test
    fun register_ShouldEmitError_WhenPasswordsDoesNotMatch() = runTest {
        coEvery { repository.register(nickname, email, password) } returns Result.success(testUser)
        val authStateJob = launch {
            viewModel.authState.test {
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

                viewModel.register(nickname, email, password, "different_password")

                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Error("Hasła nie są identyczne"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        authStateJob.join()
        coVerify(exactly = 0) { repository.register(any(), any(), any()) }
    }

    @Test
    fun getCurrentUser_ShouldUpdateStateToSuccess_WhenUserExists() = runTest {
        coEvery { repository.getCurrentUserData() } returns Result.success(testUser)

        viewModel.authState.test {
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

            viewModel.getCurrentUser()

            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
            assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Success(testUser))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun register_ShouldEmitError_WhenFieldsAreEmpty() = runTest {
        val authStateJob = launch {
            viewModel.authState.test {
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

                viewModel.register("", "", "", "")

                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Error("Wypełnij wszystkie pola"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        val uiEventJob = launch {
            viewModel.uiEvent.test {
                assertThat(awaitItem()).isEqualTo(UiEvent.ShowError("Wypełnij wszystkie pola"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        authStateJob.join()
        uiEventJob.join()
    }

    @Test
    fun resetPassword_ShouldEmitSuccessEvent_WhenEmailIsValid() = runTest {
        coEvery { repository.resetPassword(email) } returns Result.success(Unit)

        val authStateJob = launch {
            viewModel.authState.test {
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)

                viewModel.resetPassword(email)

                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Loading)
                assertThat(awaitItem()).isEqualTo(AuthViewModel.AuthState.Initial)
                cancelAndIgnoreRemainingEvents()
            }
        }

        val uiEventJob = launch {
            viewModel.uiEvent.test {
                assertThat(awaitItem()).isEqualTo(
                    UiEvent.ShowSuccess("Link do resetowania hasła został wysłany na podany adres email")
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        authStateJob.join()
        uiEventJob.join()
    }

    @Test
    fun logout_ShouldCallRepository() = runTest {
        coEvery { repository.logout() } returns Result.success(Unit)

        viewModel.logout()

        advanceUntilIdle()

        coVerify(exactly = 1) { repository.logout() }
    }

    @Test
    fun logout_ShouldEmitErrorEvent_WhenLogoutFails() = runTest {
        coEvery { repository.logout() } returns Result.failure(Exception("Test error"))

        coroutineScope {
            val uiEventJob = launch {
                viewModel.uiEvent.test {
                    viewModel.logout()
                    assertThat(awaitItem()).isEqualTo(UiEvent.ShowError("Błąd podczas wylogowywania"))
                    cancelAndIgnoreRemainingEvents()
                }
            }
            uiEventJob.join()
        }

        coVerify(exactly = 1) { repository.logout() }
    }
}