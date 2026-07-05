package com.mitv.master

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MitvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // YouTube resolution now handled by the lightweight YoutubeResolver
        // (OkHttp-based extraction) — no heavy library init needed here.
    }
}