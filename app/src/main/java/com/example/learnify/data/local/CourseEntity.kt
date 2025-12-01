package com.example.learnify.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_table")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val channelTitle: String,
    val publishedAt: String,
    val imageUrl: String,
    val rating: Float?,
    val isTrending: Boolean,
    val category: String , //  category key (e.g. "programming", "medical", "home", etc.)
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false ,
    val isDone: Boolean = false

)
