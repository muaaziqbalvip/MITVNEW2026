package com.mitv.master.di

import com.mitv.master.data.repository.FirebaseRepository
import com.mitv.master.data.repository.PlaylistRepository
import com.mitv.master.data.repository.UserTrackingRepository
import com.mitv.master.data.repository.XtreamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseRepository(): FirebaseRepository = FirebaseRepository()

    @Provides
    @Singleton
    fun provideUserTrackingRepository(): UserTrackingRepository = UserTrackingRepository()

    @Provides
    @Singleton
    fun providePlaylistRepository(): PlaylistRepository = PlaylistRepository()

    @Provides
    @Singleton
    fun provideXtreamRepository(): XtreamRepository = XtreamRepository()
}
