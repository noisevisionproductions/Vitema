package com.noisevisionsoftware.szytadieta.domain.model.guide

data class DietTip(
    val id: Int,
    val title: String,
    val content: String,
    val imageId: Int? = null
)

enum class ViewMode {
    CARDS, LIST
}