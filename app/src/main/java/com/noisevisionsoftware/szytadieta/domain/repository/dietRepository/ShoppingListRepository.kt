package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.DatePeriod
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.ShoppingList
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private var SHOPPING_LIST_COLLECTION = "shopping_lists"
    }

    suspend fun getShoppingListForDate(
        userId: String,
        date: String
    ): Result<ShoppingList> = runCatching {
        val snapshot = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereLessThanOrEqualTo("startDate", date)
            .whereGreaterThanOrEqualTo("endDate", date)
            .get()
            .await()

        snapshot.documents.firstOrNull()
            ?.toObject(ShoppingList::class.java)
            ?.copy(id = snapshot.documents.first().id)
            ?: throw Exception("Nie znaleziono listy zakupów dla wybranej daty")
    }

    suspend fun getAllShoppingLists(userId: String): Result<List<ShoppingList>> = runCatching {
        val snapshot = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(ShoppingList::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getAvailablePeriods(userId: String): Result<List<DatePeriod>> = runCatching {
        val snapshot = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(ShoppingList::class.java)?.let { list ->
                DatePeriod(
                    startDate = list.startDate,
                    endDate = list.endDate
                )
            }
        }.distinct()
    }

    fun observeShoppingLists(userId: String): Flow<List<ShoppingList>> = callbackFlow {
        val subscription = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lists = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ShoppingList::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(lists)
            }

        awaitClose { subscription.remove() }
    }

    fun addSnapshotListener(listener: (QuerySnapshot?) -> Unit): ListenerRegistration {
        return firestore.collection("shopping_lists")
            .addSnapshotListener { snapshot, _ ->
                listener(snapshot)
            }
    }
}