package com.example.data.repository

import android.content.Context
import com.example.domain.model.NameOfAllah
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NamesOfAllahRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun getNamesOfAllahFlow(): Flow<List<NameOfAllah>> = flow {
        val jsonString = context.assets.open("names_of_allah.json").bufferedReader().use { it.readText() }
        val type = Types.newParameterizedType(List::class.java, NameOfAllah::class.java)
        val adapter = moshi.adapter<List<NameOfAllah>>(type)
        val names = adapter.fromJson(jsonString) ?: emptyList()
        emit(names)
    }.flowOn(Dispatchers.IO)
}
