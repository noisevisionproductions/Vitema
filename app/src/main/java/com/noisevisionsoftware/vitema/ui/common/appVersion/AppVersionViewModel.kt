package com.noisevisionsoftware.vitema.ui.common.appVersion

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.data.localPreferences.PreferencesManager
import com.noisevisionsoftware.vitema.domain.model.app.AppVersion
import com.noisevisionsoftware.vitema.domain.repository.AppVersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppVersionViewModel @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _updateDialogState = MutableStateFlow<UpdateDialogState>(UpdateDialogState.Hidden)
    val updateDialogState = _updateDialogState.asStateFlow()

    fun checkAppVersion() {
        viewModelScope.launch {
            try {
                val currentVersion = getCurrentAppVersion()
                val isVersionCheckEnabled = preferencesManager.isVersionCheckEnabled.first()

                appVersionRepository.getAppVersion().getOrNull()?.let { appVersion ->
                    if (appVersion.isForceUpdate && currentVersion < appVersion.minimumRequiredVersion) {
                        showUpdateDialog(currentVersion, appVersion, true)
                    } else if (isVersionCheckEnabled && currentVersion < appVersion.minimumRequiredVersion) {
                        showUpdateDialog(currentVersion, appVersion, false)
                    }
                }
            } catch (e: Exception) {
                Log.e("Check App Version", "Error while checking app version:", e)
            }
        }
    }

    private fun showUpdateDialog(
        currentVersion: Int,
        appVersion: AppVersion,
        isForceUpdate: Boolean
    ) {
        _updateDialogState.value = UpdateDialogState.Visible(
            currentVersion = currentVersion,
            requiredVersion = appVersion.minimumRequiredVersion,
            updateMessage = appVersion.updateMessage,
            isForceUpdate = isForceUpdate
        )
    }

    private fun getCurrentAppVersion(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            }
        } catch (e: Exception) {
            0
        }
    }

    fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://play.google.com/store/apps/details?id=${context.packageName}"
                )
                setPackage("com.android.vending")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("Open Play Store", "Error while opening app store:", e)
            throw e
        }
    }

    fun dismissUpdateDialog() {
        _updateDialogState.value = UpdateDialogState.Hidden
    }

    sealed class UpdateDialogState {
        data object Hidden : UpdateDialogState()
        data class Visible(
            val currentVersion: Int,
            val requiredVersion: Int,
            val updateMessage: String,
            val isForceUpdate: Boolean
        ) : UpdateDialogState()
    }
}