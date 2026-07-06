package com.mitv.master.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.Playlist
import com.mitv.master.data.model.ProMediaItem
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvTextSecondary
import com.mitv.master.viewmodel.HomeViewModel

const val MITV_PRO_CONTACT_NUMBER = "03062015326"

@Composable
fun HomeScreen(
    onChannelClick: (Channel) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onPlaylistOpen: (Playlist) -> Unit,
    onBuyProClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userPlaylists by viewModel.userPlaylists.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val proLive by viewModel.proLiveChannels.collectAsState()
    val proMovies by viewModel.proMovies.collectAsState()
    val proSeries by viewModel.proSeries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                HomeHeader(onAddPlaylistClick = onAddPlaylistClick)
            }

            item {
                SectionTitle("My Playlists")
            }

            if (userPlaylists.isEmpty()) {
                item {
                    EmptyPlaylistState(onAddPlaylistClick)
                }
            } else {
                items(userPlaylists) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        onClick = { onPlaylistOpen(playlist) },
                        onDelete = { viewModel.deletePlaylist(playlist.id) }
                    )
                }
            }

            item {
                SectionTitle("MITV Pro")
            }

            item {
                if (userProfile.isPro) {
                    ProUnlockedSection(proLive, proMovies, proSeries)
                } else {
                    ProLockedBanner(onBuyProClick = onBuyProClick)
                }
            }

            item {
                FooterCredit()
            }
        }
    }
}

@Composable
private fun HomeHeader(onAddPlaylistClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "MITV",
            color = MitvRed,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp
        )
        IconButton(
            onClick = onAddPlaylistClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MitvRed)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = Color.White)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun EmptyPlaylistState(onAddPlaylistClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = MitvTextSecondary,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No playlists yet",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Add an M3U link, upload a file, or connect Xtream Codes to get started.",
            color = MitvTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Button(
            onClick = onAddPlaylistClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MitvRed)
        ) {
            Text("Add Playlist", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(playlist.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                "${playlist.channelCount} channels",
                color = MitvTextSecondary,
                fontSize = 12.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Open", tint = MitvRed)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MitvTextSecondary)
            }
        }
    }
}

@Composable
private fun ProLockedBanner(onBuyProClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141414))
            .clickable { onBuyProClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MitvRed)
            Text(
                text = "Unlock MITV Pro",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = "Get instant access to 4000+ live channels, movies, and series — curated and ready to watch.",
            color = MitvTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
        Text(
            text = "Rs 50/month — Tap to see how to activate",
            color = MitvRed,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ProUnlockedSection(
    live: List<ProMediaItem>,
    movies: List<ProMediaItem>,
    series: List<ProMediaItem>
) {
    Column {
        if (live.isNotEmpty()) ProRow("Live TV", live)
        if (movies.isNotEmpty()) ProRow("Movies", movies)
        if (series.isNotEmpty()) ProRow("Series", series)
        if (live.isEmpty() && movies.isEmpty() && series.isEmpty()) {
            Text(
                text = "Pro content is being prepared. Check back soon!",
                color = MitvTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Composable
private fun ProRow(title: String, items: List<ProMediaItem>) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
        )
        LazyRow(modifier = Modifier.padding(horizontal = 12.dp)) {
            items(items) { item ->
                ProCard(item)
            }
        }
    }
}

@Composable
private fun ProCard(item: ProMediaItem) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .size(110.dp, 150.dp)
    ) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FooterCredit() {
    Text(
        text = "Developed by Muaaz Iqbal — Muslim Islam Org",
        color = Color(0xFF555555),
        fontSize = 10.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
