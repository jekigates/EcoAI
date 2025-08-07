package edu.bluejack24_2.ecoai.model

data class PostRequest(
    val headline: String = "",
    val caption: String = "",
    val mediaList: List<MediaItem> = emptyList()
)
