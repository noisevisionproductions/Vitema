package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.noisevisionsoftware.szytadieta.domain.model.shopping.CategorizedShoppingList
import com.noisevisionsoftware.szytadieta.domain.model.shopping.DatePeriod
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    ): Result<CategorizedShoppingList> = runCatching {
        val snapshot = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val targetDate = dateFormat.parse(date)?.time
            ?: throw IllegalArgumentException("Nieprawidłowy format daty")

        val matchingList = snapshot.documents.firstNotNullOfOrNull { doc ->
            val list = doc.toObject(CategorizedShoppingList::class.java)?.copy(id = doc.id)
            if (list != null) {
                val startDate = dateFormat.format(Date(list.startTimestamp.seconds * 1000))
                val endDate = dateFormat.format(Date(list.endTimestamp.seconds * 1000))
                val targetDateStr = dateFormat.format(Date(targetDate))

                val isInRange = targetDateStr in startDate..endDate

                list.takeIf { isInRange }
            } else null
        } ?: throw Exception("Nie znaleziono listy zakupów dla wybranej daty")

        Log.d("ShoppingListRepo", "Found matching list: $matchingList")
        matchingList
    }

    suspend fun getAvailablePeriods(userId: String): Result<List<DatePeriod>> = runCatching {
        val snapshot = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(CategorizedShoppingList::class.java)?.let { list ->
                DatePeriod(
                    startTimestamp = list.startTimestamp,
                    endTimestamp = list.endTimestamp
                )
            }
        }.distinct()
    }

    fun observeShoppingLists(userId: String): Flow<List<CategorizedShoppingList>> = callbackFlow {
        val subscription = firestore.collection(SHOPPING_LIST_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lists = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CategorizedShoppingList::class.java)?.copy(id = doc.id)
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