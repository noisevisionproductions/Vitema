package com.noisevisionsoftware.szytadieta.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.BuildConfig
import javax.inject.Inject

class AppVersionUtils @Inject constructor(
    private val context: Context
) {
    @Suppress("DEPRECATION")
    fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            val buildType = if (BuildConfig.DEBUG) "Debug" else "Release"
            "${packageInfo.versionName} (Build $versionCode) $buildType"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }
}