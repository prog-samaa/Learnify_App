package com.example.learnify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import com.example.learnify.data.model.User
import com.example.learnify.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repo = UserRepository()

    val isSuccess = mutableStateOf(false)
    val isLoggedIn = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val currentUser = mutableStateOf<User?>(null)

    init { observeCurrentUser() }

    private fun observeCurrentUser() {
        val firebaseUser = repo.getCurrentUser() ?: return
        val uid = firebaseUser.uid

        repo.listenToUserRealtime(uid) { data ->
            if (data != null) {
                currentUser.value = User(
                    uid = uid,
                    name = data["name"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    phone = data["phone"] as? String ?: "",
                    imageUrl = data["imageUrl"] as? String ?: "",
                    watchlist = data["watchlist"] as? List<String> ?: emptyList(),
                    favorites = data["favorites"] as? List<String> ?: emptyList(),
                    doneCourses = data["doneCourses"] as? List<String> ?: emptyList()
                )
                isLoggedIn.value = true
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            try {
                repo.registerUser(name, email, phone, password)
                isSuccess.value = true
                isLoggedIn.value = true
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Registration failed"
                isSuccess.value = false
            }
        }
    }


//    fun login(email: String, password: String) {
//        viewModelScope.launch {
//            try {
//                repo.loginUser(email, password)
//                isLoggedIn.value = true
//                observeCurrentUser()
//            } catch (e: Exception) {
//                errorMessage.value = e.message ?: "Login failed"
//            }
//        }
//    }
fun login(email: String, password: String) {
    viewModelScope.launch {
        try {
            val success = repo.loginUser(email, password)
            isLoggedIn.value = success
            if (success) observeCurrentUser()
        } catch (e: Exception) {
            errorMessage.value = e.message ?: "Login failed"
        }
    }
}


    fun resetPassword(email: String) { repo.resetPassword(email) }

    suspend fun verifyCurrentPassword(currentPassword: String): Boolean {
        return try {
            repo.verifyPassword(currentPassword)
        } catch (e: Exception) {
            false
        }
    }

    //  التحقق من شروط الباسوورد الجديد
    fun validateNewPassword(password: String): ValidationResult {
        return when {
            password.length < 6 -> ValidationResult(
                isValid = false,
                message = "Password must be at least 6 characters long"
            )
            !password.any { it.isDigit() } -> ValidationResult(
                isValid = false,
                message = "Password must contain at least one number"
            )
            !password.any { it.isLetter() } -> ValidationResult(
                isValid = false,
                message = "Password must contain at least one letter"
            )
            !password.any { !it.isLetterOrDigit() } -> ValidationResult(
                isValid = false,
                message = "Password must contain at least one special character"
            )
            else -> ValidationResult(isValid = true, message = "Password is valid")
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val ok = repo.verifyPassword(currentPassword)
                if (!ok) {
                    errorMessage.value = "Wrong current password"
                    return@launch
                }
                repo.updateUserPassword(newPassword)
            } catch (e: Exception) { errorMessage.value = e.message }
        }
    }

    fun updateNameAndPhone(name: String, phone: String) {
        viewModelScope.launch {
            try { repo.updateUserInfo(name, phone) }
            catch (e: Exception) { errorMessage.value = e.message }
        }
    }


    fun updateProfile(
        name: String,
        phone: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // تحديث الاسم و الهاتف
                updateNameAndPhone(name, phone)

                // تحديث الباسورد
                if (!newPassword.isNullOrEmpty() && !currentPassword.isNullOrEmpty()) {
                    changePassword(currentPassword, newPassword)
                }

                // بعد كل التحديثات
                onDone()
            } catch (e: Exception) { errorMessage.value = e.message }
        }
    }
    // ---------- FAVORITES OPERATIONS ----------
    fun addToFavorites(courseId: String) {
        viewModelScope.launch {
            try {
                val currentFavorites = currentUser.value?.favorites ?: emptyList()
                if (!currentFavorites.contains(courseId)) {
                    val updatedFavorites = currentFavorites + courseId
                    repo.updateUserInfo(updates = mapOf("favorites" to updatedFavorites))
                }
                repo.addToFavorites(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to add to favorites"
            }
        }
    }

    fun removeFromFavorites(courseId: String) {
        viewModelScope.launch {
            try {
                val currentFavorites = currentUser.value?.favorites ?: emptyList()
                val updatedFavorites = currentFavorites.filter { it != courseId }
                repo.updateUserInfo(updates = mapOf("favorites" to updatedFavorites))
                repo.removeFromFavorites(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove from favorites"
            }
        }
    }

    // ---------- WATCHLIST OPERATIONS ----------
    fun addToWatchlist(courseId: String) {
        viewModelScope.launch {
            try {
                val currentWatchlist = currentUser.value?.watchlist ?: emptyList()
                if (!currentWatchlist.contains(courseId)) {
                    val updatedWatchlist = currentWatchlist + courseId
                    repo.updateUserInfo(updates = mapOf("watchlist" to updatedWatchlist))
                }
                repo.addToWatchlist(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to add to watchlist"
            }
        }
    }

    fun removeFromWatchlist(courseId: String) {
        viewModelScope.launch {
            try {
                val currentWatchlist = currentUser.value?.watchlist ?: emptyList()
                val updatedWatchlist = currentWatchlist.filter { it != courseId }
                repo.updateUserInfo(updates = mapOf("watchlist" to updatedWatchlist))
                repo.removeFromWatchlist(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove from watchlist"
            }
        }
    }

    // ---------- DONE COURSES OPERATIONS ----------
    fun addToDoneCourses(courseId: String) {
        viewModelScope.launch {
            try {
                val currentDoneCourses = currentUser.value?.doneCourses ?: emptyList()
                if (!currentDoneCourses.contains(courseId)) {
                    val updatedDoneCourses = currentDoneCourses + courseId
                    repo.updateUserInfo(updates = mapOf("doneCourses" to updatedDoneCourses))
                }
                repo.addToDoneCourses(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to mark course as completed"
            }
        }
    }

    fun removeFromDoneCourses(courseId: String) {
        viewModelScope.launch {
            try {
                val currentDoneCourses = currentUser.value?.doneCourses ?: emptyList()
                val updatedDoneCourses = currentDoneCourses.filter { it != courseId }
                repo.updateUserInfo(updates = mapOf("doneCourses" to updatedDoneCourses))
                repo.removeFromDoneCourses(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove from completed courses"
            }
        }
    }

    // مزامنة Done Courses مع Room
    fun syncDoneCoursesWithRoom(roomDoneCourses: List<String>) {
        viewModelScope.launch {
            try {
                repo.syncDoneCoursesWithRoom(roomDoneCourses)
            } catch (e: Exception) {
                errorMessage.value = "Failed to sync completed courses"
            }
        }
    }

    // مزامنة Favorites مع Room
    fun syncFavoritesWithRoom(roomFavorites: List<String>) {
        viewModelScope.launch {
            try {
                repo.syncFavoritesWithRoom(roomFavorites)
            } catch (e: Exception) {
                errorMessage.value = "Failed to sync favorites"
            }
        }
    }

    // مزامنة Watchlist مع Room
    fun syncWatchlistWithRoom(roomWatchlist: List<String>) {
        viewModelScope.launch {
            try {
                repo.syncWatchlistWithRoom(roomWatchlist)
            } catch (e: Exception) {
                errorMessage.value = "Failed to sync watchlist"
            }
        }
    }
    fun logout() {
        repo.logout()
        isLoggedIn.value = false
        currentUser.value = null
    }
}

// data class لنتيجة التحقق من الباسوورد
data class ValidationResult(
    val isValid: Boolean,
    val message: String
)