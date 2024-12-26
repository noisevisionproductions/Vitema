package com.noisevisionsoftware.fitapplication.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val nickname: String = "",
    val createdAt: Long = 0
)