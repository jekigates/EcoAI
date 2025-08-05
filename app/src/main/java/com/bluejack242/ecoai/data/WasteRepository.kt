package com.bluejack242.ecoai.data

import android.content.Context
import android.net.Uri
import com.bluejack242.ecoai.model.WasteHistoryItem
import com.bluejack242.ecoai.model.WasteItem
import com.bluejack242.ecoai.utils.CloudinaryService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class WasteRepository() {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addWasteItemWithImage(
        context: Context,
        name: String,
        co2e: Int,
        imageUri: Uri,
        uploadedBy: String,
        disposalMethod: String
    ): WasteHistoryItem {
        val cloudinary = CloudinaryService()
        val result = cloudinary.uploadMedia(context, imageUri)

        return result.getOrThrow().let { imageUrl ->
            val item = WasteHistoryItem(
                id = UUID.randomUUID().toString(),
                name = name,
                co2e = co2e,
                imageRes = imageUrl,
                date = Timestamp.now(),
                uploadedBy = uploadedBy,
                disposalMethod = disposalMethod
            )

            firestore.collection("wasteHistoryItems")
                .document(item.id)
                .set(item)
                .await()

            item
        }
    }


    suspend fun getWasteHistoryItems(): List<WasteHistoryItem> {
        return firestore.collection("wasteHistoryItems").get()
            .await().documents.mapNotNull { document ->
            document.toObject(WasteHistoryItem::class.java)?.copy(id = document.id)
        }
    }

    suspend fun getWasteHistoryItemById(id: String): WasteHistoryItem? {
        val document = firestore.collection("wasteHistoryItems").document(id).get().await()
        return document.toObject(WasteHistoryItem::class.java)?.copy(id = document.id)
    }

    suspend fun deleteWasteHistoryItem(id: String) {
        firestore.collection("wasteHistoryItems").document(id).delete().await()
    }

    suspend fun getRecentlyUploadedWaste(
        userId: String,
        limit: Int = 5
    ): List<WasteHistoryItem> {
        return firestore.collection("wasteHistoryItems")
            .whereEqualTo("uploadedBy", userId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(WasteHistoryItem::class.java)
    }


    suspend fun calculateUserCarbonTrack(userId: String): Int {
        val items = firestore.collection("wasteHistoryItems")
            .whereEqualTo("uploadedBy", userId)
            .get()
            .await()
            .toObjects(WasteHistoryItem::class.java)

        return items.sumOf { it.co2e }
    }

    suspend fun calculateUserWeeklyStreak(userId: String): Int {
        val items = firestore.collection("wasteHistoryItems")
            .whereEqualTo("uploadedBy", userId)
            .get()
            .await()
            .toObjects(WasteHistoryItem::class.java)

        val now = Calendar.getInstance()

        val last7days = items.filter { item ->
            val itemDate = item.date.toDate()
            val diffInMillis = now.timeInMillis - itemDate.time
            diffInMillis <= (7 * 24 * 60 * 60 * 1000)
        }

        return last7days
            .mapNotNull { item ->
                val cal = java.util.Calendar.getInstance()
                item.date.toDate().let { cal.time = it }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }
            .distinct()
            .size
    }

    suspend fun addWasteItemFromDatabase(
        name: String,
        co2e: Int,
        imageUrl: String,
        uploadedBy: String,
        disposalMethod: String
    ): WasteHistoryItem {
        val item = WasteHistoryItem(
            id = UUID.randomUUID().toString(),
            name = name,
            co2e = co2e,
            imageRes = imageUrl,
            date = Timestamp.now(),
            uploadedBy = uploadedBy,
            disposalMethod = disposalMethod
        )

        firestore.collection("wasteHistoryItems")
            .document(item.id)
            .set(item)
            .await()

        return item
    }

    suspend fun getWasteDatabaseItems(): List<WasteItem> {
        return firestore.collection("wasteDatabaseItems")
            .get()
            .await()
            .documents.mapNotNull { document ->
                document.toObject(WasteItem::class.java)?.copy(id = document.id)
            }
    }

    suspend fun searchWasteDatabaseItems(query: String): List<WasteItem> {
        return firestore.collection("wasteDatabaseItems")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .await()
            .documents.mapNotNull { document ->
                document.toObject(WasteItem::class.java)?.copy(id = document.id)
            }
    }
}