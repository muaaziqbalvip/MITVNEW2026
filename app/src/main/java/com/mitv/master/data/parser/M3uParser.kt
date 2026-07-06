package com.mitv.master.data.parser

import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.ContentCategory
import com.mitv.master.data.model.SourceType
import java.util.UUID

/**
 * Parses standard M3U / M3U8 playlist text into a list of Channel objects.
 *
 * Supports EXTINF attributes:
 *   tvg-id, tvg-name, tvg-logo, tvg-language, group-title
 *
 * Example line:
 * #EXTINF:-1 tvg-id="channel1" tvg-logo="http://.../logo.png" group-title="News",Channel Name
 * http://server/stream.m3u8
 */
object M3uParser {

    private val extInfRegex = Regex("""#EXTINF:-?\d+\s*(.*)?,(.*)""")
    private val attrRegex = Regex("""(\S+)="([^"]*)"""")

    fun parse(rawContent: String): List<Channel> {
        val lines = rawContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val channels = mutableListOf<Channel>()

        var pendingName: String? = null
        var pendingLogo = ""
        var pendingGroup = "General"
        var pendingTvgId = ""
        var pendingLanguage = ""

        for (line in lines) {
            when {
                line.startsWith("#EXTM3U") -> continue

                line.startsWith("#EXTINF") -> {
                    val match = extInfRegex.find(line)
                    if (match != null) {
                        val attrsBlock = match.groupValues.getOrNull(1) ?: ""
                        pendingName = match.groupValues.getOrNull(2)?.trim().orEmpty()

                        val attrs = attrRegex.findAll(attrsBlock).associate {
                            it.groupValues[1] to it.groupValues[2]
                        }
                        pendingLogo = attrs["tvg-logo"] ?: ""
                        pendingGroup = attrs["group-title"] ?: "General"
                        pendingTvgId = attrs["tvg-id"] ?: ""
                        pendingLanguage = attrs["tvg-language"] ?: ""
                    }
                }

                line.startsWith("#") -> {
                    // Ignore other directives (#EXTGRP, #EXTVLCOPT, etc.)
                    continue
                }

                else -> {
                    // This line is the actual stream URL
                    val url = line
                    val type = detectSourceType(url)
                    channels.add(
                        Channel(
                            id = UUID.randomUUID().toString(),
                            name = pendingName?.ifBlank { "Unnamed Channel" } ?: "Unnamed Channel",
                            streamUrl = url,
                            logoUrl = pendingLogo,
                            groupTitle = pendingGroup,
                            tvgId = pendingTvgId,
                            tvgLanguage = pendingLanguage,
                            sourceType = type,
                            category = detectCategory(pendingGroup, pendingName.orEmpty())
                        )
                    )
                    // reset state for next entry
                    pendingName = null
                    pendingLogo = ""
                    pendingGroup = "General"
                    pendingTvgId = ""
                    pendingLanguage = ""
                }
            }
        }
        return channels
    }

    private fun detectSourceType(url: String): SourceType = when {
        url.contains("youtube.com") || url.contains("youtu.be") -> SourceType.YOUTUBE
        url.endsWith(".m3u8") -> SourceType.M3U8
        else -> SourceType.M3U
    }

    /**
     * Best-effort classification of a channel into Live/Movie/Series based on
     * common group-title conventions used by IPTV playlist providers
     * (e.g. "VOD | Action", "Series | Drama", "Movies - 2024").
     */
    private fun detectCategory(groupTitle: String, name: String): ContentCategory {
        val haystack = "$groupTitle $name".lowercase()
        return when {
            listOf("series", "season", "episode", "s01", "s02", "tv show").any { haystack.contains(it) } ->
                ContentCategory.SERIES
            listOf("movie", "vod", "film", "cinema").any { haystack.contains(it) } ->
                ContentCategory.MOVIE
            else -> ContentCategory.LIVE
        }
    }
}
