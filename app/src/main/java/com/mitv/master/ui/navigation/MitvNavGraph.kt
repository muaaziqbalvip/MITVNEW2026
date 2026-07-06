package com.mitv.master.ui.navigation

sealed class MitvScreen(val route: String) {
    data object Splash : MitvScreen("splash")
    data object Login : MitvScreen("login")
    data object Home : MitvScreen("home")
    data object BuyPro : MitvScreen("buy_pro")
    data object SeriesDetail : MitvScreen("series_detail/{seriesId}") {
        fun createRoute(seriesId: String) = "series_detail/$seriesId"
    }
    data object Player : MitvScreen("player/{itemId}/{category}") {
        fun createRoute(itemId: String, category: String) = "player/$itemId/$category"
    }
}
