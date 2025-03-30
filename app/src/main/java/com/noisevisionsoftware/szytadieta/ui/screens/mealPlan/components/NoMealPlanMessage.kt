package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.noisevisionsoftware.szytadieta.domain.utils.EmailUtils
import com.noisevisionsoftware.szytadieta.ui.common.EmptyStateMessage
import com.noisevisionsoftware.szytadieta.ui.common.NavigationActionButton

@Composable
fun NoMealPlanMessage(
    hasAnyMealPlans: Boolean,
    onNavigateToAvailableWeek: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val dieticianEmail = "szytadieta@gmail.com"

    EmptyStateMessage(
        icon = if (hasAnyMealPlans) Icons.Default.DateRange else Icons.Default.RestaurantMenu,
        title = if (hasAnyMealPlans)
            "Brak planu na wybrany dzień"
        else
            "Brak spersonalizowanego planu posiłków",
        message = if (hasAnyMealPlans)
            "W tym dniu nie masz jeszcze zaplanowanych posiłków. Sprawdź inny termin lub skontaktuj się z dietetykiem."
        else
            "Odkryj świat zdrowego odżywiania z planem posiłków dopasowanym do Twoich celów! Skontaktuj się z naszym dietetykiem, aby otrzymać spersonalizowany plan.",
        actionButton = {
            if (hasAnyMealPlans && onNavigateToAvailableWeek != null) {
                NavigationActionButton(
                    text = "Przejdź do dostępnego planu",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onNavigateToAvailableWeek
                )
            } else if (!hasAnyMealPlans) {
                NavigationActionButton(
                    text = "Skontaktuj się z dietetykiem",
                    icon = Icons.Default.Email,
                    onClick = {
                        EmailUtils.openEmailApp(
                            context = context,
                            emailAddress = dieticianEmail
                        )
                    }
                )
            }
        }
    )
}