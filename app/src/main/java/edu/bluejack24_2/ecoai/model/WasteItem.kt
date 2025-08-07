package edu.bluejack24_2.ecoai.model

data class WasteItem(
    val id: String = "",
    val name: String = "",
    val co2e: Int = 0,
    val imageRes: String = "",
    val sortingGuide: String = ""
)