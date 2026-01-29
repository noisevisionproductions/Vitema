package com.noisevisionsoftware.vitema.domain.model.guide

data class DietTip(
    val id: Int,
    val title: String,
    val content: String,
    val imageId: Int? = null
)

enum class ViewMode {
    CARDS, LIST
}