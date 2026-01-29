package com.noisevisionsoftware.vitema.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
}

fun formatHour(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

fun parseDate(dateStr: String): Long {
    return try {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateStr)?.time
            ?: throw IllegalArgumentException("Nieprawidłowy format daty")
    } catch (e: ParseException) {
        throw IllegalArgumentException("Nieprawidłowy format daty", e)
    }
}

fun isDateInRange(date: String, startDate: String, endDate: String): Boolean {
    val dateTimestamp = parseDate(date)
    val startTimestamp = parseDate(startDate)
    val endTimestamp = parseDate(endDate)
    return dateTimestamp in startTimestamp..endTimestamp
}