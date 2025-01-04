package com.noisevisionsoftware.szytadieta.ui.screens.admin.navigation

sealed class AdminScreen {
    data object Dashboard: AdminScreen()
    data object UserManagement : AdminScreen()
    data object Statistics: AdminScreen()
    data object FileUpload: AdminScreen()
}