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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.mitv.master.ui.theme.MitvRed
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

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistChannels(playlistId)
    }

    val grouped = viewModel.groupedByCategory()
    val liveChannels = grouped.values.flatten().filter { it.category == ContentCategory.LIVE }
    val movies = grouped.values.flatten().filter { it.category == ContentCategory.MOVIE }
    val series = grouped.values.flatten().filter { it.category == ContentCategory.SERIES }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(playlistName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            label = { Text("Search") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MitvRed,
                unfocusedBorderColor = Color(0xFF444444),
                focusedLabelColor = MitvRed,
                unfocusedLabelColor = MitvTextSecondary,
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
                item { CategoryHeader("Live TV") }
                item { ChannelRow(liveChannels, onChannelClick) }
            }
            if (movies.isNotEmpty()) {
                item { CategoryHeader("Movies") }
                item { ChannelRow(movies, onChannelClick) }
            }
            if (series.isNotEmpty()) {
                item { CategoryHeader("Series") }
                item { ChannelRow(series, onChannelClick) }
            }
            if (liveChannels.isEmpty() && movies.isEmpty() && series.isEmpty()) {
                item {
                    Text(
                        text = "No channels match your search.",
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
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun ChannelRow(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
        items(channels) { channel ->
            ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(120.dp, 160.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
            )
            Text(
                text = channel.name,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}
