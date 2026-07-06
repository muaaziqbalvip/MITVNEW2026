package com.mitv.master.ui.screens.series

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mitv.master.data.model.MediaItem
import com.mitv.master.ui.theme.MitvGold
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvSurface
import com.mitv.master.ui.theme.MitvSurfaceElevated
import com.mitv.master.ui.theme.MitvTextSecondary
import com.mitv.master.viewmodel.SeriesViewModel

/** Lists episodes (grouped by season) for one series. Locked episodes route to Buy Pro. */
@Composable
fun SeriesDetailScreen(
    seriesId: String,
    seriesTitle: String,
    isPro: Boolean,
    onBack: () -> Unit,
    onPlayEpisode: (MediaItem, List<MediaItem>) -> Unit,
    onBuyProClick: () -> Unit,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val episodes by viewModel.episodes.collectAsState()

    LaunchedEffect(seriesId) {
        viewModel.loadSeries(seriesId)
    }

    val bySeason = episodes.groupBy { it.seasonNumber }.toSortedMap()

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text(seriesTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${episodes.size} episodes", color = MitvTextSecondary, fontSize = 12.sp)
            }
        }

        if (episodes.isEmpty()) {
            Text(
                text = "No episodes available yet.",
                color = MitvTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(20.dp)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            bySeason.forEach { (season, seasonEpisodes) ->
                item {
                    Text(
                        text = if (season > 0) "Season $season" else "Episodes",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                items(seasonEpisodes, key = { it.id }) { episode ->
                    EpisodeRow(
                        episode = episode,
                        locked = !episode.isFree && !isPro,
                        onClick = {
                            if (!episode.isFree && !isPro) onBuyProClick() else onPlayEpisode(episode, episodes)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: MediaItem, locked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MitvSurface)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(90.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(MitvSurfaceElevated)
        ) {
            AsyncImage(
                model = episode.backdropUrl.ifBlank { episode.posterUrl },
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (locked) MitvGold else MitvRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (locked) Icons.Filled.Lock else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = if (locked) Color.Black else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "E${episode.episodeNumber}. ${episode.title}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1
            )
            if (episode.description.isNotBlank()) {
                Text(
                    text = episode.description,
                    color = MitvTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
