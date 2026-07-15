package com.example.data.repository

import android.content.Context
import com.example.data.local.UserProgressEntity
import com.example.domain.model.AzkarCategory
import com.example.domain.model.DhikrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

interface AzkarRepository {
    fun getAzkarCategoriesFlow(progress: UserProgressEntity): Flow<List<AzkarCategory>>
    fun getDhikrItemsFlow(categoryId: String): Flow<List<DhikrItem>>
    fun getTasbihPhrasesFlow(): Flow<List<DhikrItem>>
    fun searchDhikrItems(query: String): Flow<List<DhikrItem>>
}

class RealAzkarRepository(private val context: Context) : AzkarRepository {
    private val azkarData = mutableMapOf<String, List<DhikrItem>>()
    private val mutex = Mutex()

    private suspend fun ensureLoaded() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (azkarData.isNotEmpty()) return@withContext
            try {
                // 1. Load and parse new azkar.json (which is a JSON Object of categories)
                val jsonString = context.assets.open("azkar.json").bufferedReader().use { it.readText() }
                val categoriesObj = JSONObject(jsonString)
                val keys = categoriesObj.keys()
                while (keys.hasNext()) {
                    val categoryName = keys.next()
                    val itemsArray = categoriesObj.getJSONArray(categoryName)
                    val dhikrItems = mutableListOf<DhikrItem>()
                    var itemId = 1
                    for (j in 0 until itemsArray.length()) {
                        dhikrItems.add(itemsArray.getJSONObject(j).toDhikrItem(itemId++))
                    }
                    azkarData[categoryName] = dhikrItems
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Load and parse legacy tasbih.json and merge it under "تسابيح"
            try {
                val tasbihString = context.assets.open("tasbih.json").bufferedReader().use { it.readText() }
                val tasbihArray = JSONArray(tasbihString)
                val tasbihItems = mutableListOf<DhikrItem>()
                var tasbihId = 1
                for (i in 0 until tasbihArray.length()) {
                    tasbihItems.add(tasbihArray.getJSONObject(i).toDhikrItem(tasbihId++))
                }
                azkarData["تسابيح"] = tasbihItems
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getAzkarCategoriesFlow(progress: UserProgressEntity): Flow<List<AzkarCategory>> = flow {
        ensureLoaded()
        emit(
            azkarData.keys.filter { it != "تسابيح" }.mapIndexed { index, name ->
                val iconName = when {
                    name.contains("الصباح") -> "sunrise"
                    name.contains("المساء") -> "sunset"
                    name.contains("النوم") -> "moon"
                    name.contains("الصلاة") -> "star"
                    else -> "sparkles"
                }
                val color = when {
                    name.contains("الصباح") -> 0xFFFFDEA0L
                    name.contains("المساء") -> 0xFFA8F2DCL
                    name.contains("النوم") -> 0xFFCCE8DAL
                    else -> 0xFFD8E2FFL
                }
                val progressCount = when {
                    name.contains("الصباح") -> progress.morningDone
                    name.contains("المساء") -> progress.eveningDone
                    name.contains("النوم") -> progress.sleepDone
                    name.contains("الصلاة") -> progress.afterPrayerDone
                    else -> 0
                }
                val title = when (name) {
                    "أذكار الصباح" -> "Morning Azkar"
                    "أذكار المساء" -> "Evening Azkar"
                    "أذكار النوم" -> "Sleep Azkar"
                    "أذكار بعد الصلاة" -> "Post-Prayer Azkar"
                    "أذكار الاستيقاظ" -> "Wakeup Azkar"
                    "دعاء الاستخارة" -> "Dua Al-Istikhara"
                    "دعاء للمريض" -> "Dua for the Sick"
                    else -> name
                }
                AzkarCategory(
                    id = name,
                    title = title,
                    arabicTitle = name,
                    totalCount = azkarData[name]?.size ?: 0,
                    doneCount = progressCount,
                    iconName = iconName,
                    colorHex = color
                )
            }
        )
    }

    override fun getDhikrItemsFlow(categoryId: String): Flow<List<DhikrItem>> = flow {
        ensureLoaded()
        emit(azkarData[categoryId] ?: emptyList())
    }

    override fun getTasbihPhrasesFlow(): Flow<List<DhikrItem>> = flow {
        ensureLoaded()
        emit(azkarData["تسابيح"] ?: emptyList())
    }

    override fun searchDhikrItems(query: String): Flow<List<DhikrItem>> = flow {
        ensureLoaded()
        val results = mutableListOf<DhikrItem>()
        for ((_, items) in azkarData) {
            for (item in items) {
                if (item.arabicText.contains(query) || item.englishTranslation.contains(query)) {
                    results.add(item)
                }
            }
        }
        emit(results.distinctBy { it.arabicText })
    }

    private fun JSONObject.toDhikrItem(id: Int): DhikrItem {
        val rawCount = opt("count")
        val count = when (rawCount) {
            null -> 1
            is Number -> rawCount.toInt()
            is String -> rawCount.toIntOrNull() ?: 1
            else -> 1
        }
        return DhikrItem(
            id = id,
            arabicText = optString("text", optString("content", "")),
            englishTranslation = optString("description", ""),
            translit = "",
            repeatTarget = count
        )
    }
}
