package com.example.di

import android.content.Context
import com.example.data.local.CompanionDatabase
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.RealAzkarRepository
import com.example.data.repository.RealQuranRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCompanionDatabase(@ApplicationContext context: Context): CompanionDatabase {
        return CompanionDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideQuranRepository(database: CompanionDatabase): QuranRepository {
        return RealQuranRepository(database.companionDao())
    }

    @Provides
    @Singleton
    fun provideAzkarRepository(@ApplicationContext context: Context): AzkarRepository {
        return RealAzkarRepository(context)
    }

    @Provides
    @Singleton
    fun provideCompanionRepository(
        database: CompanionDatabase,
        quranRepository: QuranRepository,
        azkarRepository: AzkarRepository
    ): CompanionRepository {
        return CompanionRepository(database.companionDao(), quranRepository, azkarRepository)
    }
}
