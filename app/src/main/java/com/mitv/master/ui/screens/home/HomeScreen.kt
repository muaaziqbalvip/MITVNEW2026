package com.mitv.master.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mitv.master.data.model.ContentCategory
import com.mitv.master.data.model.MediaItem
import com.mitv.master.ui.theme.MitvGold
import com.mitv.master.ui.theme.MitvLiveGreen
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvSurface
import com.mitv.master.ui.theme.MitvSurfaceElevated
import com.mitv.master.ui.theme.MitvTextSecondary
import com.mitv.master.viewmodel.HomeViewModel

/**
 * Netflix-style home: search bar, a hero pick, then Live TV / Movies / Series
 * rows sourced entirely from the admin-managed Firebase catalog. Pro-only
 * items show a lock badge; tapping one that the user can't access routes to
 * Buy Pro instead of the player.
 */
@Composable
fun HomeScreen(
    onPlayItem: (MediaItem) -> Unit,
    onOpenSeries: (MediaItem) -> Unit,
    onBuyProClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val live by viewModel.liveChannels.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val series by viewModel.seriesShows.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val expiryNotice by viewModel.expiryNotice.collectAsState()

    val isPro = profile.isPro
    val q = query.trim()

    fun matches(item: MediaItem) = q.isEmpty() || item.title.contains(q, ignoreCase = true)

    val filteredLive = live.filter(::matches)
    val filteredMovies = movies.filter(::matches)
    val filteredSeries = series.filter(::matches)

    fun handleTap(item: MediaItem) {
        if (item.isFree || isPro) {
            if (item.category == ContentCategory.SERIES) onOpenSeries(item) else onPlayItem(item)
        } else {
            onBuyProClick()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { HomeHeader(isPro = isPro, onBuyProClick = onBuyProClick) }
            expiryNotice?.let { notice ->
                item {
                    ExpiryBanner(
                        message = notice,
                        onRenew = onBuyProClick,
                        onDismiss = viewModel::dismissExpiryNotice
                    )
                }
            }
            item {
                SearchField(
                    value = query,
                    onValueChange = viewModel::onSearchQueryChanged
                )
            }

            if (filteredLive.isEmpty() && filteredMovies.isEmpty() && filteredSeries.isEmpty()) {
                item { EmptyCatalogState(hasQuery = q.isNotEmpty()) }
            }

            if (filteredLive.isNotEmpty()) {
                item { SectionTitle("Live TV") }
                item { MediaRow(filteredLive, isPro, showLiveBadge = true, onClick = ::handleTap) }
            }
            if (filteredMovies.isNotEmpty()) {
                item { SectionTitle("Movies") }
                item { MediaRow(filteredMovies, isPro, onClick = ::handleTap) }
            }
            if (filteredSeries.isNotEmpty()) {
                item { SectionTitle("Series") }
                item { MediaRow(filteredSeries, isPro, onClick = ::handleTap) }
            }

            item { FooterCredit() }
        }
    }
}

@Composable
private fun HomeHeader(isPro: Boolean, onBuyProClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Tv, contentDescription = null, tint = MitvRed, modifier = Modifier.size(26.dp))
            Text(
                text = "MITV",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        if (isPro) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MitvGold.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MitvGold, modifier = Modifier.size(16.dp))
                Text("PRO", color = MitvGold, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MitvRed)
                    .clickable { onBuyProClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Text("Get Pro", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(start = 5.dp))
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Search live TV, movies, series", color = MitvTextSecondary, fontSize = 13.sp) },
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
            .padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
private fun ExpiryBanner(message: String, onRenew: () -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A1414))
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MitvGold, modifier = Modifier.size(18.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = MitvTextSecondary, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = message,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )
        Text(
            text = "Renew now →",
            color = MitvRed,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { onRenew() }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 10.dp)
    )
}

@Composable
private fun EmptyCatalogState(hasQuery: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Tv, contentDescription = null, tint = MitvTextSecondary, modifier = Modifier.size(40.dp))
        Text(
            text = if (hasQuery) "No results found." else "Content is being prepared. Check back soon!",
            color = MitvTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun MediaRow(
    items: List<MediaItem>,
    isPro: Boolean,
    showLiveBadge: Boolean = false,
    onClick: (MediaItem) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        items(items, key = { it.id }) { media ->
            MediaCard(media = media, isPro = isPro, showLiveBadge = showLiveBadge, onClick = { onClick(media) })
        }
    }
}

@Composable
private fun MediaCard(media: MediaItem, isPro: Boolean, showLiveBadge: Boolean, onClick: () -> Unit) {
    val locked = !media.isFree && !isPro
    val imageUrl = media.posterUrl.ifBlank { media.logoUrl }

    Column(modifier = Modifier.width(128.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (showLiveBadge) 1f else 0.68f)
                .clip(RoundedCornerShape(12.dp))
                .background(MitvSurfaceElevated)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = media.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (imageUrl.isBlank()) {
                Icon(
                    Icons.Filled.Tv,
                    contentDescription = null,
                    tint = MitvTextSecondary,
                    modifier = Modifier.size(32.dp).align(Alignment.Center)
                )
            }
            if (showLiveBadge && !locked) {
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
            if (locked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MitvGold.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = "Pro only", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            } else if (!showLiveBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(MitvRed)
                        .size(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        Text(
            text = media.title,
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        )
    }
}

@Composable
private fun FooterCredit() {
    Text(
        text = "Developed by Muaaz Iqbal — Muslim Islam Org",
        color = Color(0xFF444444),
        fontSize = 10.sp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
        textAlign = TextAlign.Center
    )
}
