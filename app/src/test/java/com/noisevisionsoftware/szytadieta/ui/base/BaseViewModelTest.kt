package com.noisevisionsoftware.szytadieta.ui.base

import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import com.noisevisionsoftware.szytadieta.domain.alert.Alert
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val networkManager: NetworkConnectivityManager = mockk()
    private val alertManager: AlertManager = mockk()
    private val eventBus: EventBus = mockk()
    private val networkStatusFlow = MutableStateFlow(true)

    init {
        every { networkManager.isNetworkConnected } returns networkStatusFlow
        coEvery { alertManager.showAlert(any()) } just Runs
    }

    private class TestViewModel(
        networkManager: NetworkConnectivityManager,
        alertManager: AlertManager,
        eventBus: EventBus
    ) : BaseViewModel(networkManager, alertManager, eventBus)

    @Test
    fun showErrorWithNetworkAvailableShouldShowErrorAlert() = runTest {
        val viewModel = TestViewModel(networkManager, alertManager, eventBus)
        val errorMessage = "Test error"

        viewModel.showError(errorMessage)
        advanceUntilIdle()

        coVerify {
            alertManager.showAlert(withArg { alert ->
                assertTrue(alert is Alert.Error)
                assertEquals(errorMessage, alert.message)
            })
        }
    }

    @Test
    fun showErrorWithoutNetworkShouldShowNetworkError() = runTest {
        val viewModel = TestViewModel(networkManager, alertManager, eventBus)
        networkStatusFlow.value = false
        advanceUntilIdle()

        viewModel.showError("Test error")
        advanceUntilIdle()

        coVerify {
            alertManager.showAlert(withArg { alert ->
                assertTrue(alert is Alert.Error)
                assertEquals("Brak połączenia z internetem", alert.message)
            })
        }
    }

    @Test
    fun showSuccessWithNetworkAvailableShouldShowSuccessAlert() = runTest {
        val viewModel = TestViewModel(networkManager, alertManager, eventBus)
        val successMessage = "Test success"

        viewModel.showSuccess(successMessage)
        advanceUntilIdle()

        coVerify {
            alertManager.showAlert(withArg { alert ->
                assertTrue(alert is Alert.Success)
                assertEquals(successMessage, alert.message)
            })
        }
    }

    @Test
    fun showSuccessWithoutNetworkShouldShowNetworkError() = runTest {
        val viewModel = TestViewModel(networkManager, alertManager, eventBus)
        networkStatusFlow.value = false
        advanceUntilIdle()

        viewModel.showSuccess("Test success")
        advanceUntilIdle()

        coVerify {
            alertManager.showAlert(withArg { alert ->
                assertTrue(alert is Alert.Error)
                assertEquals("Brak połączenia z internetem", alert.message)
            })
        }
    }

    @Test
    fun safeApiCallShouldReturnFailureWhenNetworkIsUnavailable() = runTest {
        val viewModel = TestViewModel(networkManager, alertManager, eventBus)
        networkStatusFlow.value = false
        advanceUntilIdle()

        val result = viewModel.safeApiCall { Result.success(true) }

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppException.NetworkException)
        if (exception != null) {
            assertEquals("Brak połączenia z internetem", exception.message)
        }
    }

    @Test
    fun safeApiCallShouldReturnSuccessWhenNetworkIsAvailable() = runTest {
        val viewModel = TestViewModel(networkManager, alertManager, eventBus)
        networkStatusFlow.value = true
        advanceUntilIdle()

        val result = viewModel.safeApiCall { Result.success(true) }

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun shouldShowNetworkErrorWhenNetworkBecomesUnavailable() = runTest {
        TestViewModel(networkManager, alertManager, eventBus)
        advanceUntilIdle()

        networkStatusFlow.value = false
        advanceUntilIdle()

        coVerify {
            alertManager.showAlert(withArg { alert ->
                assertTrue(alert is Alert.Error)
                assertEquals("Brak połączenia z internetem", alert.message)
            })
        }
    }
}