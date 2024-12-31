package com.noisevisionsoftware.szytadieta.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.ui.navigation.Screen
import com.noisevisionsoftware.szytadieta.ui.screens.admin.AdminPanelScreen
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.BodyMeasurementsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.DashboardScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.AuthViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.ForgotPassword
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.LoginScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.RegisterScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.UserProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile.CompleteProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile.CompleteProfileViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.settings.SettingsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.splash.SplashScreen
import com.noisevisionsoftware.szytadieta.ui.screens.weight.WeightScreen

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    completeProfileViewModel: CompleteProfileViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val authState by authViewModel.authState.collectAsState()
    val userSession by authViewModel.userSession.collectAsState(initial = null)
    val profileState by completeProfileViewModel.profileState.collectAsState()

    LaunchedEffect(profileState) {
        if (profileState is CompleteProfileViewModel.CompleteProfileState.Success && !completeProfileViewModel.profileUpdateMessageShown) {
            completeProfileViewModel.profileUpdateMessageShown = true
            currentScreen = Screen.Dashboard
        }
    }
    HandleBackButton(
        currentScreen = currentScreen,
        userSession = userSession,
        authState = authState,
        onScreenChange = { screen -> currentScreen = screen }
    )

    LaunchedEffect(authState, userSession) {
        when {
            userSession != null || authState is AuthViewModel.AuthState.Success -> {
                completeProfileViewModel.checkProfileCompletion().collect { isCompleted ->
                    currentScreen = if (!isCompleted) {
                        Screen.CompleteProfile
                    } else {
                        Screen.Dashboard
                    }
                }
            }

            authState is AuthViewModel.AuthState.LoggedOut -> {
                currentScreen = Screen.Login
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            authState is AuthViewModel.AuthState.Initial ||
                    authState is AuthViewModel.AuthState.InitialLoading -> {
                SplashScreen()
            }

            else -> {
                when (currentScreen) {
                    Screen.AdminPanel -> {
                        AdminPanelScreen(
                            onBackClick = { currentScreen = Screen.Dashboard }
                        )
                    }

                    Screen.Profile -> {
                        UserProfileScreen(
                            onBackClick = { currentScreen = Screen.Dashboard }
                        )
                    }

                    Screen.Settings -> {
                        SettingsScreen(
                            onBackClick = { currentScreen = Screen.Dashboard },
                            onLogout = { currentScreen = Screen.Login }
                        )
                    }

                    Screen.Login -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onRegistrationClick = { currentScreen = Screen.Register },
                            onForgotPasswordClick = { currentScreen = Screen.ForgotPassword }
                        )
                    }

                    Screen.Register -> {
                        RegisterScreen(
                            onRegisterClick = { nickname, email, password, confirmPassword ->
                                authViewModel.register(nickname, email, password, confirmPassword)
                            },
                            onLoginClick = { currentScreen = Screen.Login },
                            onRegulationsClick = { },
                            onPrivacyPolicyClick = { }
                        )
                    }

                    Screen.ForgotPassword -> {
                        ForgotPassword(
                            onBackToLogin = { currentScreen = Screen.Login }
                        )
                    }

                    Screen.CompleteProfile -> {
                        CompleteProfileScreen(
                            onSkip = { currentScreen = Screen.Dashboard },
                            isLoading = profileState is CompleteProfileViewModel.CompleteProfileState.Loading
                        )
                    }

                    Screen.Dashboard -> {
                        DashboardScreen(
                            onLogoutClick = { authViewModel.logout() },
                            onBodyMeasurementsClick = { currentScreen = Screen.BodyMeasurements },
                            onAdminPanelClick = { currentScreen = Screen.AdminPanel },
                            onProgressClick = { currentScreen = Screen.Weight },
                            onSettingsClick = { currentScreen = Screen.Settings },
                            onProfileClick = { currentScreen = Screen.Profile }
                        )
                    }

                    Screen.Weight -> {
                        WeightScreen(
                            onBackClick = { currentScreen = Screen.Dashboard }
                        )
                    }

                    Screen.BodyMeasurements -> {
                        BodyMeasurementsScreen(
                            onBackClick = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }

        LoadingOverlay(isVisible = profileState is CompleteProfileViewModel.CompleteProfileState.Loading)
    }
}

@Composable
private fun HandleBackButton(
    currentScreen: Screen,
    userSession: User?,
    authState: AuthViewModel.AuthState,
    onScreenChange: (Screen) -> Unit
) {
    BackHandler {
        when (currentScreen) {
            Screen.Dashboard -> {}

            Screen.CompleteProfile, Screen.Weight, Screen.BodyMeasurements, Screen.AdminPanel, Screen.Profile, Screen.Settings -> {
                onScreenChange(Screen.Dashboard)
            }

            else -> {
                if (userSession != null || authState is AuthViewModel.AuthState.Success) {
                    onScreenChange(Screen.Dashboard)
                } else if (currentScreen !is Screen.Login) {
                    onScreenChange(Screen.Login)
                }
            }
        }
    }
}

@Composable
private fun LoadingOverlay(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}