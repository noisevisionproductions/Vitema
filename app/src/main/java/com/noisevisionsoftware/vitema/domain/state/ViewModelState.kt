package com.noisevisionsoftware.vitema.domain.state

import com.noisevisionsoftware.vitema.domain.exceptions.PasswordField

sealed class ViewModelState<out T> {
    data object Initial : ViewModelState<Nothing>()
    data object Loading : ViewModelState<Nothing>()
    data class Success<T>(val data: T) : ViewModelState<T>()
    data class Error(
        val message: String,
        val field: PasswordField? = null
    ) : ViewModelState<Nothing>()}