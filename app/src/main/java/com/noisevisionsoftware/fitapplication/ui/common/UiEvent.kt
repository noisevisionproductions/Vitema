package com.noisevisionsoftware.fitapplication.ui.common

sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class ShowSuccess(val message: String) : UiEvent()
}