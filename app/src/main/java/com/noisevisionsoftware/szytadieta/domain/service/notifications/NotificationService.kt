package com.noisevisionsoftware.szytadieta.domain.service.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.noisevisionsoftware.szytadieta.data.FCMTokenRepository
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenRepository: FCMTokenRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "NotificationService"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        when (message.data["type"]) {
            "new_diet" -> {
                val dietId = message.data["diet_id"] ?: return
                val dietName = message.data["diet_name"] ?: "Nowa dieta"
                notificationHelper.showNewDietNotification(dietId, dietName)
            }

            else ->
                message.notification?.let { notification ->
                    notificationHelper.showBasicNotification(
                        notification.title ?: "Szyta Dieta",
                        notification.body ?: return
                    )
                }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            updateFCMToken()
        }
    }

    private suspend fun updateFCMToken() {
        try {
            authRepository.getCurrentUser()?.let { user ->
                fcmTokenRepository.updateToken(user.uid)
                    .onFailure { error ->
                        Log.e(TAG, "Błąd podczas aktualizacji tokenu FCM", error)
                    }
            } ?: Log.d(TAG, "Pomijanie aktualizacji tokenu FCM - użytkownik nie jest zalogowany")
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas obsługi nowego tokenu FCM", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}