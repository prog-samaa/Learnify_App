package com.example.learnify.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.example.learnify.data.local.CourseDatabase
import com.example.learnify.data.local.CourseEntity
import com.example.learnify.data.repository.CourseRepository
import com.example.learnify.data.repository.UserRepository
import com.example.learnify.data.repository.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CourseDatabase.getDatabase(application).courseDao()
    private val repository = CourseRepository(dao)
    private val userRepo = UserRepository()

    private val _currentUid = MutableLiveData<String>(FirebaseAuth.getInstance().currentUser?.uid ?: "")

    private val _isSyncing = MutableLiveData(false)
    val isSyncing: LiveData<Boolean> = _isSyncing

    private val _syncMessage = MutableLiveData<String?>(null)
    val syncMessage: LiveData<String?> = _syncMessage

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    val favoriteCourses: LiveData<List<CourseEntity>> = _currentUid.switchMap {
        repository.getFavoriteCourses()
    }

    val watchLaterCourses: LiveData<List<CourseEntity>> = _currentUid.switchMap {
        repository.getWatchLaterCourses()
    }

    val doneCourses: LiveData<List<CourseEntity>> = _currentUid.switchMap {
        repository.getDoneCourses()
    }

    private fun getUid() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun refreshUserSession() {
        val newUid = getUid()
        if (_currentUid.value != newUid) {
            _currentUid.value = newUid
            clearInternalCache()
        }
    }

    private val _currentCourse = MutableStateFlow<CourseEntity?>(null)
    val currentCourse: StateFlow<CourseEntity?> = _currentCourse.asStateFlow()

    fun clearInternalCache() {
        loadedSearchQueries.clear()
        loadedTrending.clear()
        generalMap.clear()
        _searchResults.value = emptyList()
        _currentCourse.value = null
    }

    fun clearDatabaseForUser() {
        viewModelScope.launch {
            val uid = getUid()
            if (uid.isNotEmpty()) {
                dao.clearAllUserData(uid)
            }
            clearInternalCache()
        }
    }

    fun syncCoursesFromFirestore(favIds: List<String>, watchIds: List<String>, doneIds: List<String>) {
        if (_isSyncing.value == true) return

        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val allIds = (favIds + watchIds + doneIds).distinct()
                val uid = getUid()
                allIds.forEach { id ->
                    val exists = repository.getCourseByIdDirect(id)
                    if (exists == null) {
                        repository.searchAndSave(query = id, categoryKey = "synced_courses")
                    } else {
                        val updatedCourse = exists.copy(
                            userId = uid,
                            isFavorite = favIds.contains(id),
                            isWatchLater = watchIds.contains(id),
                            isDone = doneIds.contains(id)
                        )
                        dao.insertCourses(listOf(updatedCourse))
                    }
                }
                _syncMessage.postValue("Updated successfully ✅")
            } catch (e: Exception) {
                _syncMessage.postValue("Update failed: ${e.message ?: "Unknown error"}")
            } finally {
                delay(250)
                _isSyncing.value = false
            }
        }
    }

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            val course = repository.getCourseByIdDirect(courseId)
            _currentCourse.value = course
        }
    }

    private val _searchResults = MutableLiveData<List<CourseEntity>>()
    val searchResults: LiveData<List<CourseEntity>> = _searchResults

    private val _isTrendingLoading = MutableLiveData(false)
    val isTrendingLoading: LiveData<Boolean> = _isTrendingLoading

    private val _isSearchLoading = MutableLiveData(false)
    val isSearchLoading: LiveData<Boolean> = _isSearchLoading

    private val _isGeneralLoading = MutableLiveData(false)
    val isGeneralLoading: LiveData<Boolean> = _isGeneralLoading

    private val _trendingError = MutableLiveData<String?>(null)
    val trendingError: LiveData<String?> = _trendingError

    private val _searchError = MutableLiveData<String?>(null)
    val searchError: LiveData<String?> = _searchError

    private val _generalError = MutableLiveData<String?>(null)
    val generalError: LiveData<String?> = _generalError

    private val generalMap = mutableMapOf<String, MutableLiveData<List<CourseEntity>>>()
    private val loadedSearchQueries = mutableSetOf<String>()
    private val loadedTrending = mutableSetOf<String>()

    fun generalCoursesByCategory(category: String): LiveData<List<CourseEntity>> {
        return generalMap.getOrPut(category) { MutableLiveData(emptyList()) }
    }

    fun getCourseById(id: String): LiveData<CourseEntity> {
        return repository.getCourseById(id)
    }

    fun searchCourses(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val categoryKey = detectCategoryKeyFromQuery(q)
        val liveData = generalMap.getOrPut(categoryKey) { MutableLiveData(emptyList()) }

        if (loadedSearchQueries.contains(q)) {
            viewModelScope.launch {
                val list = dao.getCoursesListByCategory(categoryKey, getUid())
                if (list.isNotEmpty()) liveData.postValue(list)
            }
            return
        }

        viewModelScope.launch {
            _isGeneralLoading.value = true
            try {
                val saved = repository.searchAndSave(q, categoryKey)
                liveData.value = saved
                loadedSearchQueries.add(q)
                _generalError.value = null
            } catch (e: Exception) {
                _generalError.value = e.message
            } finally {
                _isGeneralLoading.value = false
            }
        }
    }

    fun refreshTrending(category: String) {
        val id = category.trim()
        loadedTrending.remove(id)
        getTrendingCourses(id)
    }

    fun getTrendingCourses(category: String) {
        val id = category.trim()
        if (id.isEmpty() || loadedTrending.contains(id)) return
        viewModelScope.launch {
            _isTrendingLoading.value = true
            try {
                val api = repository.getTrendingFromAPI(id)
                if (api.isNotEmpty()) {
                    repository.saveTrending(api, id)
                    loadedTrending.add(id)
                }
                _trendingError.value = null
            } catch (e: Exception) {
                _trendingError.value = e.message
            } finally {
                _isTrendingLoading.value = false
            }
        }
    }

    fun trendingCourses(category: String): LiveData<List<CourseEntity>> {
        return repository.getTrendingLive(category)
    }

    fun searchCoursesDirect(query: String) {
        viewModelScope.launch {
            _isSearchLoading.value = true
            try {
                val result = repository.searchDirect(query)
                val uid = getUid()
                val entities = result.map { it.toEntity(userId = uid, isTrending = false, category = "search") }
                dao.insertCourses(entities)
                _searchResults.value = entities
                _searchError.value = null
            } catch (e: Exception) {
                _searchError.value = e.message
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    fun toggleFavorite(courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(courseId, !currentStatus)
            if (!currentStatus) userRepo.addToFavorites(courseId) else userRepo.removeFromFavorites(courseId)
            loadCourse(courseId)
        }
    }

    fun toggleWatchLater(courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleWatchLater(courseId, !currentStatus)
            if (!currentStatus) userRepo.addToWatchlist(courseId) else userRepo.removeFromWatchlist(courseId)
            loadCourse(courseId)
        }
    }

    fun toggleDone(courseId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleDone(courseId, !currentStatus)
            if (!currentStatus) userRepo.addToDoneCourses(courseId) else userRepo.removeFromDoneCourses(courseId)
            loadCourse(courseId)
        }
    }

    fun detectCategoryKeyFromQuery(query: String): String {
        val q = query.lowercase()
        return when {
            "program" in q -> "programming"
            "engineer" in q -> "engineering"
            "medical" in q || "medicine" in q -> "medical"
            "marketing" in q -> "marketing"
            "language" in q -> "language"
            "human" in q || "development" in q -> "human_dev"
            q == "courses" || q == "home" -> "home"
            else -> q.replace("""\s+""".toRegex(), "_")
        }
    }
}
