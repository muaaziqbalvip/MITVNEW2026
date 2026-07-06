package com.mitv.master.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Extends XtreamApiService coverage with VOD (movies) and Series endpoints,
 * used by both free (user-entered Xtream) and Pro (admin-curated) flows.
 */
interface XtreamVodApiService {

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<XtreamVodDto>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategoryDto>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): List<XtreamSeriesDto>

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): XtreamSeriesInfoDto
}

data class XtreamVodDto(
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("rating") val rating: String?
)

data class XtreamSeriesDto(
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("rating") val rating: String?
)

data class XtreamSeriesInfoDto(
    @SerializedName("episodes") val episodes: Map<String, List<XtreamEpisodeDto>>?
)

data class XtreamEpisodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("episode_num") val episodeNum: Int,
    @SerializedName("season") val season: Int,
    @SerializedName("container_extension") val containerExtension: String?
)
