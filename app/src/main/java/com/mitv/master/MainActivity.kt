package com.mitv.master

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.Playlist
import com.mitv.master.ui.navigation.MitvScreen
import com.mitv.master.ui.screens.addplaylist.AddPlaylistScreen
import com.mitv.master.ui.screens.home.HomeScreen
import com.mitv.master.ui.screens.login.LoginScreen
import com.mitv.master.ui.screens.player.PlayerScreen
import com.mitv.master.ui.screens.playlistdetail.PlaylistDetailScreen
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

    @androidx.compose.runtime.Composable
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

        var selectedChannel by remember { mutableStateOf<Channel?>(null) }
        var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

        val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()

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
                    val dest = if (GoogleSignIn.getLastSignedInAccount(this@MainActivity) != null) {
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
                    onGoogleSignInClicked = {
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    viewModel = loginViewModel
                )
            }
            composable(MitvScreen.Home.route) {
                HomeScreen(
                    onChannelClick = { channel ->
                        selectedChannel = channel
                        navController.navigate(MitvScreen.Player.createRoute(channel.id))
                    },
                    onAddPlaylistClick = {
                        navController.navigate(MitvScreen.AddPlaylist.route)
                    },
                    onPlaylistOpen = { playlist ->
                        selectedPlaylist = playlist
                        navController.navigate(MitvScreen.PlaylistDetail.createRoute(playlist.id))
                    },
                    viewModel = homeViewModel
                )
            }
            composable(MitvScreen.AddPlaylist.route) {
                AddPlaylistScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = MitvScreen.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    playlistName = selectedPlaylist?.name ?: "Playlist",
                    onBack = { navController.popBackStack() },
                    onChannelClick = { channel ->
                        selectedChannel = channel
                        navController.navigate(MitvScreen.Player.createRoute(channel.id))
                    },
                    viewModel = homeViewModel
                )
            }
            composable(
                route = MitvScreen.Player.route,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) {
                selectedChannel?.let { channel ->
                    PlayerScreen(channel = channel, onBack = { navController.popBackStack() })
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun rememberLauncherForGoogleSignIn(loginViewModel: LoginViewModel) =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                account?.idToken?.let { idToken ->
                    loginViewModel.signInWithGoogleToken(idToken)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
