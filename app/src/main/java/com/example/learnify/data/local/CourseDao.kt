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


    @Query("SELECT * FROM course_table WHERE id = :courseId LIMIT 1")
    fun getCourseById(courseId: String): LiveData<CourseEntity>

    @Query("SELECT * FROM course_table WHERE id IN (:ids)")
    fun getCoursesByIds(ids: List<String>): LiveData<List<CourseEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    // Inserts a list of courses into the database.
    // If a course with the same ID already exists, it will be replaced.

    // get all trending courses for a specific category
    @Query("SELECT * FROM course_table WHERE isTrending = 1 AND category = :category ORDER BY publishedAt DESC")
    fun getTrendingByCategory(category: String): LiveData<List<CourseEntity>>
    // Returns LiveData of trending courses filtered by the given category.
    // Sorted by publish date in descending order (latest first).

    // get courses for a specific non-trending category
    @Query("SELECT * FROM course_table WHERE isTrending = 0 AND category = :category ORDER BY publishedAt DESC")
    fun getCoursesByCategory(category: String): LiveData<List<CourseEntity>>
    // Returns LiveData of non-trending courses for a specific category.
    // Sorted by publish date descending.

    // sync helpers (suspend functions) for repository fallback or initial loads
    @Query("SELECT * FROM course_table WHERE isTrending = :trending ORDER BY publishedAt DESC")
    suspend fun getCoursesList(trending: Boolean): List<CourseEntity>
    // Returns a list of courses based on the trending flag.
    // Used for synchronous operations, not LiveData.

    @Query("SELECT * FROM course_table WHERE isTrending = 0 AND category = :category ORDER BY publishedAt DESC")
    suspend fun getCoursesListByCategory(category: String): List<CourseEntity>
    // Returns a list of non-trending courses for a specific category.
    // Used for synchronous operations, typically as a fallback if API fails.

    // clear trending courses for a specific category
    @Query("DELETE FROM course_table WHERE isTrending = 1 AND category = :category")
    suspend fun clearTrendingForCategory(category: String)
    // Deletes all trending courses for the given category.
    // Useful before inserting fresh trending data for that category.

    // clear non-trending courses for a specific category
    @Query("DELETE FROM course_table WHERE isTrending = 0 AND category = :category")
    suspend fun clearCoursesForCategory(category: String)
    // Deletes all non-trending courses for the given category.
    // Useful before inserting fresh search results for that category.

    // تحديث المفضلة
    @Query("UPDATE course_table SET isFavorite = :fav WHERE id = :courseId")
    suspend fun setFavorite(courseId: String, fav: Boolean)

    // تحديث WatchLater
    @Query("UPDATE course_table SET isWatchLater = :watch WHERE id = :courseId")
    suspend fun setWatchLater(courseId: String, watch: Boolean)
// تحديث المكتملة
    @Query("UPDATE course_table SET isDone = :done WHERE id = :courseId")
    suspend fun setDone(courseId: String, done: Boolean)

    // LiveData للكورسات المفضلة
    @Query("SELECT * FROM course_table WHERE isFavorite = 1 ORDER BY publishedAt DESC")
    fun getFavoriteCourses(): LiveData<List<CourseEntity>>

    // LiveData للكورسات في WatchLater
    @Query("SELECT * FROM course_table WHERE isWatchLater = 1 ORDER BY publishedAt DESC")
    fun getWatchLaterCourses(): LiveData<List<CourseEntity>>

    // LiveData للكورسات المكتملة
    @Query("SELECT * FROM course_table WHERE isDone = 1 ORDER BY publishedAt DESC")
    fun getDoneCourses(): LiveData<List<CourseEntity>>

}
