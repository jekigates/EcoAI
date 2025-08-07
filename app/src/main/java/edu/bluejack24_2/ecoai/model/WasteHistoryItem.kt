package edu.bluejack24_2.ecoai.model

import com.google.firebase.Timestamp

data class WasteHistoryItem(
    val id: String = "",
    val name: String = "",
    val co2e: Int = 0,
    val date: Timestamp = Timestamp.now(),
    val imageRes: String = "",
    val uploadedBy: String = "",
    val disposalMethod: String = ""
)
