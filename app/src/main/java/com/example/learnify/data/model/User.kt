package com.example.learnify.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val watchlist: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val doneCourses: List<String> = emptyList()

)