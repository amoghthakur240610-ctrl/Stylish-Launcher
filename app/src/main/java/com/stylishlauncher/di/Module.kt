package com.stylishlauncher.di

import android.content.Context
import com.stylishlauncher.data.AppRepository
import com.stylishlauncher.data.SearchRepository
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
    fun provideAppRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AppRepository {
        return AppRepository(context)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
        appRepository: AppRepository,
    ): SearchRepository {
        return SearchRepository(context, appRepository)
    }
}