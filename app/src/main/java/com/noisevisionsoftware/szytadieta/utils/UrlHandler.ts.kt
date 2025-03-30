package com.noisevisionsoftware.szytadieta.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

object UrlHandler {
    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = url.toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}