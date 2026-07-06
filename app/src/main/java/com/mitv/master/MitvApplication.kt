package com.mitv.master

import android.app.Application
import com.mitv.master.util.NewPipeOkHttpDownloader
import dagger.hilt.android.HiltAndroidApp
import org.schabi.newpipe.extractor.NewPipe

@HiltAndroidApp
class MitvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Register the OkHttp-based downloader so NewPipeExtractor can
        // resolve YouTube video/stream info (see YoutubeResolver.kt).
        NewPipe.init(NewPipeOkHttpDownloader.instance)
    }
}
