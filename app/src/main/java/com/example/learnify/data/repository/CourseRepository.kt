package com.example.learnify.data.repository

import androidx.lifecycle.LiveData
import com.example.learnify.data.local.CourseDao
import com.example.learnify.data.local.CourseEntity
import com.example.learnify.data.model.ChannelCourse
import com.example.learnify.data.model.SearchCourse
import com.example.learnify.data.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.log10

class CourseRepository(private val dao: CourseDao) {

    private val apiKey = "Your api key"

    // Returns LiveData of trending courses for a specific category
    fun getTrendingLive(category: String): LiveData<List<CourseEntity>> =
        dao.getTrendingByCategory(category)

    // Save trending courses to DB for a specific category
    suspend fun saveTrending(list: List<ChannelCourse>, channelId: String) {
        //  نجيب البيانات الحالية من الداتابيز عشان نحفظ حالة المفضلة
        val existingCourses = dao.getCoursesList(true).associateBy { it.id }

        dao.clearTrendingForCategory(channelId)
        dao.insertCourses(
            list.map { channelCourse ->
                val existingCourse = existingCourses[channelCourse.id]
                channelCourse.toEntity(
                    isTrending = true,
                    category = channelId,
                    isFavorite = existingCourse?.isFavorite ?: false, // نحفظ الحالة القديمة
                    isWatchLater = existingCourse?.isWatchLater ?: false , // نحفظ الحالة القديمة
                    isDone = existingCourse?.isDone ?: false

                )
            }
        )
    }

    // Load trending courses from DB synchronously
    suspend fun loadTrendingFromDB(): List<ChannelCourse> {
        return dao.getCoursesList(true).map { it.toChannelCourse() }
    }

    // Search courses from API, save in DB under given category, return saved entities
    suspend fun searchAndSave(query: String, categoryKey: String): List<CourseEntity> {
        return try {
            val playlists = withContext(Dispatchers.IO) {
                RetrofitInstance.api.searchPlaylists(query = query, apiKey = apiKey)
            }

            val courses = playlists.items.map { playlist ->
                playlist.copy(rating = calculateRatingForPlaylist(playlist.playlistId.playlistId))
            }

            //  نحفظ حالة المفضلة والواتش ليتير و المكتملة
            val existingCourses = dao.getCoursesList(false).associateBy { it.id }

            val entities = courses.map { searchCourse ->
                val existingCourse = existingCourses[searchCourse.playlistId.playlistId]
                searchCourse.toEntity(
                    isTrending = false,
                    category = categoryKey,
                    isFavorite = existingCourse?.isFavorite ?: false, // نحفظ الحالة القديمة
                    isWatchLater = existingCourse?.isWatchLater ?: false, // نحفظ الحالة القديمة
                    isDone = existingCourse?.isDone ?: false

                )
            }

            dao.clearCoursesForCategory(categoryKey)
            dao.insertCourses(entities)

            entities
        } catch (e: Exception) {
            // Fallback to DB if API fails
            dao.getCoursesListByCategory(categoryKey).ifEmpty {
                dao.getCoursesList(false)
            }
        }
    }

