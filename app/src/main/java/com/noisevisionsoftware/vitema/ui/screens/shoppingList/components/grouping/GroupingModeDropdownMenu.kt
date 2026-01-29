package com.noisevisionsoftware.vitema.ui.screens.shoppingList.components.grouping

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.shopping.GroupingMode

@Composable
fun GroupingModeDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentMode: GroupingMode,
    onModeChange: (GroupingMode) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(280.dp)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Wybierz grupowanie listy",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            enabled = false,
            onClick = { }
        )

        HorizontalDivider()

        GroupingMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        when (mode) {
                            GroupingMode.SINGLE_LIST -> "Lista całościowa"
                            GroupingMode.BY_RECIPE -> "Przepisy"
                            GroupingMode.BY_DAY -> "Dni"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = if (currentMode == mode) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null,
                onClick = {
                    onModeChange(mode)
                    onDismissRequest()
                }
            )
        }
    }
}