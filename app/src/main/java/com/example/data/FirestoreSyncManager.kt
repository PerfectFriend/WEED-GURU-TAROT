package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreSyncManager"

class FirestoreSyncManager(private val context: Context) {

    // Resolves mandatory custom named database ID from res/values/firebase_applet_config.xml
    private val firestore: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: error("User must be authenticated to perform cloud Firestore operations")
    }

    /**
     * Uploads or syncs local user profile to cloud Firestore document `/users/{userId}`.
     */
    suspend fun syncProfileToCloud(profile: UserProfile): Result<Unit> {
        return try {
            val uid = requireUserId()
            val data = hashMapOf(
                "userId" to uid,
                "username" to profile.username,
                "weed" to profile.weed,
                "avatarIndex" to profile.avatarIndex,
                "systemLanguage" to profile.systemLanguage,
                "lastSyncTimestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .await()

            Log.d(TAG, "User profile successfully synced to Firestore for user: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user profile to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Saves a tarot divination reading to `/users/{userId}/readings/{readingId}`.
     */
    suspend fun saveReadingToCloud(
        question: String,
        cardIndices: List<Int>,
        isUpright: List<Boolean>,
        report: String
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val readingRef = firestore.collection("users")
                .document(uid)
                .collection("readings")
                .document()

            val data = hashMapOf(
                "id" to readingRef.id,
                "userId" to uid,
                "question" to question,
                "cardIndices" to cardIndices,
                "isUpright" to isUpright,
                "report" to report,
                "timestamp" to System.currentTimeMillis()
            )

            readingRef.set(data).await()
            Log.d(TAG, "Tarot reading saved to cloud with ID: ${readingRef.id}")
            Result.success(readingRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save reading to cloud: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow of cloud readings for the authenticated user.
     */
    fun observeCloudReadings(): Flow<List<Map<String, Any>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection("users")
                .document(uid)
                .collection("readings")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to cloud readings: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { it.data }
                        trySend(items)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception attaching snapshot listener: ${e.message}", e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    companion object {
        @Volatile
        private var instance: FirestoreSyncManager? = null

        fun getInstance(context: Context): FirestoreSyncManager {
            return instance ?: synchronized(this) {
                instance ?: FirestoreSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
