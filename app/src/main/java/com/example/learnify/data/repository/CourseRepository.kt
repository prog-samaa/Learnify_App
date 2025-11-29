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

    private val apiKey = "AIzaSyD_Nntb2yllML53TAqll67lTzZaJNKFe3w"

    // Returns LiveData of trending courses for a specific category
    fun getTrendingLive(category: String): LiveData<List<CourseEntity>> =
        dao.getTrendingByCategory(category)

    // Save trending courses to DB for a specific category
    suspend fun saveTrending(list: List<ChannelCourse>, channelId: String) {
        dao.clearTrendingForCategory(channelId)
        dao.insertCourses(
            list.map { it.toEntity(isTrending = true, category = channelId) }
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

            val entities = courses.map { it.toEntity(false, category = categoryKey) }

            dao.clearCoursesForCategory(categoryKey) // replace existing for category
            dao.insertCourses(entities)

            entities
        } catch (e: Exception) {
            // Fallback to DB if API fails
            dao.getCoursesListByCategory(categoryKey).ifEmpty {
                dao.getCoursesList(false) // general fallback
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



    fun getCourseById(id: String): LiveData<CourseEntity> = dao.getCourseById(id)

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
}

// Extensions to convert SearchCourse/ChannelCourse -> CourseEntity with category
fun SearchCourse.toEntity(isTrending: Boolean, category: String): CourseEntity =
    CourseEntity(
        id = this.playlistId.playlistId,
        title = this.details.courseTitle,
        description = this.details.courseDescription,
        channelTitle = this.details.channelTitle,
        publishedAt = this.details.publishTime,
        imageUrl = this.details.imageUrl.thumbnail.url,
        rating = this.rating,
        isTrending = isTrending,
        category = category
    )

fun ChannelCourse.toEntity(isTrending: Boolean, category: String): CourseEntity =
    CourseEntity(
        id = this.id,
        title = this.details.courseTitle,
        description = this.details.courseDescription,
        channelTitle = this.details.channelTitle,
        publishedAt = this.details.publishTime,
        imageUrl = this.details.imageUrl.thumbnail.url,
        rating = this.rating,
        isTrending = isTrending,
        category = category
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
