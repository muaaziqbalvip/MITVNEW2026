package com.mitv.master.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo

/**
 * Resolves a YouTube watch/video URL into a direct playable stream URL
 * so it can be fed into ExoPlayer alongside regular M3U/Xtream channels.
 *
 * Uses NewPipeExtractor — no bundled Python/FFmpeg binaries needed.
 * Must call NewPipe.init(NewPipeOkHttpDownloader.instance) once at app
 * startup (done in MitvApplication) before calling resolveDirectUrl.
 */
object YoutubeResolver {

    suspend fun resolveDirectUrl(youtubeUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, youtubeUrl)

            // Prefer a progressive (audio+video combined) stream so it plays
            // directly in ExoPlayer without needing separate audio/video merge.
            val videoStream = streamInfo.videoStreams
                ?.filter { !it.isVideoOnly }
                ?.maxByOrNull { it.getResolution()?.replace("p", "")?.toIntOrNull() ?: 0 }

            videoStream?.url
                ?: streamInfo.videoOnlyStreams?.firstOrNull()?.url
                ?: streamInfo.hlsUrl
                ?: streamInfo.dashMpdUrl
        } catch (e: Exception) {
            null
        }
    }
}
