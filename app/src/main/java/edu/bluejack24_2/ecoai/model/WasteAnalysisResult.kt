package edu.bluejack24_2.ecoai.model

data class WasteAnalysisResult(
    val name: String,
    val item_details: String,
    val carbon_footprint_data: Int,
    val disposal_methods: String
)
