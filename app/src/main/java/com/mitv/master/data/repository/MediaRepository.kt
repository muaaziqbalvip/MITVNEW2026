package com.mitv.master.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.mitv.master.data.model.EpgProgram
import com.mitv.master.data.model.MediaItem
import com.mitv.master.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the shared, admin-curated content catalog. All writes to these
 * nodes happen from the HTML admin panel — the app only ever reads.
 *
 * Structure:
 *   /users/{uid}/profile           -> UserProfile (isPro flag, expiry)
 *   /live_channels/{id}            -> MediaItem (category = LIVE)
 *   /movies/{id}                   -> MediaItem (category = MOVIE)
 *   /series/{seriesId}/episodes/{episodeId} -> MediaItem (category = SERIES)
 *   /epg/{channelId}/{programId}   -> EpgProgram
 *   /app_config/update             -> forced-update flag (unchanged)
 */
@Singleton
class MediaRepository @Inject constructor() {

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

    /**
     * Marks the "your Pro subscription is expiring/expired" notice as shown,
     * so the app doesn't keep re-showing it every launch.
     */
    suspend fun markExpiryNotified(uid: String) {
        db.getReference("users").child(uid).child("profile")
            .child("proExpiryNotified").setValue(true).await()
    }

    // ---------- Live TV ----------

    fun observeLiveChannels(): Flow<List<MediaItem>> = observeNode("live_channels")

    // ---------- Movies ----------

    fun observeMovies(): Flow<List<MediaItem>> = observeNode("movies")

    // ---------- Series ----------

    /** All series "cover" entries — one per show, episodes nested under seriesId. */
    fun observeSeriesEpisodes(seriesId: String): Flow<List<MediaItem>> = callbackFlow {
        val ref = db.getReference("series").child(seriesId).child("episodes")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { safeMediaItem(it) }
                    .sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Distinct series "shows" — derived from the first episode of each seriesId group. */
    fun observeAllSeries(): Flow<List<MediaItem>> = observeNode("series_index")

    // ---------- EPG (program guide) ----------

    fun observeEpg(channelId: String): Flow<List<EpgProgram>> = callbackFlow {
        val ref = db.getReference("epg").child(channelId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue<EpgProgram>() }
                    .sortedBy { it.startTimestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ---------- Remote update flag (Netflix-style "Update Available") ----------

    fun observeUpdateFlag(): Flow<AppUpdateInfo> = callbackFlow {
        val ref = db.getReference("app_config").child("update")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val info = snapshot.getValue<AppUpdateInfo>() ?: AppUpdateInfo()
                trySend(info)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ---------- Shared helper ----------

    /**
     * Safely converts a single DataSnapshot into a MediaItem.
     * Firebase's getValue<T>() throws DatabaseException if a field's stored
     * type doesn't match the data class (e.g. an old entry with sortOrder
     * saved as a String, or a null field for a non-nullable String).
     * A single malformed entry must NEVER take down the whole list — before
     * this fix, one bad row from a bulk import could make mapNotNull crash
     * synchronously inside onDataChange, which Flow.catch{} cannot intercept
     * because the throw happens outside the coroutine's suspend machinery.
     */
    private fun safeMediaItem(snap: DataSnapshot): MediaItem? {
        return try {
            snap.getValue<MediaItem>()
        } catch (e: Exception) {
            null
        }
    }

    private fun observeNode(node: String): Flow<List<MediaItem>> = callbackFlow {
        val ref = db.getReference(node)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { safeMediaItem(it) }
                    .sortedBy { it.sortOrder }
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

data class AppUpdateInfo(
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val forceUpdate: Boolean = false,
    val updateMessage: String = "",
    val downloadUrl: String = ""
)
