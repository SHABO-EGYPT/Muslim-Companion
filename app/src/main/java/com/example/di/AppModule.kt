package com.example.di

import android.content.Context
import com.example.data.local.CompanionDatabase
import com.example.data.quran.QuranAssetLoader
import com.example.data.quran.QuranAudioManager
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.OfflineQuranRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.RealAzkarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.data.repository.NamesOfAllahRepository
import com.example.data.repository.WeatherRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCompanionDatabase(@ApplicationContext context: Context): CompanionDatabase {
        return CompanionDatabase.buildDatabase(context)
    }

    @Provides
    @Singleton
    fun provideQuranAssetLoader(
        @ApplicationContext context: Context,
        database: CompanionDatabase
    ): QuranAssetLoader = QuranAssetLoader(context, database.companionDao())

    @Provides
    @Singleton
    fun provideQuranAudioManager(@ApplicationContext context: Context): QuranAudioManager =
        QuranAudioManager(context)

    @Provides
    @Singleton
    fun provideQuranRepository(
        database: CompanionDatabase,
        assetLoader: QuranAssetLoader,
        audioManager: QuranAudioManager
    ): QuranRepository {
        return OfflineQuranRepository(database.companionDao(), assetLoader, audioManager)
    }

    @Provides
    @Singleton
    fun provideAzkarRepository(@ApplicationContext context: Context): AzkarRepository {
        return RealAzkarRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(): WeatherRepository {
        return WeatherRepository()
    }

    @Provides
    @Singleton
    fun provideNamesOfAllahRepository(@ApplicationContext context: Context): NamesOfAllahRepository {
        return NamesOfAllahRepository(context)
    }

    @Provides
    @Singleton
    fun provideQuranicDuasRepository(@ApplicationContext context: Context): com.example.data.repository.QuranicDuasRepository {
        return com.example.data.repository.QuranicDuasRepository(context)
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

