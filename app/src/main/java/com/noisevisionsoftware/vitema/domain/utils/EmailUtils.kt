package com.noisevisionsoftware.vitema.domain.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

object EmailUtils {
    fun openEmailApp(
        context: Context,
        emailAddress: String,
        subject: String = "Pytanie odnośnie planu dietetycznego",
        body: String = ""
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Nie znaleziono aplikacji do obsługi e-mail",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}