package com.example.learnify.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnify.data.model.User
import com.example.learnify.data.repository.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repo = UserRepository()
    val isSuccess = mutableStateOf(false)
    val isLoggedIn = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val currentUser = mutableStateOf<User?>(null)

    init {
        val user = repo.getCurrentUser()
        if (user != null) {
            isLoggedIn.value = true
            observeCurrentUser()
        }
    }

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

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                repo.registerUser(name, email, phone, password)
                isSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Registration failed"
                isSuccess.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val success = repo.loginUser(email, password)
                if (success) {
                    observeCurrentUser()
                    isLoggedIn.value = true
                } else {
                    isLoggedIn.value = false
                }
            } catch (e: Exception) {
                errorMessage.value = when (e) {
                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthInvalidUserException ->
                        "Incorrect email or password!"
                    is FirebaseNetworkException ->
                        "Please check your internet connection"
                    else ->
                        e.message ?: "Login failed!"
                }
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.resetPassword(email)
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = when (e) {
                    is FirebaseAuthInvalidUserException ->
                        "This email is not registered"
                    is FirebaseNetworkException ->
                        "Please check your internet connection"
                    else ->
                        e.message ?: "Failed to send reset email"
                }
            }
        }
    }

    suspend fun verifyCurrentPassword(currentPassword: String): Boolean {
        return try {
            repo.verifyPassword(currentPassword)
        } catch (e: Exception) {
            false
        }
    }

    fun validateNewPassword(password: String): ValidationResult {
        return when {
            password.length < 6 ->
                ValidationResult(false, "Password must be at least 6 characters long")
            !password.any { it.isDigit() } ->
                ValidationResult(false, "Password must contain at least one number")
            !password.any { it.isLetter() } ->
                ValidationResult(false, "Password must contain at least one letter")
            !password.any { !it.isLetterOrDigit() } ->
                ValidationResult(false, "Password must contain at least one special character")
            else ->
                ValidationResult(true, "Password is valid")
        }
    }

    suspend fun updateNameAndPhone(name: String, phone: String) {
        repo.updateUserInfo(name, phone)
    }

    suspend fun changePasswordInternal(
        currentPassword: String,
        newPassword: String
    ) {
        val ok = repo.verifyPassword(currentPassword)
        if (!ok) throw Exception("Wrong current password")
        repo.updateUserPassword(newPassword)
    }

    fun updateProfile(
        name: String,
        phone: String,
        currentPassword: String? = null,
        newPassword: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                updateNameAndPhone(name, phone)

                if (!newPassword.isNullOrEmpty() && !currentPassword.isNullOrEmpty()) {
                    changePasswordInternal(currentPassword, newPassword)
                }

                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun addToFavorites(courseId: String) {
        viewModelScope.launch {
            try {
                repo.addToFavorites(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to add to favorites"
            }
        }
    }

    fun removeFromFavorites(courseId: String) {
        viewModelScope.launch {
            try {
                repo.removeFromFavorites(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove from favorites"
            }
        }
    }

    fun addToWatchlist(courseId: String) {
        viewModelScope.launch {
            try {
                repo.addToWatchlist(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to add to watchlist"
            }
        }
    }

    fun removeFromWatchlist(courseId: String) {
        viewModelScope.launch {
            try {
                repo.removeFromWatchlist(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove from watchlist"
            }
        }
    }

    fun addToDoneCourses(courseId: String) {
        viewModelScope.launch {
            try {
                repo.addToDoneCourses(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to mark course as completed"
            }
        }
    }

    fun removeFromDoneCourses(courseId: String) {
        viewModelScope.launch {
            try {
                repo.removeFromDoneCourses(courseId)
            } catch (e: Exception) {
                errorMessage.value = "Failed to remove from completed courses"
            }
        }
    }

    fun syncDoneCoursesWithRoom(roomDoneCourses: List<String>) {
        viewModelScope.launch {
            try {
                repo.syncDoneCoursesWithRoom(roomDoneCourses)
            } catch (e: Exception) {
                errorMessage.value = "Failed to sync completed courses"
            }
        }
    }

    fun syncFavoritesWithRoom(roomFavorites: List<String>) {
        viewModelScope.launch {
            try {
                repo.syncFavoritesWithRoom(roomFavorites)
            } catch (e: Exception) {
                errorMessage.value = "Failed to sync favorites"
            }
        }
    }

    fun syncWatchlistWithRoom(roomWatchlist: List<String>) {
        viewModelScope.launch {
            try {
                repo.syncWatchlistWithRoom(roomWatchlist)
            } catch (e: Exception) {
                errorMessage.value = "Failed to sync watchlist"
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            isLoggedIn.value = false
            currentUser.value = null
            isSuccess.value = false
            onComplete()
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}
