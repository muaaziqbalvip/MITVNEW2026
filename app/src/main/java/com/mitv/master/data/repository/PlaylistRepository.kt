package com.mitv.master.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mitv.master.data.model.Channel
import com.mitv.master.data.model.Playlist
import com.mitv.master.data.model.ProMediaItem
import com.mitv.master.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles all per-user playlist storage (free tier — manually added M3U/Xtream
 * playlists) and the admin-curated MITV Pro catalog (read-only for users,
 * unlocked based on their isPro flag).
 *
 * Structure:
 *   /users/{uid}/profile                     -> UserProfile (isPro flag lives here)
 *   /users/{uid}/playlists/{playlistId}       -> Playlist metadata
 *   /users/{uid}/playlists/{playlistId}/channels/{channelId} -> Channel
 *   /pro_content/live_channels/{id}           -> ProMediaItem (admin-only writes)
 *   /pro_content/movies/{id}
 *   /pro_content/series/{id}
 */
@Singleton
class PlaylistRepository @Inject constructor() {

    private val db = FirebaseDatabase.getInstance()

    // ---------- User profile / Pro status ----------

    fun observeUserProfile(uid: String): Flow<UserProfile> = callbackFlow {
        val ref = db.getReference("users").child(uid).child("profile")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue<UserProfile>() ?: UserProfile(uid = uid)
                trySend(profile)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun ensureUserProfileExists(uid: String, email: String) {
        val ref = db.getReference("users").child(uid).child("profile")
        val snapshot = ref.get().await()
        if (!snapshot.exists()) {
            ref.setValue(UserProfile(uid = uid, email = email, isPro = false)).await()
        }
    }

    // ---------- Free tier: user's own playlists ----------

    fun observeUserPlaylists(uid: String): Flow<List<Playlist>> = callbackFlow {
        val ref = db.getReference("users").child(uid).child("playlists")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue<Playlist>() }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun savePlaylist(uid: String, playlist: Playlist, channels: List<Channel>) {
        val playlistRef = db.getReference("users").child(uid).child("playlists").child(playlist.id)
        playlistRef.setValue(playlist.copy(channelCount = channels.size)).await()

        val channelsRef = playlistRef.child("channels")
        val updates = channels.associateBy({ it.id }, { it })
        channelsRef.setValue(updates).await()
    }

    fun observePlaylistChannels(uid: String, playlistId: String): Flow<List<Channel>> = callbackFlow {
        val ref = db.getReference("users").child(uid)
            .child("playlists").child(playlistId).child("channels")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue<Channel>() }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun deletePlaylist(uid: String, playlistId: String) {
        db.getReference("users").child(uid).child("playlists").child(playlistId).removeValue().await()
    }

    // ---------- Pro tier: admin-curated catalog (read-only for users) ----------

    fun observeProLiveChannels(): Flow<List<ProMediaItem>> =
        observeProNode("live_channels")

    fun observeProMovies(): Flow<List<ProMediaItem>> =
        observeProNode("movies")

    fun observeProSeries(): Flow<List<ProMediaItem>> =
        observeProNode("series")

    private fun observeProNode(node: String): Flow<List<ProMediaItem>> = callbackFlow {
        val ref = db.getReference("pro_content").child(node)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue<ProMediaItem>() }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
