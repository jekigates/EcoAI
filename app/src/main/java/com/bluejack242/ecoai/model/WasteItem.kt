package com.bluejack242.ecoai.model

import com.google.firebase.Timestamp

data class WasteItem(
    val id: String,
    val name: String,
    val co2e: Int,
    val date: Timestamp = Timestamp.now(),
    val imageRes: String,
    val sortingGuide: String
)
