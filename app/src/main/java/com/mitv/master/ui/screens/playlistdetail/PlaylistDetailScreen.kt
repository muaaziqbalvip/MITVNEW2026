package com.mitv.master.ui.screens.playlistdetail

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.ContentCategory
import com.mitv.master.ui.theme.MitvLiveGreen
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvSurface
import com.mitv.master.ui.theme.MitvSurfaceElevated
import com.mitv.master.ui.theme.MitvTextSecondary
import com.mitv.master.viewmodel.HomeViewModel

/**
 * Shows a single playlist's content split into Live TV / Movies / Series
 * category rows (Netflix-style), plus a search field to filter by name.
 */
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    playlistName: String,
    onBack: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allChannels by viewModel.selectedPlaylistChannels.collectAsState()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistChannels(playlistId)
    }

    val query = searchQuery.trim()
    val filtered = if (query.isEmpty()) {
        allChannels
    } else {
        allChannels.filter { it.name.contains(query, ignoreCase = true) }
    }
    val liveChannels = filtered.filter { it.category == ContentCategory.LIVE }
    val movies = filtered.filter { it.category == ContentCategory.MOVIE }
    val series = filtered.filter { it.category == ContentCategory.SERIES }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text(playlistName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "${allChannels.size} channels",
                    color = MitvTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Search channels", color = MitvTextSecondary) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MitvTextSecondary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MitvRed,
                unfocusedBorderColor = Color(0xFF333333),
                focusedContainerColor = MitvSurface,
                unfocusedContainerColor = MitvSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = MitvRed
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (liveChannels.isNotEmpty()) {
                item { CategoryHeader("Live TV", liveChannels.size) }
                item { ChannelRow(liveChannels, onChannelClick, showLiveBadge = true) }
            }
            if (movies.isNotEmpty()) {
                item { CategoryHeader("Movies", movies.size) }
                item { ChannelRow(movies, onChannelClick) }
            }
            if (series.isNotEmpty()) {
                item { CategoryHeader("Series", series.size) }
                item { ChannelRow(series, onChannelClick) }
            }
            if (liveChannels.isEmpty() && movies.isEmpty() && series.isEmpty()) {
                item {
                    Text(
                        text = if (query.isEmpty()) "This playlist has no channels." else "No channels match \"$query\".",
                        color = MitvTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String, count: Int) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
    ) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            text = "  $count",
            color = MitvTextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ChannelRow(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    showLiveBadge: Boolean = false
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelCard(channel = channel, onClick = { onChannelClick(channel) }, showLiveBadge = showLiveBadge)
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onClick: () -> Unit, showLiveBadge: Boolean) {
    Column(
        modifier = Modifier
            .width(128.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MitvSurfaceElevated)
        ) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (channel.logoUrl.isBlank()) {
                Icon(
                    Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = MitvTextSecondary,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }
            if (showLiveBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MitvLiveGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = channel.name,
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}
