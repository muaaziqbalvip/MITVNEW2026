package com.mitv.master

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MitvApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Register an ImageLoader with SVG support so channel logos and
        // EPG program icons can be .svg (in addition to .png/.jpg) — the
        // admin panel can push either format.
        val imageLoader = ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
