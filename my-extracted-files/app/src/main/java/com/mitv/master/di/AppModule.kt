package com.mitv.master.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MediaRepository and UserTrackingRepository use @Inject constructor(),
 * so Hilt provides them automatically — no explicit @Provides needed.
 * This module only supplies the shared FirebaseAuth instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
