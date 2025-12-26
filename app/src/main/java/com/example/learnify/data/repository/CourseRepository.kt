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

    private val apiKey = ""

    fun getTrendingLive(category: String): LiveData<List<CourseEntity>> =
        dao.getTrendingByCategory(category)

    suspend fun saveTrending(list: List<ChannelCourse>, channelId: String) {
        val existingCourses = dao.getCoursesList(true).associateBy { it.id }

        dao.clearTrendingForCategory(channelId)
        dao.insertCourses(
            list.map { channelCourse ->
                val existingCourse = existingCourses[channelCourse.id]
                channelCourse.toEntity(
                    isTrending = true,
                    category = channelId,
                    isFavorite = existingCourse?.isFavorite ?: false,
                    isWatchLater = existingCourse?.isWatchLater ?: false,
                    isDone = existingCourse?.isDone ?: false
                )
            }
        )
    }

    suspend fun searchAndSave(query: String, categoryKey: String): List<CourseEntity> {
        return try {
            val playlists = withContext(Dispatchers.IO) {
                RetrofitInstance.api.searchPlaylists(query = query, apiKey = apiKey)
            }

            val courses = playlists.items.map { playlist ->
                playlist.copy(rating = calculateRatingForPlaylist(playlist.playlistId.playlistId))
            }

            val existingCourses = dao.getCoursesList(false).associateBy { it.id }

            val entities = courses.map { searchCourse ->
                val existingCourse = existingCourses[searchCourse.playlistId.playlistId]
                searchCourse.toEntity(
                    isTrending = false,
                    category = categoryKey,
                    isFavorite = existingCourse?.isFavorite ?: false,
                    isWatchLater = existingCourse?.isWatchLater ?: false,
                    isDone = existingCourse?.isDone ?: false
                )
            }

            dao.clearCoursesForCategory(categoryKey)
            dao.insertCourses(entities)

            entities
        } catch (e: Exception) {
            dao.getCoursesListByCategory(categoryKey).ifEmpty {
                dao.getCoursesList(false)
            }
        }
    }

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

    suspend fun getTrendingFromAPI(channelId: String): List<ChannelCourse> {
        return try {
            withContext(Dispatchers.IO) {
                RetrofitInstance.api.getChannelPlaylists(channelId = channelId, apiKey = apiKey).items
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

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

fun SearchCourse.toEntity(
    isTrending: Boolean,
    category: String,
    isFavorite: Boolean = false,
    isWatchLater: Boolean = false,
    isDone: Boolean = false
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
