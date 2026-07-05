package com.mitv.master.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.mitv.master.data.repository.UserTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userTrackingRepository: UserTrackingRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    onLoginSuccess(user.uid, user.email.orEmpty())
                } else {
                    _errorMessage.value = "Sign-in returned no user. Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Google sign-in failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter both email and password."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                val user = result.user
                if (user != null) {
                    onLoginSuccess(user.uid, user.email.orEmpty())
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _errorMessage.value = "No account found for this email. Try creating one instead."
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _errorMessage.value = "Incorrect email or password."
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign-in failed."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter both email and password."
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val user = result.user
                if (user != null) {
                    onLoginSuccess(user.uid, user.email.orEmpty())
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _errorMessage.value = "An account already exists for this email. Try signing in instead."
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _errorMessage.value = "Please enter a valid email address."
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Account creation failed."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun onLoginSuccess(uid: String, email: String) {
        _isLoggedIn.value = true
        userTrackingRepository.startSession(
            context = context,
            uid = uid,
            email = email
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun signOut() {
        auth.signOut()
        _isLoggedIn.value = false
    }
}