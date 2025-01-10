package com.noisevisionsoftware.szytadieta.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.noisevisionsoftware.szytadieta.ui.navigation.BottomNavItem
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.admin.AdminPanelScreen
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.BodyMeasurementsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.DashboardScreen
import com.noisevisionsoftware.szytadieta.ui.screens.documents.PrivacyPolicyScreen
import com.noisevisionsoftware.szytadieta.ui.screens.documents.RegulationsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.AuthViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.ForgotPassword
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.LoginScreen
import com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister.RegisterScreen
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.MealPlanScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.UserProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile.CompleteProfileScreen
import com.noisevisionsoftware.szytadieta.ui.screens.profile.profileEdit.ProfileEditScreen
import com.noisevisionsoftware.szytadieta.ui.screens.settings.SettingsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.ShoppingListScreen
import com.noisevisionsoftware.szytadieta.ui.screens.splash.SplashScreen
import com.noisevisionsoftware.szytadieta.ui.screens.subscription.SubscriptionPlanScreen
import com.noisevisionsoftware.szytadieta.ui.screens.weight.WeightScreen
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val userSession by authViewModel.userSession.collectAsState(initial = null)
    val profileCompleted by authViewModel.profileCompleted.collectAsState()
    var isInitialLoading by remember { mutableStateOf(true) }
    val currentScreen by mainViewModel.currentScreen.collectAsState()

    LaunchedEffect(Unit) {
        delay(1500)
        isInitialLoading = false
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                mainViewModel.refreshAllScreens()
            }

            is AuthState.Logout -> {
                mainViewModel.clearAllScreens()
                mainViewModel.updateScreen(NavigationDestination.UnauthenticatedDestination.Login)
            }

            else -> {}
        }
    }

    LaunchedEffect(authState, profileCompleted, isInitialLoading) {
        when {
            authState is AuthState.Success && profileCompleted == false -> {
                mainViewModel.updateScreen(NavigationDestination.AuthenticatedDestination.CompleteProfile)
            }

            authState is AuthState.Success && profileCompleted == true -> {
                mainViewModel.updateScreen(NavigationDestination.AuthenticatedDestination.Dashboard)
            }

            authState is AuthState.Error || authState is AuthState.Logout -> {
                mainViewModel.updateScreen(NavigationDestination.UnauthenticatedDestination.Login)
            }
        }
    }

    when {
        isInitialLoading -> SplashScreen()

        currentScreen is NavigationDestination.UnauthenticatedDestination -> {
            UnauthenticatedContent(
                currentScreen = currentScreen as NavigationDestination.UnauthenticatedDestination,
                onNavigate = mainViewModel::updateScreen
            )
        }

        currentScreen is NavigationDestination.AuthenticatedDestination -> {
            AuthenticatedContent(
                currentScreen = currentScreen as NavigationDestination.AuthenticatedDestination,
                authViewModel = authViewModel,
                onNavigate = mainViewModel::updateScreen
            )
        }
    }

    HandleBackButton(
        currentScreen = currentScreen,
        userSession = userSession,
        authState = authState,
        onNavigate = mainViewModel::updateScreen
    )
}

@Composable
private fun AuthenticatedContent(
    currentScreen: NavigationDestination.AuthenticatedDestination,
    authViewModel: AuthViewModel,
    onNavigate: (NavigationDestination) -> Unit
) {
    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(500))
                }, label = "Fade In/Out Animation"
            ) { screen ->
                when (screen) {
                    NavigationDestination.AuthenticatedDestination.Dashboard ->
                        DashboardScreen(
                            onNavigate = onNavigate
                        )

                    NavigationDestination.AuthenticatedDestination.CompleteProfile ->
                        CompleteProfileScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.Profile ->
                        UserProfileScreen(
                            onNavigate = onNavigate,
                            onLogoutClick = { authViewModel.logout() }
                        )

                    NavigationDestination.AuthenticatedDestination.AdminPanel -> {
                        LaunchedEffect(Unit) {
                            val hasAccess = authViewModel.checkAdminAccess()
                            if (!hasAccess) {
                                onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard)
                            }
                        }
                        AdminPanelScreen(onNavigate = onNavigate)
                    }

                    NavigationDestination.AuthenticatedDestination.BodyMeasurements ->
                        BodyMeasurementsScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.MealPlan ->
                        MealPlanScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.Settings ->
                        SettingsScreen(
                            onNavigate = onNavigate,
                            onLogout = { authViewModel.logout() }
                        )

                    NavigationDestination.AuthenticatedDestination.ShoppingList ->
                        ShoppingListScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.Weight ->
                        WeightScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.Subscription ->
                        SubscriptionPlanScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.EditProfile ->
                        ProfileEditScreen(onNavigate = onNavigate)

                    NavigationDestination.AuthenticatedDestination.PrivacyPolicy ->
                        PrivacyPolicyScreen(
                            onNavigate = onNavigate,
                            isAuthenticated = true
                        )

                    NavigationDestination.AuthenticatedDestination.Regulations ->
                        RegulationsScreen(
                            onNavigate = onNavigate,
                            isAuthenticated = true
                        )
                }
            }
        }
    }
}

