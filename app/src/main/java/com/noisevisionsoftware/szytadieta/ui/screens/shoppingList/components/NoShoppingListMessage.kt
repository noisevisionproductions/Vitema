package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.noisevisionsoftware.szytadieta.ui.common.EmptyStateMessage
import com.noisevisionsoftware.szytadieta.ui.common.NavigationActionButton
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination

@Composable
fun NoShoppingListMessage(
    hasAnyShoppingLists: Boolean,
    onNavigateToAvailableWeek: (() -> Unit)? = null,
    onNavigate: (NavigationDestination) -> Unit
) {
    EmptyStateMessage(
        icon = if (hasAnyShoppingLists) Icons.Default.DateRange else Icons.Default.ShoppingCart,
        title = if (hasAnyShoppingLists)
            "Brak listy zakupów na wybrany tydzień"
        else
            "Brak listy zakupów",
        message = if (hasAnyShoppingLists)
            "W tym tygodniu nie masz jeszcze listy zakupów. Sprawdź inny termin lub poczekaj na przydzielenie diety."
        else
            "Lista zakupów pojawi się automatycznie po przydzieleniu planu posiłków przez dietetyka.",
        actionButton = {
            if (hasAnyShoppingLists && onNavigateToAvailableWeek != null) {
                NavigationActionButton(
                    text = "Przejdź do dostępnej listy",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onNavigateToAvailableWeek
                )
            } else if (!hasAnyShoppingLists) {
                NavigationActionButton(
                    text = "Sprawdź plany subskrypcji",
                    icon = Icons.Default.Star,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Subscription) }
                )
            }
        }
    )
}