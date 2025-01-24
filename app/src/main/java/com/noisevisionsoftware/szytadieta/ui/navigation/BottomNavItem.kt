package com.noisevisionsoftware.szytadieta.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: NavigationDestination.AuthenticatedDestination,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        route = NavigationDestination.AuthenticatedDestination.Dashboard,
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    data object MealPlan : BottomNavItem(
        route = NavigationDestination.AuthenticatedDestination.MealPlan,
        title = "Plan posiłków",
        icon = Icons.AutoMirrored.Filled.MenuBook
    )

    data object Profile : BottomNavItem(
        route = NavigationDestination.AuthenticatedDestination.Profile,
        title = "Profil",
        icon = Icons.Default.Person
    )

    companion object {
        val items = listOf(MealPlan, Dashboard, Profile)
    }
}