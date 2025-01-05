package com.noisevisionsoftware.szytadieta.ui.navigation

sealed class DashboardScreen {
    data object Login : DashboardScreen()
    data object Register : DashboardScreen()
    data object ForgotPassword : DashboardScreen()
    data object CompleteProfile : DashboardScreen()
    data object Dashboard : DashboardScreen()
    data object Weight : DashboardScreen()
    data object BodyMeasurements : DashboardScreen()
    data object Profile : DashboardScreen()
    data object MealPlan : DashboardScreen()
    data object ShoppingList : DashboardScreen()
    data object Settings : DashboardScreen()
    data object AdminPanel : DashboardScreen()
}