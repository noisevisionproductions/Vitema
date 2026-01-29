package com.noisevisionsoftware.vitema.ui.screens.admin.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.noisevisionsoftware.vitema.ui.screens.admin.navigation.AdminScreen

data class AdminMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val screen: AdminScreen
)
