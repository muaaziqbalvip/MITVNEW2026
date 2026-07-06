package com.mitv.master.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.mitv.master.data.model.Channel
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

/**
 * Fullscreen Netflix-style player: tap anywhere to reveal an overlay with
 * back, previous/next (moves through the playlist the user was browsing),
 * play/pause, and the current channel name. Overlay auto-hides after a
 * few seconds of inactivity, like Netflix/YouTube.
 */
@Composable
fun PlayerScreen(
    channel: Channel,
    playlistContext: List<Channel> = listOf(channel),
    onBack: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val isBuffering by viewModel.isBuffering.collectAsState()
    val playbackError by viewModel.playbackError.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(channel.id) {
        viewModel.setPlaylistContext(playlistContext, channel)
    }

    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                controlsVisible = !controlsVisible
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player
                    useController = false // custom overlay below replaces default controls
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            CircularProgressIndicator(
                color = MitvRed,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerOverlay(
                channelName = currentChannel?.name ?: channel.name,
                isPlaying = isPlaying,
                hasNext = viewModel.hasNext(),
                hasPrevious = viewModel.hasPrevious(),
                onBack = onBack,
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() }
            )
        }

        playbackError?.let {
            Text(
                text = "Playback error: $it",
                color = MitvRed,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun PlayerOverlay(
    channelName: String,
    isPlaying: Boolean,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        // Top bar: back button + channel name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = channelName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Center controls: previous / play-pause / next
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = hasPrevious,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = if (hasPrevious) Color.White else Color(0xFF555555),
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .size(64.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MitvRed,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(
                onClick = onNext,
                enabled = hasNext,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = if (hasNext) Color.White else Color(0xFF555555),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
