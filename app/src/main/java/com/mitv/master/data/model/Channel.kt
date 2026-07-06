package com.mitv.master.data.model

/**
 * Represents a single playable channel/stream entry.
 * Supports M3U/M3U8, Xtream Codes, and YouTube-sourced entries.
 */
data class Channel(
    val id: String = "",
    val name: String = "",
    val streamUrl: String = "",
    val logoUrl: String = "",       // supports .png/.jpg/.svg
    val groupTitle: String = "General",
    val tvgId: String = "",
    val tvgLanguage: String = "",
    val sourceType: SourceType = SourceType.M3U,
    val category: ContentCategory = ContentCategory.LIVE,
    val isFavorite: Boolean = false,
    val isFree: Boolean = false,   // admin-controlled: true = free for all users, false = Pro-only
    val isAlive: Boolean = true,   // set by the Hugging Face link-checker (once/day)
    val audioTracks: List<String> = emptyList(),
    val lastWatchedTimestamp: Long = 0L
)

/** Broad content bucket, used to split a playlist into Live TV / Movies / Series tabs. */
enum class ContentCategory {
    LIVE,
    MOVIE,
    SERIES
}

enum class SourceType {
    M3U,
    M3U8,
    XTREAM,
    YOUTUBE
}
