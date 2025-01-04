package com.noisevisionsoftware.szytadieta.domain.model.file

enum class FileType {
    DIET_PLAN,
    SHOPPING_LIST,
    UNKNOWN;

    companion object {
        fun fromFileName(fileName: String): FileType {
            return when {
                fileName.lowercase().contains("diet") -> DIET_PLAN
                fileName.lowercase().contains("shopping") -> SHOPPING_LIST
                else -> UNKNOWN
            }
        }
    }
}