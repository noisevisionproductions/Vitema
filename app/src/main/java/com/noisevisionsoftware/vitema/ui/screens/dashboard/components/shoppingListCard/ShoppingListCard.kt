package com.noisevisionsoftware.vitema.ui.screens.dashboard.components.shoppingListCard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.model.shopping.ProductCategory
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.screens.shoppingList.components.toIcon

@Composable
fun ShoppingListCard(
    viewModel: DashboardShoppingListViewModel = hiltViewModel(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weeklyShoppingList by viewModel.weeklyShoppingList.collectAsState()
    val remainingItems by viewModel.remainingItems.collectAsState()
    val checkedProducts by viewModel.checkedProducts.collectAsState()
    val categoryProgress by viewModel.categoryProgress.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista zakupów",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (weeklyShoppingList) {
                is ViewModelState.Loading -> LoadingContent()
                is ViewModelState.Success -> {
                    val shoppingList = (weeklyShoppingList as ViewModelState.Success).data
                    if (shoppingList != null) {
                        val products = shoppingList.allProducts
                        if (products.isEmpty()) {
                            EmptyShoppingList()
                        } else {
                            ShoppingListSummary(
                                categoryProgress = categoryProgress,
                                remainingItems = when (remainingItems) {
                                    is ViewModelState.Success -> (remainingItems as ViewModelState.Success).data
                                    else -> products.size - checkedProducts.size
                                }
                            )
                        }
                    } else {
                        EmptyShoppingList()
                    }
                }

                is ViewModelState.Error -> {
                    Text(
                        text = (weeklyShoppingList as ViewModelState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> Unit
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kliknij, aby dowiedzieć się więcej",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ShoppingListSummary(
    categoryProgress: Map<ProductCategory, Pair<Int, Int>>,
    remainingItems: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val totalChecked = categoryProgress.values.sumOf { it.first }
        val totalItems = categoryProgress.values.sumOf { it.second }
        val totalProgress = if (totalItems > 0) totalChecked.toFloat() / totalItems else 0f

        LinearProgressIndicator(
            progress = { totalProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Text(
            text = when {
                remainingItems > 0 -> "Pozostało $remainingItems produktów do kupienia"
                else -> "Wszystkie produkty zostały kupione!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )

        val topCategories = categoryProgress.entries
            .sortedByDescending { it.value.second }
            .take(3)

        if (topCategories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topCategories) { (category, progress) ->
                    CategoryProgressBadge(
                        category = category,
                        checked = progress.first,
                        total = progress.second
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressBadge(
    category: ProductCategory,
    checked: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon.toIcon(),
                contentDescription = null,
                tint = Color(android.graphics.Color.parseColor(category.color)),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$checked/$total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun EmptyShoppingList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Brak listy zakupów",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "Lista stworzy się automatycznie po przydzieleniu diety",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}