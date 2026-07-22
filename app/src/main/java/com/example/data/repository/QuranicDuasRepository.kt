package com.example.data.repository

import android.content.Context
import com.example.domain.model.QuranicDua
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
class QuranicDuasRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun getQuranicDuasFlow(): Flow<List<QuranicDua>> = flow {
        val jsonString = context.assets.open("quranic_duas.json").bufferedReader().use { it.readText() }
        val type = Types.newParameterizedType(List::class.java, QuranicDua::class.java)
        val adapter = moshi.adapter<List<QuranicDua>>(type)
        val duas = adapter.fromJson(jsonString) ?: emptyList()
        emit(duas)
    }.flowOn(Dispatchers.IO)
}
