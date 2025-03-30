package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.noisevisionsoftware.szytadieta.domain.utils.EmailUtils
import com.noisevisionsoftware.szytadieta.ui.common.EmptyStateMessage
import com.noisevisionsoftware.szytadieta.ui.common.NavigationActionButton

@Composable
fun NoShoppingListMessage(
    hasAnyShoppingLists: Boolean,
    onNavigateToAvailablePeriod: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val dieticianEmail = "szytadieta@gmail.com"

    EmptyStateMessage(
        icon = if (hasAnyShoppingLists) Icons.Default.DateRange else Icons.Default.ShoppingCart,
        title = if (hasAnyShoppingLists)
            "Brak listy zakupów na wybrany tydzień"
        else
            "Brak listy zakupów",
        message = if (hasAnyShoppingLists)
            "W tym tygodniu nie masz jeszcze listy zakupów. Sprawdź inny termin lub skontaktuj się z dietetykiem."
        else
            "Lista zakupów pojawi się automatycznie po przydzieleniu planu posiłków. Skontaktuj się z naszym dietetykiem, aby uzyskać więcej informacji.",
        actionButton = {
            if (hasAnyShoppingLists && onNavigateToAvailablePeriod != null) {
                NavigationActionButton(
                    text = "Przejdź do dostępnej listy",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onNavigateToAvailablePeriod
                )
            } else if (!hasAnyShoppingLists) {
                NavigationActionButton(
                    text = "Skontaktuj się z dietetykiem",
                    icon = Icons.Default.Email,
                    onClick = {
                        EmailUtils.openEmailApp(
                            context = context,
                            emailAddress = dieticianEmail,
                            subject = "Pytanie odnośnie listy zakupów"
                        )
                    }
                )
            }
        }
    )
}