package com.noisevisionsoftware.szytadieta.domain.service.notifications.water

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.noisevisionsoftware.szytadieta.domain.model.health.water.WaterIntake
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.WaterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class WaterReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var waterRepository: WaterRepository

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ADD_WATER") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    authRepository.withAuthenticatedUser { userId ->
                        val waterIntake = WaterIntake(
                            userId = userId,
                            amount = 250
                        )
                        waterRepository.addWaterIntake(waterIntake)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Dodano szklankę wody",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Nie udało się dodać wody",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}