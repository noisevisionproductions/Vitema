package com.noisevisionsoftware.fitapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.fitapplication.ui.navigation.Screen
import com.noisevisionsoftware.fitapplication.ui.screens.dashboard.DashboardScreen
import com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister.AuthViewModel
import com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister.ForgotPassword
import com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister.LoginScreen
import com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister.RegisterScreen

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val authState by authViewModel.authState.collectAsState()
    val userSession by authViewModel.userSession.collectAsState(initial = null)
    val uiEventFlow = authViewModel.uiEvent.collectAsState(initial = null)

    LaunchedEffect(userSession) {
        userSession?.let {
            currentScreen = Screen.Dashboard
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                currentScreen = Screen.Dashboard
            }

            is AuthViewModel.AuthState.LoggedOut -> {
                currentScreen = Screen.Login
            }

            else -> {}
        }
    }

    when (currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegistrationClick = { currentScreen = Screen.Register },
                onForgotPasswordClick = { currentScreen = Screen.ForgotPassword },
            )
        }

        is Screen.Register -> {
            RegisterScreen(
                onRegisterClick = { nickname, email, password, confirmPassword ->
                    authViewModel.register(nickname, email, password, confirmPassword)
                },
                onLoginClick = { currentScreen = Screen.Login },
                onRegulationsClick = { },
                onPrivacyPolicyClick = { }
            )
        }

        is Screen.ForgotPassword -> {
            ForgotPassword(
                onBackToLogin = { currentScreen = Screen.Login }
            )
        }

        is Screen.Dashboard -> {
            DashboardScreen(
                onLogoutClick = {authViewModel.logout()},
                onMealPlanClick = {},
                onCaloriesTrackerClick = {},
                onWaterTrackerClick = {},
                onRecipesClick = {},
                onProgressClick = {},
                onSettingsClick = {}
            )
        }
    }
}