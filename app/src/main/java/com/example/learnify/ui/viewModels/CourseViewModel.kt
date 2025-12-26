package com.example.learnify.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.example.learnify.data.local.CourseDatabase
import com.example.learnify.data.local.CourseEntity
import com.example.learnify.data.repository.CourseRepository
import com.example.learnify.data.repository.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CourseDatabase.getDatabase(application).courseDao()
    private val repository = CourseRepository(dao)

    private fun getUid() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _currentCourse = MutableStateFlow<CourseEntity?>(null)
    val currentCourse: StateFlow<CourseEntity?> = _currentCourse.asStateFlow()

    fun initializeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteCourses()
            repository.getWatchLaterCourses()
            repository.getDoneCourses()
        }
    }

    fun syncCoursesFromFirestore(favIds: List<String>, watchIds: List<String>, doneIds: List<String>) {
        viewModelScope.launch {
            val allIds = (favIds + watchIds + doneIds).distinct()
            allIds.forEach { id ->
                val exists = repository.getCourseByIdDirect(id)
                if (exists == null) {
                    repository.searchAndSave(query = id, categoryKey = "synced_courses")
                } else {
                    val updatedCourse = exists.copy(
                        isFavorite = favIds.contains(id),
                        isWatchLater = watchIds.contains(id),
                        isDone = doneIds.contains(id)
                    )
                    dao.insertCourses(listOf(updatedCourse))
                }
            }
        }
    }

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            val course = repository.getCourseByIdDirect(courseId)
            _currentCourse.value = course
        }
    }

    val favoriteCourses: LiveData<List<CourseEntity>> = repository.getFavoriteCourses()
    val watchLaterCourses: LiveData<List<CourseEntity>> = repository.getWatchLaterCourses()
    val doneCourses: LiveData<List<CourseEntity>> = repository.getDoneCourses()

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

    fun generalCoursesByCategory(category: String): LiveData<List<CourseEntity>> {
        return generalMap.getOrPut(category) { MutableLiveData(emptyList()) }
    }

    private val loadedSearchQueries = mutableSetOf<String>()
    private val loadedTrending = mutableSetOf<String>()

    fun getCourseById(id: String): LiveData<CourseEntity> {
        return repository.getCourseById(id)
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
                _searchResults.value = result.map {
                    it.toEntity(userId = getUid(), isTrending = false, category = "search")
                }
            } catch (e: Exception) {
                _searchError.value = e.message
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    fun searchCourses(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val categoryKey = detectCategoryKeyFromQuery(q)
        val liveData = generalMap.getOrPut(categoryKey) { MutableLiveData(emptyList()) }
        if (loadedSearchQueries.contains(q)) {
            viewModelScope.launch {
                val list = dao.getCoursesListByCategory(categoryKey)
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

    fun toggleFavorite(courseId: String, value: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(courseId, value)
            loadCourse(courseId)
        }
    }

    fun toggleWatchLater(courseId: String, value: Boolean) {
        viewModelScope.launch {
            repository.toggleWatchLater(courseId, value)
            loadCourse(courseId)
        }
    }

    fun toggleDone(courseId: String, value: Boolean) {
        viewModelScope.launch {
            repository.toggleDone(courseId, value)
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
