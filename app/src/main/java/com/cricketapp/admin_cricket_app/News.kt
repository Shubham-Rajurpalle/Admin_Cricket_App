package com.cricketapp.admin_cricket_app

data class News(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val views: Int = 0,
    val timestamp: Long = 0,
    val newsContent: String = ""
)