    // Direct search without saving to DB
    suspend fun searchDirect(query: String): List<SearchCourse> {
        return try {
            val playlists = withContext(Dispatchers.IO) {
                RetrofitInstance.api.searchPlaylists(query = query, apiKey = apiKey)
            }
            playlists.items.map { playlist ->
                playlist.copy(rating = calculateRatingForPlaylist(playlist.playlistId.playlistId))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fetch trending courses from API only (no DB save)
    suspend fun getTrendingFromAPI(channelId: String): List<ChannelCourse> {
        return try {
            withContext(Dispatchers.IO) {
                RetrofitInstance.api.getChannelPlaylists(channelId = channelId, apiKey = apiKey).items
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    //  إدخال أو تحديث كورس مع الحفاظ على الحالة
    suspend fun insertOrUpdateCourse(course: CourseEntity) {
        val existingCourse = dao.getCourseByIdDirect(course.id)
        if (existingCourse != null) {
            // إذا الكورس موجود، نحدثه مع الحفاظ على حالة المفضلة والواتش ليتير و المكتملة
            val updatedCourse = course.copy(
                isFavorite = existingCourse.isFavorite,
                isWatchLater = existingCourse.isWatchLater,
                isDone = existingCourse.isDone
            )
            dao.insertCourses(listOf(updatedCourse))
        } else {
            // إذا الكورس جديد، نضيفه
            dao.insertCourses(listOf(course))
        }
    }

    fun getCourseById(id: String): LiveData<CourseEntity> = dao.getCourseById(id)

    suspend fun getCourseByIdDirect(id: String): CourseEntity? {
        return dao.getCourseByIdDirect(id)
    }

    // Calculate a rating for a playlist based on views and likes
    private suspend fun calculateRatingForPlaylist(playlistId: String): Float? {
        return try {
            val playlistItems = RetrofitInstance.api.getPlaylistItems(
                playlistId = playlistId,
                apiKey = apiKey
            )
            val firstVideoId = playlistItems.items.firstOrNull()?.contentDetails?.videoId ?: return null
            val videoStats = RetrofitInstance.api.getVideoStats(videoId = firstVideoId, apiKey = apiKey)
            val stats = videoStats.items.firstOrNull()?.statistics ?: return null

            val views = stats.viewCount.toFloatOrNull() ?: return null
            val likes = stats.likeCount.toFloatOrNull() ?: 0f

            if (views < 100) return 1f

            val logViews = log10(views + 1)
            val logLikes = log10(likes + 1)
            val likeRatio = (likes / views).coerceIn(0f, 1f)

            val popularityWeight = 0.6f
            val qualityWeight = 0.4f

            val normalizedViews = (logViews / 6f).coerceIn(0f, 1f)
            val normalizedLikes = ((logLikes / 4f) + likeRatio).coerceIn(0f, 1f)

            val weightedScore = (normalizedViews * popularityWeight) + (normalizedLikes * qualityWeight)
            (weightedScore * 5f).coerceIn(0f, 5f)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun toggleFavorite(courseId: String, value: Boolean) {
        dao.setFavorite(courseId, value)
    }

    suspend fun toggleWatchLater(courseId: String, value: Boolean) {
        dao.setWatchLater(courseId, value)
    }
    suspend fun toggleDone(courseId: String, value: Boolean) {
        dao.setDone(courseId, value)
    }

}

// Extensions to convert SearchCourse/ChannelCourse -> CourseEntity with category
fun SearchCourse.toEntity(
    isTrending: Boolean,
    category: String,
    isFavorite: Boolean = false,
    isWatchLater: Boolean = false,
    isDone: Boolean = false // أضف هذا
): CourseEntity =
    CourseEntity(
        id = this.playlistId.playlistId,
        title = this.details.courseTitle,
        description = this.details.courseDescription,
        channelTitle = this.details.channelTitle,
        publishedAt = this.details.publishTime,
        imageUrl = this.details.imageUrl.thumbnail.url,
        rating = this.rating,
        isTrending = isTrending,
        category = category,
        isFavorite = isFavorite,
        isWatchLater = isWatchLater,
        isDone = isDone
    )

fun ChannelCourse.toEntity(
    isTrending: Boolean,
    category: String,
    isFavorite: Boolean = false,
    isWatchLater: Boolean = false,
    isDone: Boolean = false
): CourseEntity =
    CourseEntity(
        id = this.id,
        title = this.details.courseTitle,
        description = this.details.courseDescription,
        channelTitle = this.details.channelTitle,
        publishedAt = this.details.publishTime,
        imageUrl = this.details.imageUrl.thumbnail.url,
        rating = this.rating,
        isTrending = isTrending,
        category = category,
        isFavorite = isFavorite,
        isWatchLater = isWatchLater,
        isDone = isDone
    )

// Converters back (if needed)
fun CourseEntity.toSearchCourse() = com.example.learnify.data.model.SearchCourse(
    details = com.example.learnify.data.model.PlaylistSnippet(
        courseTitle = this.title,
        courseDescription = this.description,
        channelTitle = this.channelTitle,
        publishTime = this.publishedAt,
        imageUrl = com.example.learnify.data.model.PlaylistThumbnails(
            thumbnail = com.example.learnify.data.model.ThumbnailDetail(this.imageUrl)
        )
    ),
    playlistId = com.example.learnify.data.model.PlaylistId(this.id),
    rating = this.rating
)

fun CourseEntity.toChannelCourse() = com.example.learnify.data.model.ChannelCourse(
    details = com.example.learnify.data.model.PlaylistSnippet(
        courseTitle = this.title,
        courseDescription = this.description,
        channelTitle = this.channelTitle,
        publishTime = this.publishedAt,
        imageUrl = com.example.learnify.data.model.PlaylistThumbnails(
            thumbnail = com.example.learnify.data.model.ThumbnailDetail(this.imageUrl)
        )
    ),
    id = this.id,
    rating = this.rating
)