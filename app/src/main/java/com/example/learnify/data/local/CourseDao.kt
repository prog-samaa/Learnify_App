package com.example.learnify.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao {

    @Query("SELECT * FROM course_table WHERE id = :id LIMIT 1")
    suspend fun getCourseByIdDirect(id: String): CourseEntity?

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

    @Query("UPDATE course_table SET isFavorite = :fav WHERE id = :courseId")
    suspend fun setFavorite(courseId: String, fav: Boolean)

    @Query("UPDATE course_table SET isWatchLater = :watch WHERE id = :courseId")
    suspend fun setWatchLater(courseId: String, watch: Boolean)

    @Query("UPDATE course_table SET isDone = :done WHERE id = :courseId")
    suspend fun setDone(courseId: String, done: Boolean)

    @Query("SELECT * FROM course_table WHERE isFavorite = 1 ORDER BY publishedAt DESC")
    fun getFavoriteCourses(): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM course_table WHERE isWatchLater = 1 ORDER BY publishedAt DESC")
    fun getWatchLaterCourses(): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM course_table WHERE isDone = 1 ORDER BY publishedAt DESC")
    fun getDoneCourses(): LiveData<List<CourseEntity>>
}
