package com.example.di

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.CompanionDatabase
import com.example.data.quran.QuranAssetLoader
import com.example.data.quran.QuranAudioManager
import com.example.data.remote.GeminiApiService
import com.example.data.remote.PrayerApi
import com.example.data.remote.QuranApi
import com.example.data.remote.WeatherApi
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.NamesOfAllahRepository
import com.example.data.repository.OfflineQuranRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.QuranicDuasRepository
import com.example.data.repository.RealAzkarRepository
import com.example.data.repository.WeatherRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun providePrayerApi(okHttpClient: OkHttpClient, moshi: Moshi): PrayerApi {
        return Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PrayerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(okHttpClient: OkHttpClient, moshi: Moshi): WeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideQuranApi(okHttpClient: OkHttpClient, moshi: Moshi): QuranApi {
        return Retrofit.Builder()
            .baseUrl("https://api.quran.com/api/v4/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(okHttpClient: OkHttpClient, moshi: Moshi): GeminiApiService {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

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
        audioManager: QuranAudioManager,
        quranApi: QuranApi
    ): QuranRepository {
        return OfflineQuranRepository(database.companionDao(), assetLoader, audioManager, quranApi)
    }

    @Provides
    @Singleton
    fun provideAzkarRepository(@ApplicationContext context: Context): AzkarRepository {
        return RealAzkarRepository(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(weatherApi: WeatherApi): WeatherRepository {
        return WeatherRepository(weatherApi)
    }

    @Provides
    @Singleton
    fun provideNamesOfAllahRepository(@ApplicationContext context: Context, moshi: Moshi): NamesOfAllahRepository {
        return NamesOfAllahRepository(context, moshi)
    }

    @Provides
    @Singleton
    fun provideQuranicDuasRepository(@ApplicationContext context: Context, moshi: Moshi): QuranicDuasRepository {
        return QuranicDuasRepository(context, moshi)
    }

    @Provides
    @Singleton
    fun provideCompanionRepository(
        database: CompanionDatabase,
        quranRepository: QuranRepository,
        azkarRepository: AzkarRepository,
        prayerApi: PrayerApi
    ): CompanionRepository {
        return CompanionRepository(database.companionDao(), quranRepository, azkarRepository, prayerApi)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): com.example.data.location.LocationRepository {
        return com.example.data.location.RealLocationRepository(context)
    }

    @Provides
    @Singleton
    fun provideCustomDhikrRepository(database: CompanionDatabase): com.example.data.repository.CustomDhikrRepository {
        return com.example.data.repository.RealCustomDhikrRepository(database.companionDao())
    }
}


