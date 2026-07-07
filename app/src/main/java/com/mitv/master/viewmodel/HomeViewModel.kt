package com.mitv.master.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mitv.master.data.model.MediaItem
import com.mitv.master.data.model.UserProfile
import com.mitv.master.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the Home screen: live channels, movies, and series come straight
 * from the admin-managed Firebase catalog — there is no user "add playlist"
 * step anymore. Pro-only items are still shown (so users know they exist)
 * but are visually locked and blocked from playback unless isPro is true.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val uid: String? get() = auth.currentUser?.uid

    // TEMPORARY DEBUG AID: surfaces the real Firebase exception message on
    // screen instead of silently swallowing it into an empty list. This is
    // the fastest way to see the exact error (permission-denied, network,
    // etc.) without adb/logcat access. Safe to remove once the root cause
    // of the empty catalog is confirmed and fixed.
    private val _debugError = MutableStateFlow<String?>(null)
    val debugError: StateFlow<String?> = _debugError.asStateFlow()

    val userProfile: StateFlow<UserProfile> =
        (uid?.let { mediaRepository.observeUserProfile(it) } ?: kotlinx.coroutines.flow.flowOf(UserProfile()))
            .catch { emit(UserProfile()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val liveChannels: StateFlow<List<MediaItem>> =
        mediaRepository.observeLiveChannels()
            .catch { e -> _debugError.value = "LIVE: ${e.javaClass.simpleName}: ${e.message}"; emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movies: StateFlow<List<MediaItem>> =
        mediaRepository.observeMovies()
            .catch { e -> _debugError.value = "MOVIES: ${e.javaClass.simpleName}: ${e.message}"; emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seriesShows: StateFlow<List<MediaItem>> =
        mediaRepository.observeAllSeries()
            .catch { e -> _debugError.value = "SERIES: ${e.javaClass.simpleName}: ${e.message}"; emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Non-null when the user's Pro subscription has just expired and hasn't been acknowledged yet. */
    private val _expiryNotice = MutableStateFlow<String?>(null)
    val expiryNotice: StateFlow<String?> = _expiryNotice.asStateFlow()

    fun dismissExpiryNotice() {
        _expiryNotice.value = null
        uid?.let { id ->
            viewModelScope.launch { mediaRepository.markExpiryNotified(id) }
        }
    }

    init {
        uid?.let { id ->
            viewModelScope.launch {
                mediaRepository.ensureUserProfileExists(id, auth.currentUser?.email.orEmpty())
            }
            viewModelScope.launch {
                userProfile.collect { profile ->
                    val now = System.currentTimeMillis()
                    val hasExpired = profile.proExpiresAt in 1 until now
                    if (hasExpired && !profile.proExpiryNotified) {
                        _expiryNotice.value = "Your MITV Pro subscription has expired. Renew to keep enjoying Pro channels, movies, and series."
                    }
                }
            }
        }
    }
}
