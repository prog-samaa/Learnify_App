package com.example.learnify.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao {

    @Query("SELECT * FROM course_table WHERE isFavorite = 1 AND userId = :userId ORDER BY publishedAt DESC")
    fun getFavoriteCourses(userId: String): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM course_table WHERE isWatchLater = 1 AND userId = :userId ORDER BY publishedAt DESC")
    fun getWatchLaterCourses(userId: String): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM course_table WHERE isDone = 1 AND userId = :userId ORDER BY publishedAt DESC")
    fun getDoneCourses(userId: String): LiveData<List<CourseEntity>>

    @Query("UPDATE course_table SET isFavorite = :fav WHERE id = :courseId AND userId = :userId")
    suspend fun setFavorite(courseId: String, userId: String, fav: Boolean)

    @Query("UPDATE course_table SET isWatchLater = :watch WHERE id = :courseId AND userId = :userId")
    suspend fun setWatchLater(courseId: String, userId: String, watch: Boolean)

    @Query("UPDATE course_table SET isDone = :done WHERE id = :courseId AND userId = :userId")
    suspend fun setDone(courseId: String, userId: String, done: Boolean)

    @Query("SELECT * FROM course_table WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getCourseByIdDirect(id: String, userId: String): CourseEntity?

    @Query("SELECT * FROM course_table WHERE id = :courseId AND userId = :userId LIMIT 1")
    fun getCourseById(courseId: String, userId: String): LiveData<CourseEntity>

    @Query("SELECT * FROM course_table WHERE id IN (:ids)")
    fun getCoursesByIds(ids: List<String>): LiveData<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("SELECT * FROM course_table WHERE isTrending = 1 AND category = :category ORDER BY publishedAt DESC")
    fun getTrendingByCategory(category: String): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM course_table WHERE isTrending = 0 AND category = :category ORDER BY publishedAt DESC")
    fun getCoursesByCategory(category: String): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM course_table WHERE isTrending = :trending ORDER BY publishedAt DESC")
    suspend fun getCoursesList(trending: Boolean): List<CourseEntity>

    @Query("SELECT * FROM course_table WHERE isTrending = 0 AND category = :category ORDER BY publishedAt DESC")
    suspend fun getCoursesListByCategory(category: String): List<CourseEntity>

    @Query("DELETE FROM course_table WHERE isTrending = 1 AND category = :category")
    suspend fun clearTrendingForCategory(category: String)

    @Query("DELETE FROM course_table WHERE isTrending = 0 AND category = :category")
    suspend fun clearCoursesForCategory(category: String)
}