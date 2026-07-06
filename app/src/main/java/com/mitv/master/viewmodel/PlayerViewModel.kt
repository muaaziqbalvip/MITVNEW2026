package com.mitv.master.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mitv.master.data.model.Channel
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
 * Next/Previous controls can move through the same list of channels the
 * user was browsing (Live TV row, Movies row, or Series episode list).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** The ordered list of channels available for Next/Previous navigation. */
    private var playlistContext: List<Channel> = emptyList()
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
                _playbackError.value = error.message
            }
        })
    }

    /**
     * Call once when entering the player with the full list the user was
     * browsing plus which item they tapped.
     */
    fun setPlaylistContext(channels: List<Channel>, startChannel: Channel) {
        playlistContext = channels
        currentIndex = channels.indexOfFirst { it.id == startChannel.id }.coerceAtLeast(0)
        playChannel(startChannel)
    }

    fun playNext() {
        if (playlistContext.isEmpty()) return
        currentIndex = (currentIndex + 1).coerceAtMost(playlistContext.lastIndex)
        playChannel(playlistContext[currentIndex])
    }

    fun playPrevious() {
        if (playlistContext.isEmpty()) return
        currentIndex = (currentIndex - 1).coerceAtLeast(0)
        playChannel(playlistContext[currentIndex])
    }

    fun hasNext(): Boolean = playlistContext.isNotEmpty() && currentIndex < playlistContext.lastIndex
    fun hasPrevious(): Boolean = playlistContext.isNotEmpty() && currentIndex > 0

    fun togglePlayPause() {
        player.playWhenReady = !player.playWhenReady
    }

    private fun playChannel(channel: Channel) {
        viewModelScope.launch {
            _playbackError.value = null
            _currentChannel.value = channel

            val resolvedChannel = if (channel.sourceType == SourceType.YOUTUBE) {
                val direct = YoutubeResolver.resolveDirectUrl(channel.streamUrl)
                if (direct != null) channel.copy(streamUrl = direct) else channel
            } else {
                channel
            }

            val mediaSource = PlayerFactory.buildMediaSource(context, resolvedChannel)
            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
        }
    }

    fun selectAudioTrack(trackIndex: Int) {
        // Track group selection handled via player.trackSelectionParameters
        // Left as an extension point for multi-audio IPTV streams.
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
