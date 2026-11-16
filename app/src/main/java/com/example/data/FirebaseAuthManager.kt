package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirebaseAuthManager"

class FirebaseAuthManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    /**
     * Interactive Google Sign-In using Credential Manager with GetSignInWithGoogleOption.
     * Requires an Activity/UI Context so Credential Manager can display the account chooser sheet.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        _isSigningIn.value = true
        _authError.value = null

        return try {
            val serverClientId = appContext.getString(R.string.default_web_client_id)
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val uiCredentialManager = CredentialManager.create(activityContext)
            val result = uiCredentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: error("Firebase sign-in returned null user")
                _isSigningIn.value = false
                Result.success(user)
            } else {
                _isSigningIn.value = false
                val errorMsg = "Unexpected credential type: ${credential.type}"
                Log.e(TAG, errorMsg)
                _authError.value = errorMsg
                Result.failure(IllegalStateException(errorMsg))
            }
        } catch (e: GetCredentialCancellationException) {
            _isSigningIn.value = false
            Log.w(TAG, "Interactive Google sign-in was cancelled by user: ${e.message}")
            Result.failure(e)
        } catch (e: NoCredentialException) {
            _isSigningIn.value = false
            val msg = "No Google account available on device for Credential Manager sign-in."
            Log.w(TAG, "$msg ${e.message}")
            _authError.value = msg
            Result.failure(e)
        } catch (e: GetCredentialException) {
            _isSigningIn.value = false
            Log.e(TAG, "CredentialManager error: ${e.message}", e)
            _authError.value = e.localizedMessage ?: "Sign-in error"
            Result.failure(e)
        } catch (e: Exception) {
            _isSigningIn.value = false
            Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
            _authError.value = e.localizedMessage ?: "Authentication failure"
            Result.failure(e)
        }
    }

    suspend fun signOut(activityContext: Context = appContext) {
        try {
            CredentialManager.create(activityContext).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing credential state: ${e.message}")
        }
        auth.signOut()
        _currentUser.value = null
    }

    companion object {
        @Volatile
        private var instance: FirebaseAuthManager? = null

        fun getInstance(context: Context): FirebaseAuthManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseAuthManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
