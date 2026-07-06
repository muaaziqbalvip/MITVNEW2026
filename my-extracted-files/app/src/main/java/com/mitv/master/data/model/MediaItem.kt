package com.mitv.master.data.model

/**
 * Unified content entry for Live TV, Movies, and Series episodes.
 * Populated entirely by the admin (via the HTML admin panel → Firebase),
 * synced in real time to every user's app. No in-app "add playlist" flow —
 * users only browse and play what the admin has published.
 *
 * `streamUrl` supports:
 *  - Direct HTTP/HTTPS .m3u8 / .mp4 / .ts links
 *  - Masked/wrapped/proxy links, e.g.
 *      https://fusion-rkdyiptv.vercel.app/api/rkdyiptv/playlist.m3u?action=stream&id=481913&token=rkdyiptv&key=rkdy
 *    (these resolve like any normal HTTP(S) URL — ExoPlayer follows the
 *    redirect/response chain, so no special handling is required beyond
 *    allowing cross-protocol redirects, which PlayerFactory already does)
 *  - YouTube watch/share links (resolved to a direct stream via YoutubeResolver
 *    at playback time)
 */
data class MediaItem(
    val id: String = "",
    val title: String = "",
    val streamUrl: String = "",
    val posterUrl: String = "",       // vertical poster for Movies/Series
    val logoUrl: String = "",         // square/round logo for Live TV — supports .svg via Coil SVG decoder
    val backdropUrl: String = "",     // wide banner image for hero/detail sections
    val groupTitle: String = "General",
    val category: ContentCategory = ContentCategory.LIVE,
    val sourceType: SourceType = SourceType.M3U8,
    val isFree: Boolean = true,       // admin toggle: true = visible to everyone, false = Pro-only
    val isFeatured: Boolean = false,  // admin toggle: show in the home hero carousel
    val year: String = "",
    val rating: String = "",
    val description: String = "",
    val language: String = "",        // e.g. "Hindi", "Urdu", "English"
    val epgId: String = "",           // links to /epg/{epgId} for program guide data
    val seasonNumber: Int = 0,        // series episodes only
    val episodeNumber: Int = 0,       // series episodes only
    val seriesId: String = "",        // series episodes only — groups episodes under one show
    val sortOrder: Int = 0,           // admin-controlled display order within a group
    val addedTimestamp: Long = 0L
)

enum class ContentCategory {
    LIVE,
    MOVIE,
    SERIES
}

enum class SourceType {
    M3U8,
    HLS,
    MP4,
    YOUTUBE,
    XTREAM
}

/** A single EPG (program guide) entry for a Live TV channel. */
data class EpgProgram(
    val id: String = "",
    val channelId: String = "",
    val title: String = "",
    val description: String = "",
    val startTimestamp: Long = 0L,
    val endTimestamp: Long = 0L,
    val iconUrl: String = ""          // supports .svg
)
