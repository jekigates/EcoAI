package com.bluejack242.ecoai.data

import android.content.Context
import android.net.Uri
import com.bluejack242.ecoai.model.WasteHistoryItem
import com.bluejack242.ecoai.utils.CloudinaryService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WasteRepository() {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addWasteItemWithImage(
        context: Context,
        name: String,
        co2e: Int,
        imageUri: Uri,
        uploadedBy: String
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
                uploadedBy = uploadedBy
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


    suspend fun getUserCarbonTrack(): Int {
        val snapshot = firestore.collection("carbonTrack").document("userId").get().await()
        return snapshot.getLong("totalCO2")?.toInt() ?: 0
    }

    suspend fun getUserWeeklyStreak(): Int {
        val snapshot = firestore.collection("weeklyStreak").document("userId").get().await()
        return snapshot.getLong("streak")?.toInt() ?: 0
    }

}