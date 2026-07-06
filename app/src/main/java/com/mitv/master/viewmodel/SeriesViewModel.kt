package com.mitv.master.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitv.master.data.model.MediaItem
import com.mitv.master.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads the episode list for a single series (grouped by seriesId in Firebase). */
@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _episodes = MutableStateFlow<List<MediaItem>>(emptyList())
    val episodes: StateFlow<List<MediaItem>> = _episodes.asStateFlow()

    fun loadSeries(seriesId: String) {
        viewModelScope.launch {
            mediaRepository.observeSeriesEpisodes(seriesId).collect { list ->
                _episodes.value = list
            }
        }
    }
}
