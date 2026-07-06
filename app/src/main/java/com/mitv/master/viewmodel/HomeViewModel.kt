package com.mitv.master.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.Playlist
import com.mitv.master.data.model.ProMediaItem
import com.mitv.master.data.model.UserProfile
import com.mitv.master.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

    private val _selectedPlaylistChannels = MutableStateFlow<List<Channel>>(emptyList())
    val selectedPlaylistChannels: StateFlow<List<Channel>> = _selectedPlaylistChannels.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _proLiveChannels = MutableStateFlow<List<ProMediaItem>>(emptyList())
    val proLiveChannels: StateFlow<List<ProMediaItem>> = _proLiveChannels.asStateFlow()

    private val _proMovies = MutableStateFlow<List<ProMediaItem>>(emptyList())
    val proMovies: StateFlow<List<ProMediaItem>> = _proMovies.asStateFlow()

    private val _proSeries = MutableStateFlow<List<ProMediaItem>>(emptyList())
    val proSeries: StateFlow<List<ProMediaItem>> = _proSeries.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                playlistRepository.ensureUserProfileExists(uid, auth.currentUser?.email.orEmpty())
            }
            observeProfile(uid)
            observePlaylists(uid)
            observeProCatalog()
        }
    }

    private fun observeProfile(uid: String) {
        viewModelScope.launch {
            playlistRepository.observeUserProfile(uid).collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    private fun observePlaylists(uid: String) {
        viewModelScope.launch {
            playlistRepository.observeUserPlaylists(uid).collect { playlists ->
                _userPlaylists.value = playlists
            }
        }
    }

    private fun observeProCatalog() {
        viewModelScope.launch {
            playlistRepository.observeProLiveChannels().collect { _proLiveChannels.value = it }
        }
        viewModelScope.launch {
            playlistRepository.observeProMovies().collect { _proMovies.value = it }
        }
        viewModelScope.launch {
            playlistRepository.observeProSeries().collect { _proSeries.value = it }
        }
    }

    fun loadPlaylistChannels(playlistId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            playlistRepository.observePlaylistChannels(uid, playlistId).collect { channels ->
                _selectedPlaylistChannels.value = channels
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            playlistRepository.deletePlaylist(uid, playlistId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
