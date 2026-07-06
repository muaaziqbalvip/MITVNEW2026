package com.mitv.master.data.repository

import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.ContentCategory
import com.mitv.master.data.model.SourceType
import com.mitv.master.data.model.XtreamConfig
import com.mitv.master.data.remote.RetrofitProvider
import com.mitv.master.data.remote.XtreamVodApiService
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches Live TV, Movies (VOD), and Series from an Xtream Codes panel and
 * converts them into the app's unified Channel model. Used both when a
 * regular (free) user manually enters Xtream credentials, and internally
 * for populating the admin's Pro catalog.
 */
@Singleton
class XtreamRepository @Inject constructor() {

    private val client = OkHttpClient()

    /** Live TV channels via the classic player_api.php get_live_streams action. */
    suspend fun fetchLiveChannels(config: XtreamConfig): List<Channel> {
        val service = RetrofitProvider.createXtreamService(config.baseUrlForRetrofit())
        val streams = service.getLiveStreams(config.username, config.password)
        return streams.map { dto ->
            Channel(
                id = UUID.randomUUID().toString(),
                name = dto.name,
                streamUrl = config.buildLiveStreamUrl(dto.streamId.toString()),
                logoUrl = dto.streamIcon.orEmpty(),
                groupTitle = "Live TV",
                sourceType = SourceType.XTREAM,
                category = ContentCategory.LIVE
            )
        }
    }

    /** Movies (VOD) via get_vod_streams. */
    suspend fun fetchMovies(config: XtreamConfig): List<Channel> {
        val vodService = vodService(config)
        val vods = vodService.getVodStreams(config.username, config.password)
        return vods.map { dto ->
            val ext = dto.containerExtension ?: "mp4"
            Channel(
                id = UUID.randomUUID().toString(),
                name = dto.name,
                streamUrl = config.buildVodStreamUrl(dto.streamId.toString(), ext),
                logoUrl = dto.streamIcon.orEmpty(),
                groupTitle = "Movies",
                sourceType = SourceType.XTREAM,
                category = ContentCategory.MOVIE
            )
        }
    }

    /** Series listing via get_series. Episode-level fetching happens on demand (get_series_info). */
    suspend fun fetchSeriesList(config: XtreamConfig): List<Channel> {
        val vodService = vodService(config)
        val seriesList = vodService.getSeries(config.username, config.password)
        return seriesList.map { dto ->
            Channel(
                id = dto.seriesId.toString(), // keep numeric id so we can re-query episodes later
                name = dto.name,
                streamUrl = "", // series itself has no direct stream; episodes do
                logoUrl = dto.cover.orEmpty(),
                groupTitle = "Series",
                sourceType = SourceType.XTREAM,
                category = ContentCategory.SERIES
            )
        }
    }

    /** Fetches episode list + direct stream URLs for a specific series id. */
    suspend fun fetchSeriesEpisodes(config: XtreamConfig, seriesId: Int): List<Channel> {
        val vodService = vodService(config)
        val info = vodService.getSeriesInfo(config.username, config.password, seriesId = seriesId)
        val episodes = info.episodes?.values?.flatten().orEmpty()
        return episodes.map { ep ->
            val ext = ep.containerExtension ?: "mp4"
            Channel(
                id = UUID.randomUUID().toString(),
                name = "S${ep.season} E${ep.episodeNum} — ${ep.title}",
                streamUrl = config.buildSeriesEpisodeUrl(ep.id, ext),
                logoUrl = "",
                groupTitle = "Series",
                sourceType = SourceType.XTREAM,
                category = ContentCategory.SERIES
            )
        }
    }

    /** Validates credentials quickly by attempting a live-streams call. */
    suspend fun validateCredentials(config: XtreamConfig): Boolean {
        return try {
            fetchLiveChannels(config)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun vodService(config: XtreamConfig): XtreamVodApiService {
        val baseUrl = config.baseUrlForRetrofit()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamVodApiService::class.java)
    }
}
