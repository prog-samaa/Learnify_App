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
import com.google.firebase.auth.FirebaseAuth

class CourseRepository(private val dao: CourseDao) {

    private val apiKey = "Api key"

    private fun getUid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getFavoriteCourses(): LiveData<List<CourseEntity>> = dao.getFavoriteCourses(getUid())
    fun getWatchLaterCourses(): LiveData<List<CourseEntity>> = dao.getWatchLaterCourses(getUid())
    fun getDoneCourses(): LiveData<List<CourseEntity>> = dao.getDoneCourses(getUid())

    suspend fun toggleFavorite(courseId: String, value: Boolean) = dao.setFavorite(courseId, getUid(), value)
    suspend fun toggleWatchLater(courseId: String, value: Boolean) = dao.setWatchLater(courseId, getUid(), value)
    suspend fun toggleDone(courseId: String, value: Boolean) = dao.setDone(courseId, getUid(), value)

    fun getTrendingLive(category: String): LiveData<List<CourseEntity>> = dao.getTrendingByCategory(category)

    suspend fun saveTrending(list: List<ChannelCourse>, channelId: String) {
        val currentUid = getUid()
        val existingCourses = dao.getCoursesList(true).associateBy { it.id }

        dao.clearTrendingForCategory(channelId)
        dao.insertCourses(list.map { channelCourse ->
            val existingCourse = existingCourses[channelCourse.id]
            channelCourse.toEntity(
                userId = currentUid,
                isTrending = true,
                category = channelId,
                isFavorite = existingCourse?.isFavorite ?: false,
                isWatchLater = existingCourse?.isWatchLater ?: false,
                isDone = existingCourse?.isDone ?: false
            )
        })
    }

    suspend fun searchAndSave(query: String, categoryKey: String): List<CourseEntity> {
        val currentUid = getUid()
        return try {
            val playlists = withContext(Dispatchers.IO) { RetrofitInstance.api.searchPlaylists(query = query, apiKey = apiKey) }
            val courses = playlists.items.map { playlist ->
                playlist.copy(rating = calculateRatingForPlaylist(playlist.playlistId.playlistId))
            }

            val existingCourses = dao.getCoursesList(false).associateBy { it.id }
            val entities = courses.map { searchCourse ->
                val existingCourse = existingCourses[searchCourse.playlistId.playlistId]
                searchCourse.toEntity(
                    userId = currentUid,
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
            dao.getCoursesListByCategory(categoryKey).ifEmpty { dao.getCoursesList(false) }
        }
    }

    suspend fun searchDirect(query: String): List<SearchCourse> {
        return try {
            val playlists = withContext(Dispatchers.IO) { RetrofitInstance.api.searchPlaylists(query = query, apiKey = apiKey) }
            playlists.items.map { playlist ->
                playlist.copy(rating = calculateRatingForPlaylist(playlist.playlistId.playlistId))
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getTrendingFromAPI(channelId: String): List<ChannelCourse> {
        return try {
            withContext(Dispatchers.IO) { RetrofitInstance.api.getChannelPlaylists(channelId = channelId, apiKey = apiKey).items }
        } catch (e: Exception) { emptyList() }
    }

    fun getCourseById(id: String): LiveData<CourseEntity> = dao.getCourseById(id, getUid())
    suspend fun getCourseByIdDirect(id: String): CourseEntity? = dao.getCourseByIdDirect(id, getUid())

    private suspend fun calculateRatingForPlaylist(playlistId: String): Float? {
        return try {
            val playlistItems = RetrofitInstance.api.getPlaylistItems(playlistId = playlistId, apiKey = apiKey)
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
            ((normalizedViews * popularityWeight) + (normalizedLikes * qualityWeight) * 5f).coerceIn(0f, 5f)
        } catch (e: Exception) { null }
    }
}

fun SearchCourse.toEntity(
    userId: String,
    isTrending: Boolean,
    category: String,
    isFavorite: Boolean = false,
    isWatchLater: Boolean = false,
    isDone: Boolean = false
): CourseEntity = CourseEntity(
    id = this.playlistId.playlistId,
    userId = userId,
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
    userId: String,
    isTrending: Boolean,
    category: String,
    isFavorite: Boolean = false,
    isWatchLater: Boolean = false,
    isDone: Boolean = false
): CourseEntity = CourseEntity(
    id = this.id,
    userId = userId,
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

fun CourseEntity.toChannelCourse() = ChannelCourse(
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
