package edu.bluejack24_2.ecoai.data

import android.content.Context
import android.net.Uri
import edu.bluejack24_2.ecoai.model.WasteHistoryItem
import edu.bluejack24_2.ecoai.model.WasteItem
import edu.bluejack24_2.ecoai.utils.CloudinaryService
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

        // Get all upload days as midnight millis, sorted descending (latest first)
        val uploadDays = items.mapNotNull { item ->
            val cal = Calendar.getInstance()
            item.date.toDate().let { cal.time = it }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet().toList().sortedDescending()

        if (uploadDays.isEmpty()) return 0

        // Find the latest upload day
        val lastUploadDay = uploadDays.first()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayMillis = today.timeInMillis

        // If last upload is not today, check if a day is missed
        if (lastUploadDay < todayMillis) {
            // Check if last upload was yesterday
            val yesterday = Calendar.getInstance()
            yesterday.set(Calendar.HOUR_OF_DAY, 0)
            yesterday.set(Calendar.MINUTE, 0)
            yesterday.set(Calendar.SECOND, 0)
            yesterday.set(Calendar.MILLISECOND, 0)
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayMillis = yesterday.timeInMillis
            if (lastUploadDay == yesterdayMillis) {
                // Check for consecutive streak up to yesterday
                var streak = 1
                var currentDay = yesterdayMillis
                while (uploadDays.contains(currentDay - 24 * 60 * 60 * 1000)) {
                    streak++
                    currentDay -= 24 * 60 * 60 * 1000
                }
                return streak
            } else {
                // Missed a day, streak resets
                return 0
            }
        } else {
            // Last upload is today, count consecutive days up to today
            var streak = 1
            var currentDay = todayMillis
            while (uploadDays.contains(currentDay - 24 * 60 * 60 * 1000)) {
                streak++
                currentDay -= 24 * 60 * 60 * 1000
            }
            return streak
        }
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