@Composable
private fun UnauthenticatedContent(
    currentScreen: NavigationDestination.UnauthenticatedDestination,
    onNavigate: (NavigationDestination) -> Unit
) {
    when (currentScreen) {
        NavigationDestination.UnauthenticatedDestination.Login ->
            LoginScreen(onNavigate = onNavigate)

        NavigationDestination.UnauthenticatedDestination.Register ->
            RegisterScreen(onNavigate = onNavigate)

        NavigationDestination.UnauthenticatedDestination.ForgotPassword ->
            ForgotPassword(onNavigate = onNavigate)

        NavigationDestination.UnauthenticatedDestination.PrivacyPolicy ->
            PrivacyPolicyScreen(
                onNavigate = onNavigate,
                isAuthenticated = false
            )

        NavigationDestination.UnauthenticatedDestination.Regulations ->
            RegulationsScreen(
                onNavigate = onNavigate,
                isAuthenticated = false
            )
    }
}

@Composable
private fun AppBottomNavigation(
    currentScreen: NavigationDestination.AuthenticatedDestination,
    onNavigate: (NavigationDestination) -> Unit
) {
    NavigationBar {
        BottomNavItem.items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentScreen == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun HandleBackButton(
    currentScreen: NavigationDestination,
    userSession: User?,
    authState: AuthState<User>,
    onNavigate: (NavigationDestination) -> Unit = {}
) {
    BackHandler {
        when (currentScreen) {
            NavigationDestination.AuthenticatedDestination.Dashboard -> {}

            NavigationDestination.AuthenticatedDestination.Settings -> {
                onNavigate(NavigationDestination.AuthenticatedDestination.Profile)
            }

            NavigationDestination.AuthenticatedDestination.EditProfile -> {
                onNavigate(NavigationDestination.AuthenticatedDestination.Profile)
            }

            NavigationDestination.AuthenticatedDestination.PrivacyPolicy -> {
                if (userSession != null || authState is AuthState.Success) {
                    onNavigate(NavigationDestination.AuthenticatedDestination.Settings)
                }
            }

            NavigationDestination.UnauthenticatedDestination.PrivacyPolicy -> {
                onNavigate(NavigationDestination.UnauthenticatedDestination.Register)
            }

            NavigationDestination.AuthenticatedDestination.Regulations -> {
                if (userSession != null || authState is AuthState.Success) {
                    onNavigate(NavigationDestination.AuthenticatedDestination.Settings)
                }
            }

            NavigationDestination.UnauthenticatedDestination.Regulations -> {
                onNavigate(NavigationDestination.UnauthenticatedDestination.Register)
            }

            NavigationDestination.AuthenticatedDestination.CompleteProfile,
            NavigationDestination.AuthenticatedDestination.Weight,
            NavigationDestination.AuthenticatedDestination.BodyMeasurements,
            NavigationDestination.AuthenticatedDestination.Profile,
            NavigationDestination.AuthenticatedDestination.MealPlan,
            NavigationDestination.AuthenticatedDestination.ShoppingList -> {
                onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard)
            }

            else -> {
                if (userSession != null || authState is AuthState.Success) {
                    onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard)
                } else if (currentScreen !is NavigationDestination.UnauthenticatedDestination.Login) {
                    onNavigate(NavigationDestination.UnauthenticatedDestination.Login)
                }
            }
        }
    }
}