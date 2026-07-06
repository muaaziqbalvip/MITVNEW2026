package com.mitv.master.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response as NPResponse
import org.schabi.newpipe.extractor.downloader.Request as NPRequest

/**
 * Minimal OkHttp-backed Downloader implementation required by NewPipeExtractor.
 * Must be registered once via NewPipe.init(NewPipeOkHttpDownloader.instance)
 * before any extraction call (done in MitvApplication).
 */
class NewPipeOkHttpDownloader private constructor(
    private val client: OkHttpClient
) : Downloader() {

    override fun execute(request: NPRequest): NPResponse {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val builder = Request.Builder().url(url)

        headers.forEach { (key, values) ->
            values.forEach { value -> builder.addHeader(key, value) }
        }

        when (httpMethod) {
            "GET" -> builder.get()
            "POST" -> builder.post((dataToSend ?: ByteArray(0)).toRequestBody())
            else -> builder.method(httpMethod, dataToSend?.toRequestBody())
        }

        client.newCall(builder.build()).execute().use { response ->
            val body = response.body?.string() ?: ""
            val latestUrl = response.request.url.toString()
            return NPResponse(
                response.code,
                response.message,
                response.headers.toMultimap(),
                body,
                latestUrl
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NewPipeOkHttpDownloader? = null

        val instance: NewPipeOkHttpDownloader
            get() = INSTANCE ?: synchronized(this) {
                INSTANCE ?: NewPipeOkHttpDownloader(
                    OkHttpClient.Builder().build()
                ).also { INSTANCE = it }
            }
    }
}
