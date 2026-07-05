package com.mitv.master.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

object YoutubeResolver {

    private val client = OkHttpClient()
    private val videoIdPattern = Pattern.compile(
        "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([a-zA-Z0-9_-]{11})"
    )

    suspend fun resolveDirectUrl(youtubeUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val videoId = extractVideoId(youtubeUrl) ?: return@withContext null
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"

            val request = Request.Builder()
                .url(watchUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"
                )
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null

            extractStreamUrlFromPlayerResponse(html)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractVideoId(url: String): String? {
        val matcher = videoIdPattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractStreamUrlFromPlayerResponse(html: String): String? {
        val urlPattern = Pattern.compile("\"url\":\"(https:\\\\/\\\\/[^\"]*googlevideo\\.com[^\"]*)\"")
        val matcher = urlPattern.matcher(html)
        return if (matcher.find()) {
            matcher.group(1)?.replace("\\/", "/")
        } else {
            null
        }
    }
}