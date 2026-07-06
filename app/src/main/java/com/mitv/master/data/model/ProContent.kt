package com.mitv.master.data.model

/**
 * Represents the user's subscription tier and their curated MITV Pro access.
 * Written by admin (Muaaz) directly in Firebase Console at:
 *   /users/{uid}/isPro
 * When true, the Pro sections (curated 4000+ channels/movies/series) unlock
 * for that user automatically via a real-time listener — no payment gateway,
 * fully manual/admin-controlled.
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val isPro: Boolean = false,
    val proActivatedAt: Long = 0L,
    val displayName: String = ""
)

/** Content type for Pro catalog items. */
enum class ProContentType {
    LIVE_CHANNEL,
    MOVIE,
    SERIES
}

/**
 * A single item in the curated MITV Pro catalog.
 * Stored under /pro_content/{live_channels|movies|series}/{id}
 * Only the admin edits this — regular users only read it once isPro = true.
 */
data class ProMediaItem(
    val id: String = "",
    val title: String = "",
    val posterUrl: String = "",
    val streamUrl: String = "",
    val groupTitle: String = "General",
    val type: ProContentType = ProContentType.LIVE_CHANNEL,
    val year: String = "",
    val rating: String = "",
    val description: String = "",
    val sourceType: SourceType = SourceType.M3U8
)

/** A season grouping for Pro series content. */
data class ProSeriesSeason(
    val seasonNumber: Int = 0,
    val episodes: List<ProSeriesEpisode> = emptyList()
)

data class ProSeriesEpisode(
    val id: String = "",
    val title: String = "",
    val episodeNumber: Int = 0,
    val streamUrl: String = "",
    val posterUrl: String = ""
)
