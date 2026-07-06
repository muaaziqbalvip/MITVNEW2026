package com.mitv.master.util

import android.content.Context
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.mitv.master.data.model.MediaItem
import com.mitv.master.data.model.SourceType

/**
 * Builds an ExoPlayer instance tuned for IPTV playback on slow/unstable networks:
 * - Small min buffer so playback starts fast
 * - Larger max buffer to absorb network jitter once playing
 * - Aggressive rebuffer-after-stall thresholds
 *
 * `setAllowCrossProtocolRedirects(true)` is what makes masked/proxy stream
 * URLs work (e.g. a Vercel API route that redirects to the real .m3u8) —
 * without it, any redirect that changes http<->https would fail to load.
 */
object PlayerFactory {

    fun buildPlayer(context: Context): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 5_000,
                /* maxBufferMs = */ 30_000,
                /* bufferForPlaybackMs = */ 1_000,
                /* bufferForPlaybackAfterRebufferMs = */ 2_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
    }

    fun buildMediaSource(context: Context, media: MediaItem): MediaSource {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
            .setUserAgent("MITV/1.0 (Android)")

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val exoMediaItem = ExoMediaItem.fromUri(media.streamUrl)

        return when (media.sourceType) {
            SourceType.M3U8, SourceType.HLS, SourceType.XTREAM -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem)
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem)
            }
        }
    }
}
