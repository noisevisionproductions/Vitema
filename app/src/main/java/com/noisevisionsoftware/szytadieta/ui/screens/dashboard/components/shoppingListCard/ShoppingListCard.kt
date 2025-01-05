package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState

@Composable
fun ShoppingListCard(
    viewModel: DashboardShoppingListViewModel = hiltViewModel(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shoppingList by viewModel.weeklyShoppingList.collectAsState()
    val remainingItems by viewModel.remainingItems.collectAsState()

    LaunchedEffect(shoppingList) {
        if (shoppingList is ViewModelState.Success) {
            viewModel.calculateRemainingItems()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista zakupów",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = "Tydzień: ${viewModel.getFormattedWeekDates()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            when (shoppingList) {
                is ViewModelState.Success -> {
                    val list = (shoppingList as ViewModelState.Success<ShoppingList?>).data
                    if (list != null) {
                        ShoppingListPreview(list, remainingItems)
                    } else {
                        EmptyShoppingList()
                    }
                }

                is ViewModelState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                else -> Unit
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zobacz pełną listę",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ShoppingListPreview(
    shoppingList: ShoppingList,
    remainingItems: ViewModelState<Int>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        shoppingList.categories.take(2).forEach { category ->
            Text(
                text = "• ${category.name} (${category.products.size})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        if (shoppingList.categories.size > 2) {
            Text(
                text = "i ${shoppingList.categories.size - 2} więcej kategorii...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }

        if (remainingItems is ViewModelState.Success) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pozostało do kupienia: ${remainingItems.data} produktów",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun EmptyShoppingList() {
    Text(
        text = "Brak listy zakupów na ten tydzień",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
    )
}