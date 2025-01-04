package com.noisevisionsoftware.szytadieta.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.state.AuthState
import com.noisevisionsoftware.szytadieta.ui.navigation.Screen
import com.noisevisionsoftware.szytadieta.ui.screens.admin.AdminPanelScreen
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.BodyMeasurementsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.DashboardScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.AuthViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.ForgotPassword
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.LoginScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.RegisterScreen
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.MealPlanScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.UserProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile.CompleteProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile.CompleteProfileViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.settings.SettingsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.splash.SplashScreen
import com.noisevisionsoftware.szytadieta.ui.screens.weight.WeightScreen
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    completeProfileViewModel: CompleteProfileViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val authState by authViewModel.authState.collectAsState()
    val userSession by authViewModel.userSession.collectAsState(initial = null)
    val profileState by completeProfileViewModel.profileState.collectAsState()
    var isInitialLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        isInitialLoading = false
    }

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
            userSession != null || authState is AuthState.Success -> {
                completeProfileViewModel.checkProfileCompletion().collect { isCompleted ->
                    currentScreen = if (!isCompleted) {
                        Screen.CompleteProfile
                    } else {
                        Screen.Dashboard
                    }
                }
            }

            authState is AuthState.Logout -> {
                currentScreen = Screen.Login
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isInitialLoading && authState is AuthState.Loading -> {
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
                            onProfileClick = { currentScreen = Screen.Profile },
                            onMealPlanClick = { currentScreen = Screen.MealPlan }
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

                    Screen.MealPlan -> {
                        MealPlanScreen(
                            onBackClick = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HandleBackButton(
    currentScreen: Screen,
    userSession: User?,
    authState: AuthState<User>,
    onScreenChange: (Screen) -> Unit
) {
    BackHandler {
        when (currentScreen) {
            Screen.Dashboard -> {}

            Screen.CompleteProfile,
            Screen.Weight,
            Screen.BodyMeasurements,
            Screen.Profile,
            Screen.MealPlan,
            Screen.Settings -> {
                onScreenChange(Screen.Dashboard)
            }

            else -> {
                if (userSession != null || authState is AuthState.Success) {
                    onScreenChange(Screen.Dashboard)
                } else if (currentScreen !is Screen.Login) {
                    onScreenChange(Screen.Login)
                }
            }
        }
    }
}