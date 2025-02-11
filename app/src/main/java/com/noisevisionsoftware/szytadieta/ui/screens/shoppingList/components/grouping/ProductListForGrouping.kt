package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components.grouping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.shopping.ShoppingListProductContext

@Composable
fun ProductListForGrouping(
    products: List<ShoppingListProductContext>,
    checkedProducts: Set<String>,
    onProductCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val validProducts = remember(products) {
        products.filter { it.name.isNotBlank() }
    }

    val uncheckedProducts = remember(validProducts, checkedProducts) {
        validProducts.filter { !checkedProducts.contains(it.productId) }
    }

    val checkedProductsList = remember(validProducts, checkedProducts) {
        validProducts.filter { checkedProducts.contains(it.productId) }
    }

    var isCheckedProductsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uncheckedProducts.forEach { product ->
            ProductItem(
                product = product,
                isChecked = checkedProducts.contains(product.productId),
                onCheckedChange = { checked ->
                    onProductCheckedChange(product.productId, checked)
                }
            )
        }

        if (checkedProductsList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCheckedProductsExpanded = !isCheckedProductsExpanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zaznaczone produkty (${checkedProductsList.size})",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Icon(
                        imageVector = if (isCheckedProductsExpanded)
                            Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isCheckedProductsExpanded)
                            "Zwiń zaznaczone" else "Rozwiń zaznaczone",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = isCheckedProductsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    checkedProductsList.forEach { product ->
                        ProductItem(
                            product = product,
                            isChecked = checkedProducts.contains(product.productId),
                            onCheckedChange = { checked ->
                                onProductCheckedChange(product.productId, checked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductItem(
    product: ShoppingListProductContext,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onCheckedChange(!isChecked) }
            .then(
                if (isChecked) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isChecked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.scale(0.9f)
            )

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                ),
                color = if (isChecked) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}