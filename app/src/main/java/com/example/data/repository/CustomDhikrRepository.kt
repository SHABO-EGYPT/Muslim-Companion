package com.example.data.repository

import com.example.data.local.CompanionDao
import com.example.data.local.CustomDhikrChainEntity
import com.example.domain.model.ChainDhikrItem
import com.example.domain.model.CustomDhikrChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

interface CustomDhikrRepository {
    fun getAllChainsFlow(): Flow<List<CustomDhikrChain>>
    suspend fun getChainById(id: Int): CustomDhikrChain?
    suspend fun saveChain(chain: CustomDhikrChain): Long
    suspend fun deleteChain(id: Int)
    suspend fun recordChainCompletion(id: Int)
    suspend fun seedDefaultTemplatesIfEmpty()
}

@Singleton
class RealCustomDhikrRepository @Inject constructor(
    private val dao: CompanionDao
) : CustomDhikrRepository {

    override fun getAllChainsFlow(): Flow<List<CustomDhikrChain>> {
        return dao.getAllCustomChainsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChainById(id: Int): CustomDhikrChain? = withContext(Dispatchers.IO) {
        dao.getCustomChainById(id)?.toDomain()
    }

    override suspend fun saveChain(chain: CustomDhikrChain): Long = withContext(Dispatchers.IO) {
        val totalCount = chain.items.sumOf { it.targetCount }
        val entity = CustomDhikrChainEntity(
            id = chain.id,
            title = chain.title,
            description = chain.description,
            itemsJson = chain.items.toJsonString(),
            totalCount = totalCount,
            timesCompleted = chain.timesCompleted,
            colorHex = chain.colorHex,
            createdAt = if (chain.createdAt == 0L) System.currentTimeMillis() else chain.createdAt
        )
        dao.insertCustomChain(entity)
    }

    override suspend fun deleteChain(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteCustomChainById(id)
    }

    override suspend fun recordChainCompletion(id: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getCustomChainById(id) ?: return@withContext
        dao.updateCustomChain(existing.copy(timesCompleted = existing.timesCompleted + 1))
    }

    override suspend fun seedDefaultTemplatesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getAllCustomChainsFlow().firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            val templates = listOf(
                CustomDhikrChain(
                    title = "التسبيح والتحميد اليومي (Daily Core)",
                    description = "سلسلة التسبيح والتحميد والتكبير الجامعة بعد الصلوات وفي كل وقت",
                    items = listOf(
                        ChainDhikrItem(id = 1, arabicText = "سُبْحَانَ اللَّهِ", targetCount = 33),
                        ChainDhikrItem(id = 2, arabicText = "الْحَمْدُ لِلَّهِ", targetCount = 33),
                        ChainDhikrItem(id = 3, arabicText = "اللَّهُ أَكْبَرُ", targetCount = 33),
                        ChainDhikrItem(id = 4, arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", targetCount = 1)
                    ),
                    totalCount = 100,
                    colorHex = 0xFF0D9488
                ),
                CustomDhikrChain(
                    title = "ورد الاستغفار والسكينة (Istighfar & Peace)",
                    description = "سلسلة تفريج الهموم والاستغفار والصلاة على النبي ﷺ",
                    items = listOf(
                        ChainDhikrItem(id = 1, arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ", targetCount = 33),
                        ChainDhikrItem(id = 2, arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", targetCount = 33),
                        ChainDhikrItem(id = 3, arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ", targetCount = 33),
                        ChainDhikrItem(id = 4, arabicText = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ", targetCount = 1)
                    ),
                    totalCount = 100,
                    colorHex = 0xFF2563EB
                )
            )
            for (tpl in templates) {
                saveChain(tpl)
            }
        }
    }

    private fun CustomDhikrChainEntity.toDomain(): CustomDhikrChain {
        return CustomDhikrChain(
            id = id,
            title = title,
            description = description,
            items = parseItemsJson(itemsJson),
            totalCount = totalCount,
            timesCompleted = timesCompleted,
            colorHex = colorHex,
            createdAt = createdAt
        )
    }

    private fun parseItemsJson(jsonStr: String): List<ChainDhikrItem> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<ChainDhikrItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChainDhikrItem(
                        id = obj.optInt("id", i + 1),
                        arabicText = obj.optString("text", ""),
                        targetCount = obj.optInt("count", 33)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun List<ChainDhikrItem>.toJsonString(): String {
        val array = JSONArray()
        for (item in this) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.arabicText)
            obj.put("count", item.targetCount)
            array.put(obj)
        }
        return array.toString()
    }
}
