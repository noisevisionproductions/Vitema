package com.noisevisionsoftware.vitema.ui.screens.documents

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Application
import com.noisevisionsoftware.vitema.R
import java.io.BufferedReader
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    fun getPrivacyPolicy(): String {
        return try {
            application.resources.openRawResource(R.raw.privacy_policy)
                .bufferedReader()
                .use(BufferedReader::readText)
        } catch (e: Exception) {
            "Nie udało się załadować polityki prywatności"
        }
    }

    fun getRegulations(): String {
        return try {
            application.resources.openRawResource(R.raw.regulations)
                .bufferedReader()
                .use(BufferedReader::readText)
        } catch (e: Exception) {
            "Nie udało się załadować regulaminu"
        }
    }
}