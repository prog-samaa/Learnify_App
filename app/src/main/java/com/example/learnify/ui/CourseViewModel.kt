package com.example.learnify.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.learnify.data.local.CourseDatabase
import com.example.learnify.data.local.CourseEntity
import com.example.learnify.data.repository.CourseRepository
import com.example.learnify.data.repository.toEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CourseDatabase.getDatabase(application).courseDao()
    private val repository = CourseRepository(dao)

    // StateFlow  لحل مشكلة تحديث الـ
    private val _currentCourse = MutableStateFlow<CourseEntity?>(null)
    val currentCourse: StateFlow<CourseEntity?> = _currentCourse.asStateFlow()

    // دالة لتحميل الكورس الحالي
    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            val course = dao.getCourseByIdDirect(courseId)
            _currentCourse.value = course
        }
    }

    //  تحميل المفضلة عند بداية التطبيق
    fun initializeFavorites() {
        viewModelScope.launch {
            // تفعيل الـ Observers للتأكد من تحميل البيانات
            favoriteCourses.value
            watchLaterCourses.value
            doneCourses.value

        }
    }

    fun trendingCourses(category: String): LiveData<List<CourseEntity>> {
        return repository.getTrendingLive(category)
    }

    private val generalMap = mutableMapOf<String, MutableLiveData<List<CourseEntity>>>()

    fun generalCoursesByCategory(category: String): LiveData<List<CourseEntity>> {
        return generalMap.getOrPut(category) { MutableLiveData(emptyList()) }
    }

    private val _searchResults = MutableLiveData<List<CourseEntity>>()
    val searchResults: LiveData<List<CourseEntity>> = _searchResults

    // Loading states
    private val _isTrendingLoading = MutableLiveData(false)
    val isTrendingLoading: LiveData<Boolean> = _isTrendingLoading

    private val _isSearchLoading = MutableLiveData(false)
    val isSearchLoading: LiveData<Boolean> = _isSearchLoading

    private val _isGeneralLoading = MutableLiveData(false)
    val isGeneralLoading: LiveData<Boolean> = _isGeneralLoading

    // Error states
    private val _trendingError = MutableLiveData<String?>(null)
    val trendingError: LiveData<String?> = _trendingError

    private val _searchError = MutableLiveData<String?>(null)
    val searchError: LiveData<String?> = _searchError

    private val _generalError = MutableLiveData<String?>(null)
    val generalError: LiveData<String?> = _generalError

    // caches
    private val loadedSearchQueries = mutableSetOf<String>()
    private val loadedCategories = mutableSetOf<String>()
    private val loadedTrending = mutableSetOf<String>()

    fun getCoursesByIds(ids: List<String>): LiveData<List<CourseEntity>> {
        val result = MutableLiveData<List<CourseEntity>>()

        viewModelScope.launch {
            val list = ids.mapNotNull { id ->
                dao.getCourseByIdDirect(id)
            }
            result.postValue(list)
        }

        return result
    }

    fun getCourseById(id: String): LiveData<CourseEntity> {
        return repository.getCourseById(id)
    }

    // detect category
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

    fun searchCoursesDirect(query: String) {
        viewModelScope.launch {
            _isSearchLoading.value = true
            try {
                val result = repository.searchDirect(query)

                _searchResults.value = result.map {
                    it.toEntity(
                        isTrending = false,
                        category = "search"   // temporary
                    )
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
                loadedCategories.add(categoryKey)
                _generalError.value = null
            } catch (e: Exception) {
                _generalError.value = e.message
            } finally {
                _isGeneralLoading.value = false
            }
        }
    }

    fun getTrendingCourses(category: String) {
        val id = category.trim()
        if (id.isEmpty()) return

        if (loadedTrending.contains(id)) return

        viewModelScope.launch {
            _isTrendingLoading.value = true
            try {
                val api = repository.getTrendingFromAPI(id)
                if (api.isNotEmpty())
                    repository.saveTrending(api, id)

                loadedTrending.add(id)

                _trendingError.value = null
            } catch (e: Exception) {
                _trendingError.value = e.message
            } finally {
                _isTrendingLoading.value = false
            }
        }
    }

    fun refreshTrending(category: String) {
        val id = category.trim()
        if (id.isEmpty()) return
        viewModelScope.launch {
            _isTrendingLoading.value = true
            try {
                val api = repository.getTrendingFromAPI(id)
                if (api.isNotEmpty()) repository.saveTrending(api, id)
                _trendingError.value = null
            } catch (e: Exception) {
                _trendingError.value = e.message
            } finally {
                _isTrendingLoading.value = false
            }
        }
    }

    fun refreshSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return

        val categoryKey = detectCategoryKeyFromQuery(q)
        val liveData = generalMap.getOrPut(categoryKey) { MutableLiveData(emptyList()) }

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

    fun clearLoadedCaches() {
        loadedSearchQueries.clear()
        loadedCategories.clear()
        loadedTrending.clear()
    }

    val favoriteCourses: LiveData<List<CourseEntity>> = dao.getFavoriteCourses()
    val watchLaterCourses: LiveData<List<CourseEntity>> = dao.getWatchLaterCourses()
    val doneCourses: LiveData<List<CourseEntity>> = dao.getDoneCourses()

    fun toggleFavorite(courseId: String, value: Boolean) {
        viewModelScope.launch {
            val currentCourse = dao.getCourseByIdDirect(courseId)

            dao.setFavorite(courseId, value)
            loadCourse(courseId)
            if (currentCourse == null) {
            }
        }
    }

    fun toggleWatchLater(courseId: String, value: Boolean) {
        viewModelScope.launch {
            val currentCourse = dao.getCourseByIdDirect(courseId)
            dao.setWatchLater(courseId, value)
            loadCourse(courseId)

            if (currentCourse == null) {
            }
        }
    }
    fun toggleDone(courseId: String, value: Boolean) {
        viewModelScope.launch {
            dao.setDone(courseId, value)
            loadCourse(courseId)
        }
    }

    //  التحقق من حالة الكورس
    suspend fun getCourseState(courseId: String): Pair<Boolean, Boolean> {
        val course = dao.getCourseByIdDirect(courseId)
        return Pair(course?.isFavorite ?: false, course?.isWatchLater ?: false)
    }
}