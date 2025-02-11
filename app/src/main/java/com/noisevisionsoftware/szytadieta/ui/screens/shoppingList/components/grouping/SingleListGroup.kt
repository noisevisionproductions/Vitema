package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.grouping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.shopping.ShoppingListGroup
import com.noisevisionsoftware.szytadieta.domain.model.shopping.ShoppingListProductContext

@Composable
fun SingleListCard(
    group: ShoppingListGroup.SingleList,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lista zakupów na ${group.totalDays} dni",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            val sortedProducts = remember(group.items, checkedProducts) {
                group.items.sortedWith(
                    compareBy<ShoppingListProductContext> {
                        it.occurrences?.all { id -> checkedProducts.contains(id) } ?: false
                    }.thenBy {
                        it.name.lowercase()
                    }
                )
            }

            ProductListWithQuantity(
                products = sortedProducts,
                checkedProducts = checkedProducts,
                onProductCheckedChange = onProductCheckedChange
            )
        }
    }
}


@Composable
private fun ProductListWithQuantity(
    products: List<ShoppingListProductContext>,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        products.forEach { product ->
            ProductItemWithQuantity(
                product = product,
                isChecked = product.occurrences?.all { checkedProducts.contains(it) } ?: false,
                onCheckedChange = { checked ->
                    product.occurrences?.forEach { productId ->
                        onProductCheckedChange(productId, checked)
                    }
                }
            )
        }
    }
}


@Composable
private fun ProductItemWithQuantity(
    product: ShoppingListProductContext,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isChecked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) }
            )

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "${product.quantity} szt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}