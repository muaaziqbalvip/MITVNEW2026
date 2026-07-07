package com.mitv.master

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.mitv.master.data.model.ContentCategory
import com.mitv.master.data.model.MediaItem
import com.mitv.master.ui.navigation.MitvScreen
import com.mitv.master.ui.screens.buypro.BuyProScreen
import com.mitv.master.ui.screens.home.HomeScreen
import com.mitv.master.ui.screens.login.LoginScreen
import com.mitv.master.ui.screens.player.PlayerScreen
import com.mitv.master.ui.screens.series.SeriesDetailScreen
import com.mitv.master.ui.screens.splash.SplashScreen
import com.mitv.master.ui.theme.MITVTheme
import com.mitv.master.viewmodel.HomeViewModel
import com.mitv.master.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val webClientId = "882828936310-itknilv5rqn9pn6uvangeglnjjf7h8vo.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MITVTheme {
                MitvAppRoot()
            }
        }
    }

    @Composable
    private fun MitvAppRoot() {
        val navController = rememberNavController()
        val loginViewModel: LoginViewModel = hiltViewModel()
        val homeViewModel: HomeViewModel = hiltViewModel()

        val gso = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        }
        val googleSignInClient = remember { GoogleSignIn.getClient(this, gso) }
        val launcher = rememberLauncherForGoogleSignIn(loginViewModel)

        // Holds what the player screen needs: the tapped item + the row/list
        // it came from, so Next/Previous can move through the same set.
        var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
        var currentPlaylistContext by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
        var selectedSeriesTitle by remember { mutableStateOf("Series") }

        val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()
        val profile by homeViewModel.userProfile.collectAsState()

        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) {
                navController.navigate(MitvScreen.Home.route) {
                    popUpTo(MitvScreen.Login.route) { inclusive = true }
                }
            }
        }

        NavHost(navController = navController, startDestination = MitvScreen.Splash.route) {
            composable(MitvScreen.Splash.route) {
                SplashScreen(onFinished = {
                    // IMPORTANT: check FirebaseAuth's real session, not the
                    // Google Play Services account cache. GoogleSignIn.getLastSignedInAccount()
                    // only reflects whether a Google account was ever picked on
                    // this device — it says nothing about whether that sign-in
                    // actually completed into Firebase. If it's out of sync
                    // (reinstall, cleared app data, cache drift), the app would
                    // land on Home with FirebaseAuth.currentUser == null, and
                    // every Realtime Database read guarded by "auth != null"
                    // rules would silently fail, showing an empty/blank app
                    // even though the admin panel shows channels just fine.
                    val dest = if (FirebaseAuth.getInstance().currentUser != null) {
                        MitvScreen.Home.route
                    } else {
                        MitvScreen.Login.route
                    }
                    navController.navigate(dest) {
                        popUpTo(MitvScreen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(MitvScreen.Login.route) {
                LoginScreen(
                    onGoogleSignInClicked = { launcher.launch(googleSignInClient.signInIntent) },
                    viewModel = loginViewModel
                )
            }

            composable(MitvScreen.Home.route) {
                HomeScreen(
                    onPlayItem = { media ->
                        selectedItem = media
                        currentPlaylistContext = when (media.category) {
                            ContentCategory.LIVE -> homeViewModel.liveChannels.value
                            ContentCategory.MOVIE -> homeViewModel.movies.value
                            else -> listOf(media)
                        }
                        navController.navigate(MitvScreen.Player.createRoute(media.id, media.category.name))
                    },
                    onOpenSeries = { series ->
                        selectedSeriesTitle = series.title
                        navController.navigate(MitvScreen.SeriesDetail.createRoute(series.seriesId.ifBlank { series.id }))
                    },
                    onBuyProClick = { navController.navigate(MitvScreen.BuyPro.route) },
                    viewModel = homeViewModel
                )
            }

            composable(MitvScreen.BuyPro.route) {
                BuyProScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = MitvScreen.SeriesDetail.route,
                arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
            ) { backStackEntry ->
                val seriesId = backStackEntry.arguments?.getString("seriesId").orEmpty()
                SeriesDetailScreen(
                    seriesId = seriesId,
                    seriesTitle = selectedSeriesTitle,
                    isPro = profile.isPro,
                    onBack = { navController.popBackStack() },
                    onPlayEpisode = { episode, episodeList ->
                        selectedItem = episode
                        currentPlaylistContext = episodeList
                        navController.navigate(MitvScreen.Player.createRoute(episode.id, ContentCategory.SERIES.name))
                    },
                    onBuyProClick = { navController.navigate(MitvScreen.BuyPro.route) }
                )
            }

            composable(
                route = MitvScreen.Player.route,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType }
                )
            ) {
                selectedItem?.let { item ->
                    PlayerScreen(
                        item = item,
                        playlistContext = currentPlaylistContext.ifEmpty { listOf(item) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    @Composable
    private fun rememberLauncherForGoogleSignIn(loginViewModel: LoginViewModel) =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                account?.idToken?.let { idToken -> loginViewModel.signInWithGoogleToken(idToken) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
