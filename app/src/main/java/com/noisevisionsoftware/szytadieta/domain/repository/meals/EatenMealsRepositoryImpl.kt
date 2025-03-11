package com.noisevisionsoftware.szytadieta.domain.repository.meals

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.LocalEatenMealsRepository
import com.noisevisionsoftware.szytadieta.data.remote.RemoteEatenMealsRepository
import com.noisevisionsoftware.szytadieta.di.ApplicationScope
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EatenMealsRepositoryImpl @Inject constructor(
    private val localRepository: LocalEatenMealsRepository,
    private val remoteRepository: RemoteEatenMealsRepository,
    private val networkManager: NetworkConnectivityManager,
    private val workManager: WorkManager,
    @ApplicationScope private val scope: CoroutineScope
) : EatenMealsRepository {

    private data class PendingOperation(
        val type: OperationType,
        val userId: String,
        val date: String,
        val mealId: String
    )

    private enum class OperationType {
        SAVE, REMOVE
    }

    private val pendingOperations = mutableListOf<PendingOperation>()
    private val mutex = Mutex()

    init {
        scope.launch {
            networkManager.isNetworkConnected.collect { isConnected ->
                if (isConnected) {
                    processPendingOperations()
                }
            }
        }

        schedulePeriodicSync()
    }

    override suspend fun saveEatenMeal(userId: String, date: String, mealId: String) {
        localRepository.saveEatenMeal(userId, date, mealId)

        val operation = PendingOperation(
            type = OperationType.SAVE,
            userId = userId,
            date = date,
            mealId = mealId
        )

        if (networkManager.isCurrentlyConnected()) {
            executeRemoteOperation(operation)
        } else {
            queueOperation(operation)
        }
    }

    override suspend fun removeEatenMeal(userId: String, date: String, mealId: String) {
        localRepository.removeEatenMeal(userId, date, mealId)

        val operation = PendingOperation(
            type = OperationType.REMOVE,
            userId = userId,
            date = date,
            mealId = mealId
        )

        if (networkManager.isCurrentlyConnected()) {
            executeRemoteOperation(operation)
        } else {
            queueOperation(operation)
        }
    }

    override suspend fun getEatenMeals(userId: String, date: String): Set<String> {
        if (networkManager.isCurrentlyConnected()) {
            syncWithRemote(userId, date)
        }
        return localRepository.observeEatenMeals(userId, date).first()
    }

    override fun observeEatenMeals(userId: String, date: String): Flow<Set<String>> {
        scope.launch {
            if (networkManager.isCurrentlyConnected()) {
                syncWithRemote(userId,date)
            }
        }

        return localRepository.observeEatenMeals(userId, date)
    }

    override suspend fun syncWithRemote(userId: String, date: String) {
        if (!networkManager.isCurrentlyConnected()) return

        withRetry {
            val remoteMeals = remoteRepository.getEatenMeals(userId, date)
            val localMeals = localRepository.observeEatenMeals(userId, date ).first()

            val mealsToAdd = remoteMeals - localMeals
            val mealsToRemove = localMeals - remoteMeals

            mealsToAdd.forEach { mealId ->
                localRepository.saveEatenMeal(userId, date, mealId)
            }

            mealsToRemove.forEach { mealId ->
                localRepository.removeEatenMeal(userId, date, mealId)
            }
        }
    }

    private suspend fun executeRemoteOperation(operation: PendingOperation) {
        withRetry {
            when (operation.type) {
                OperationType.SAVE -> remoteRepository.saveEatenMeal(
                    operation.userId,
                    operation.date,
                    operation.mealId
                )

                OperationType.REMOVE -> remoteRepository.removeEatenMeal(
                    operation.userId,
                    operation.date,
                    operation.mealId
                )
            }
        }
    }

    private suspend fun queueOperation(operation: PendingOperation) {
        mutex.withLock {
            pendingOperations.add(operation)
        }
    }

    private suspend fun processPendingOperations() {
        mutex.withLock {
            val operations = pendingOperations.toList()
            pendingOperations.clear()

            operations.forEach { operation ->
                try {
                    executeRemoteOperation(operation)
                } catch (e: Exception) {
                    pendingOperations.add(operation)
                    Log.e("EatenMealsRepo", "Failed to process pending operation", e)
                }
            }
        }
    }

    private fun schedulePeriodicSync() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "meal_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxAttempts - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                Log.e("EatenMealsRepo", "Attempt ${attempt + 1} failed", e)
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block()
    }
}