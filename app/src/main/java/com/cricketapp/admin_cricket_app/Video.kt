package com.cricketapp.admin_cricket_app

data class Video(
    val id: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    var views: Int = 0,
    val timestamp: Long = 0
)
