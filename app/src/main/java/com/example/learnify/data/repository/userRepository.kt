package com.example.learnify.data.repository

import com.example.learnify.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    // --------------------------- AUTH ---------------------------
    suspend fun registerUser(name: String, email: String, phone: String, password: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return false

            val user = User(uid, name, email, phone, "")
            users.document(uid).set(user).await()

            true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            throw e
        }
    }

    fun resetPassword(email: String) = auth.sendPasswordResetEmail(email)
    fun getCurrentUser() = auth.currentUser
    fun logout() = auth.signOut()

    // --------------------------- USER DATA ---------------------------
    suspend fun getUserData(userId: String): Map<String, Any>? {
        val snap = users.document(userId).get().await()
        return snap.data
    }

    suspend fun updateUserInfo(name: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update(mapOf("name" to name, "phone" to phone)).await()
    }


    suspend fun updateUserInfo(updates: Map<String, Any>) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update(updates).await()
    }

    suspend fun updateUserPassword(newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)?.await()
    }

    suspend fun verifyPassword(currentPassword: String): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email ?: return false
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        return try {
            user.reauthenticate(credential).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    // --------------------------- FAVORITES & WATCHLIST & DONE COURSES ---------------------------

    suspend fun addToFavorites(courseId: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("favorites", FieldValue.arrayUnion(courseId)).await()
    }

    suspend fun removeFromFavorites(courseId: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("favorites", FieldValue.arrayRemove(courseId)).await()
    }

    suspend fun addToWatchlist(courseId: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("watchlist", FieldValue.arrayUnion(courseId)).await()
    }


    suspend fun removeFromWatchlist(courseId: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("watchlist", FieldValue.arrayRemove(courseId)).await()
    }
    suspend fun addToDoneCourses(courseId: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("doneCourses", FieldValue.arrayUnion(courseId)).await()
    }

    suspend fun removeFromDoneCourses(courseId: String) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("doneCourses", FieldValue.arrayRemove(courseId)).await()
    }

    suspend fun getCurrentUserFavorites(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = users.document(uid).get().await()
        return snap.get("favorites") as? List<String> ?: emptyList()
    }


    suspend fun getCurrentUserWatchlist(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = users.document(uid).get().await()
        return snap.get("watchlist") as? List<String> ?: emptyList()
    }

    suspend fun getCurrentUserDoneCourses(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snap = users.document(uid).get().await()
        return snap.get("doneCourses") as? List<String> ?: emptyList()
    }


     // مزامنة المفضلة بين Firebase و Room

    suspend fun syncFavoritesWithRoom(roomFavorites: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("favorites", roomFavorites).await()
    }


     //  مزامنة قائمة المشاهدة بين Firebase و Room

    suspend fun syncWatchlistWithRoom(roomWatchlist: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("watchlist", roomWatchlist).await()
    }

     // مزامنة الكورسات المكتملة بين Firebase و Room

    suspend fun syncDoneCoursesWithRoom(roomDoneCourses: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        users.document(uid).update("doneCourses", roomDoneCourses).await()
    }

    suspend fun addToWatchlist(userId: String, courseId: String) =
        users.document(userId).update("watchlist", FieldValue.arrayUnion(courseId)).await()

    suspend fun addToFavorites(userId: String, courseId: String) =
        users.document(userId).update("favorites", FieldValue.arrayUnion(courseId)).await()

    suspend fun removeFromWatchlist(userId: String, courseId: String) =
        users.document(userId).update("watchlist", FieldValue.arrayRemove(courseId)).await()

    suspend fun removeFromFavorites(userId: String, courseId: String) =
        users.document(userId).update("favorites", FieldValue.arrayRemove(courseId)).await()

    // --------------------------- REALTIME LISTENER ---------------------------
    fun listenToUserRealtime(userId: String, onChange: (Map<String, Any>?) -> Unit) {
        users.document(userId).addSnapshotListener { snap, error ->
            if (error != null) onChange(null)
            else onChange(snap?.data)
        }
    }
}