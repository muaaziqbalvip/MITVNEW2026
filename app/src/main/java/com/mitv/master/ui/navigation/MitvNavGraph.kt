package com.mitv.master.ui.navigation

sealed class MitvScreen(val route: String) {
    data object Splash : MitvScreen("splash")
    data object Login : MitvScreen("login")
    data object Home : MitvScreen("home")
    data object AddPlaylist : MitvScreen("add_playlist")
    data object BuyPro : MitvScreen("buy_pro")
    data object PlaylistDetail : MitvScreen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
    data object Player : MitvScreen("player/{channelId}") {
        fun createRoute(channelId: String) = "player/$channelId"
    }
}
