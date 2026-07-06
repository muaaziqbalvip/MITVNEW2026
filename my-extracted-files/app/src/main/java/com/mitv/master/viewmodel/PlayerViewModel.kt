package com.mitv.master.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mitv.master.data.model.MediaItem
import com.mitv.master.data.model.SourceType
import com.mitv.master.util.PlayerFactory
import com.mitv.master.util.YoutubeResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the fullscreen player, including a playlist context so the
 * Next/Previous controls can move through the same list of items the
 * user was browsing (Live TV row, Movies row, or a series' episode list).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _currentItem = MutableStateFlow<MediaItem?>(null)
    val currentItem: StateFlow<MediaItem?> = _currentItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** The ordered list of items available for Next/Previous navigation. */
    private var playlistContext: List<MediaItem> = emptyList()
    private var currentIndex: Int = -1

    val player: ExoPlayer = PlayerFactory.buildPlayer(context)

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = playbackState == Player.STATE_BUFFERING
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playbackError.value = error.message ?: "Playback failed. Try again or pick another item."
            }
        })
    }

    /**
     * Call once when entering the player with the full list the user was
     * browsing plus which item they tapped.
     */
    fun setPlaylistContext(items: List<MediaItem>, startItem: MediaItem) {
        playlistContext = items
        currentIndex = items.indexOfFirst { it.id == startItem.id }.coerceAtLeast(0)
        playItem(startItem)
    }

    fun playNext() {
        if (playlistContext.isEmpty()) return
        currentIndex = (currentIndex + 1).coerceAtMost(playlistContext.lastIndex)
        playItem(playlistContext[currentIndex])
    }

    fun playPrevious() {
        if (playlistContext.isEmpty()) return
        currentIndex = (currentIndex - 1).coerceAtLeast(0)
        playItem(playlistContext[currentIndex])
    }

    fun hasNext(): Boolean = playlistContext.isNotEmpty() && currentIndex < playlistContext.lastIndex
    fun hasPrevious(): Boolean = playlistContext.isNotEmpty() && currentIndex > 0

    fun togglePlayPause() {
        player.playWhenReady = !player.playWhenReady
    }

    private fun playItem(media: MediaItem) {
        viewModelScope.launch {
            _playbackError.value = null
            _isBuffering.value = true
            _currentItem.value = media

            val resolved = if (media.sourceType == SourceType.YOUTUBE) {
                val direct = YoutubeResolver.resolveDirectUrl(media.streamUrl)
                if (direct != null) {
                    media.copy(streamUrl = direct)
                } else {
                    _playbackError.value = "Couldn't resolve this YouTube video. It may be private, age-restricted, or removed."
                    return@launch
                }
            } else {
                media
            }

            try {
                val mediaSource = PlayerFactory.buildMediaSource(context, resolved)
                player.setMediaSource(mediaSource)
                player.prepare()
                player.playWhenReady = true
            } catch (e: Exception) {
                _playbackError.value = "${e.javaClass.simpleName}: ${e.message ?: "Could not start playback."}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
