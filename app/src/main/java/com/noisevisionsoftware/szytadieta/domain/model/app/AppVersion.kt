package com.noisevisionsoftware.szytadieta.domain.model.app

data class AppVersion (
    val versionCode: Int =0,
    val isForceUpdate: Boolean = false,
    val minimumRequiredVersion: Int = 0,
    val updateMessage: String = ""
)