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
import com.noisevisionsoftware.szytadieta.ui.navigation.DashboardScreen
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
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.ShoppingListScreen
import com.noisevisionsoftware.szytadieta.ui.screens.splash.SplashScreen
import com.noisevisionsoftware.szytadieta.ui.screens.weight.WeightScreen
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    completeProfileViewModel: CompleteProfileViewModel = hiltViewModel()
) {
    val currentDashboardScreen by mainViewModel.currentScreen.collectAsState()
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
            mainViewModel.updateScreen(DashboardScreen.Dashboard)
        }
    }

    HandleBackButton(
        currentDashboardScreen = currentDashboardScreen,
        userSession = userSession,
        authState = authState,
        onScreenChange = mainViewModel::updateScreen
    )

    LaunchedEffect(authState, userSession) {
        when {
            userSession != null || authState is AuthState.Success -> {
                completeProfileViewModel.checkProfileCompletion().collect { isCompleted ->
                    mainViewModel.updateScreen(
                        if (!isCompleted) DashboardScreen.CompleteProfile
                        else DashboardScreen.Dashboard
                    )
                }
            }

            authState is AuthState.Logout -> {
                mainViewModel.updateScreen(DashboardScreen.Login)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isInitialLoading && authState is AuthState.Loading -> {
                SplashScreen()
            }

            else -> {
                when (currentDashboardScreen) {
                    DashboardScreen.AdminPanel -> {
                        AdminPanelScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) }
                        )
                    }

                    DashboardScreen.Profile -> {
                        UserProfileScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) }
                        )
                    }

                    DashboardScreen.Settings -> {
                        SettingsScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) },
                            onLogout = { mainViewModel.updateScreen(DashboardScreen.Login) }
                        )
                    }

                    DashboardScreen.Login -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onRegistrationClick = {
                                mainViewModel.updateScreen(DashboardScreen.Register)
                            },
                            onForgotPasswordClick = {
                                mainViewModel.updateScreen(DashboardScreen.ForgotPassword)
                            }
                        )
                    }

                    DashboardScreen.Register -> {
                        RegisterScreen(
                            onRegisterClick = { nickname, email, password, confirmPassword ->
                                authViewModel.register(nickname, email, password, confirmPassword)
                            },
                            onLoginClick = {
                                mainViewModel.updateScreen(DashboardScreen.Login)
                            },
                            onRegulationsClick = { },
                            onPrivacyPolicyClick = { }
                        )
                    }

                    DashboardScreen.ForgotPassword -> {
                        ForgotPassword(
                            onBackToLogin = {
                                mainViewModel.updateScreen(DashboardScreen.Login)
                            }
                        )
                    }

                    DashboardScreen.CompleteProfile -> {
                        CompleteProfileScreen(
                            onSkip = {
                                mainViewModel.updateScreen(DashboardScreen.Dashboard)
                            },
                            isLoading = profileState is CompleteProfileViewModel.CompleteProfileState.Loading
                        )
                    }

                    DashboardScreen.Dashboard -> {
                        DashboardScreen(
                            onLogoutClick = { authViewModel.logout() },
                            onBodyMeasurementsClick = {
                                mainViewModel.updateScreen(DashboardScreen.BodyMeasurements)

                            },
                            onAdminPanelClick = {
                                mainViewModel.updateScreen(DashboardScreen.AdminPanel)
                            },
                            onProgressClick = {
                                mainViewModel.updateScreen(DashboardScreen.Weight)
                            },
                            onSettingsClick = { mainViewModel.updateScreen(DashboardScreen.Settings) },
                            onProfileClick = { mainViewModel.updateScreen(DashboardScreen.Profile) },
                            onMealPlanClick = { mainViewModel.updateScreen(DashboardScreen.MealPlan) },
                            onShoppingListClick = { mainViewModel.updateScreen(DashboardScreen.ShoppingList) }
                        )
                    }

                    DashboardScreen.Weight -> {
                        WeightScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) }
                        )
                    }

                    DashboardScreen.BodyMeasurements -> {
                        BodyMeasurementsScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) }
                        )
                    }

                    DashboardScreen.MealPlan -> {
                        MealPlanScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) }
                        )
                    }

                    DashboardScreen.ShoppingList -> {
                        ShoppingListScreen(
                            onBackClick = { mainViewModel.updateScreen(DashboardScreen.Dashboard) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HandleBackButton(
    currentDashboardScreen: DashboardScreen,
    userSession: User?,
    authState: AuthState<User>,
    onScreenChange: (DashboardScreen) -> Unit
) {
    BackHandler {
        when (currentDashboardScreen) {
            DashboardScreen.Dashboard -> {}

            DashboardScreen.CompleteProfile,
            DashboardScreen.Weight,
            DashboardScreen.BodyMeasurements,
            DashboardScreen.Profile,
            DashboardScreen.MealPlan,
            DashboardScreen.ShoppingList,
            DashboardScreen.Settings -> {
                onScreenChange(DashboardScreen.Dashboard)
            }

            else -> {
                if (userSession != null || authState is AuthState.Success) {
                    onScreenChange(DashboardScreen.Dashboard)
                } else if (currentDashboardScreen !is DashboardScreen.Login) {
                    onScreenChange(DashboardScreen.Login)
                }
            }
        }
    }
}