package com.mitv.master.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mitv.master.data.model.Playlist
import com.mitv.master.data.model.SourceType
import com.mitv.master.data.model.XtreamConfig
import com.mitv.master.data.parser.M3uParser
import com.mitv.master.data.repository.PlaylistRepository
import com.mitv.master.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import javax.inject.Inject

sealed class AddPlaylistResult {
    data object Idle : AddPlaylistResult()
    data object Loading : AddPlaylistResult()
    data class Success(val channelCount: Int) : AddPlaylistResult()
    data class Error(val message: String) : AddPlaylistResult()
}

@HiltViewModel
class AddPlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val xtreamRepository: XtreamRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val httpClient = OkHttpClient()

    private val _result = MutableStateFlow<AddPlaylistResult>(AddPlaylistResult.Idle)
    val result: StateFlow<AddPlaylistResult> = _result.asStateFlow()

    /** Tab 1: paste an M3U/M3U8 URL. */
    fun addFromM3uUrl(name: String, url: String) {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank() || url.isBlank()) {
            _result.value = AddPlaylistResult.Error("Please enter both a name and a URL.")
            return
        }
        viewModelScope.launch {
            _result.value = AddPlaylistResult.Loading
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty()
                val channels = M3uParser.parse(body)

                if (channels.isEmpty()) {
                    _result.value = AddPlaylistResult.Error("No channels found. Check the URL and try again.")
                    return@launch
                }

                val playlist = Playlist(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    sourceUrl = url,
                    type = SourceType.M3U8
                )
                playlistRepository.savePlaylist(uid, playlist, channels)
                _result.value = AddPlaylistResult.Success(channels.size)
            } catch (e: Exception) {
                _result.value = AddPlaylistResult.Error(e.message ?: "Failed to load playlist.")
            }
        }
    }

    /** Tab 2: raw M3U text already read from an uploaded file. */
    fun addFromUploadedContent(name: String, rawContent: String) {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank() || rawContent.isBlank()) {
            _result.value = AddPlaylistResult.Error("Please enter a name and select a file.")
            return
        }
        viewModelScope.launch {
            _result.value = AddPlaylistResult.Loading
            try {
                val channels = M3uParser.parse(rawContent)
                if (channels.isEmpty()) {
                    _result.value = AddPlaylistResult.Error("No channels found in this file.")
                    return@launch
                }
                val playlist = Playlist(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    sourceUrl = "local-upload",
                    type = SourceType.M3U
                )
                playlistRepository.savePlaylist(uid, playlist, channels)
                _result.value = AddPlaylistResult.Success(channels.size)
            } catch (e: Exception) {
                _result.value = AddPlaylistResult.Error(e.message ?: "Failed to read file.")
            }
        }
    }

    /** Tab 3: Xtream Codes login — fetches Live + Movies + Series in one go. */
    fun addFromXtream(name: String, server: String, username: String, password: String) {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank() || server.isBlank() || username.isBlank() || password.isBlank()) {
            _result.value = AddPlaylistResult.Error("Please fill in all Xtream Codes fields.")
            return
        }
        viewModelScope.launch {
            _result.value = AddPlaylistResult.Loading
            try {
                val normalizedServer = if (server.startsWith("http")) server else "http://$server"
                val config = XtreamConfig(
                    serverUrl = normalizedServer.substringBeforeLast(":").ifBlank { normalizedServer },
                    username = username,
                    password = password,
                    port = normalizedServer.substringAfterLast(":", "80")
                )

                val live = runCatching { xtreamRepository.fetchLiveChannels(config) }.getOrDefault(emptyList())
                val movies = runCatching { xtreamRepository.fetchMovies(config) }.getOrDefault(emptyList())
                val series = runCatching { xtreamRepository.fetchSeriesList(config) }.getOrDefault(emptyList())

                val allChannels = live + movies + series
                if (allChannels.isEmpty()) {
                    _result.value = AddPlaylistResult.Error("Could not connect. Check your Xtream Codes details.")
                    return@launch
                }

                val playlist = Playlist(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    sourceUrl = config.buildPlayerApiUrl(),
                    type = SourceType.XTREAM
                )
                playlistRepository.savePlaylist(uid, playlist, allChannels)
                _result.value = AddPlaylistResult.Success(allChannels.size)
            } catch (e: Exception) {
                _result.value = AddPlaylistResult.Error(e.message ?: "Xtream connection failed.")
            }
        }
    }

    fun resetResult() {
        _result.value = AddPlaylistResult.Idle
    }
}
