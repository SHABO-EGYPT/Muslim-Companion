package com.example.data.quran

import android.content.Context
import com.example.data.local.CompanionDao
import com.example.data.local.QuranAyahEntity
import org.json.JSONArray

/**
 * Reads the bundled Arabic Quran text from assets on first launch and seeds
 * the local Room database. Subsequent launches are instant (DB already populated).
 *
 * Asset format (quran/quran_uthmani.json):
 *   [{"s":1,"a":1,"t":"بِسْمِ ٱللَّهِ ..."}, ...]
 */
class QuranAssetLoader(
    private val context: Context,
    private val dao: CompanionDao
) {
    companion object {
        private const val ASSET_PATH = "quran/quran_en.json"
        private const val TOTAL_AYAHS = 6236
        private const val BATCH_SIZE = 500
    }

    /** Returns true if the quran_ayahs table is already fully seeded. */
    suspend fun isSeeded(): Boolean = dao.getQuranAyahCount() >= TOTAL_AYAHS

    /**
     * Parses the bundled JSON asset and inserts all 6236 ayahs into Room.
     * Should be called once, on a background coroutine.
     */
    suspend fun seedDatabase() {
        val json = try {
            context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            context.assets.open("quran/quran_uthmani.json").bufferedReader().use { it.readText() }
        }
        
        val array = JSONArray(json)
        val quranBatch = mutableListOf<QuranAyahEntity>()
        val cachedBatch = mutableListOf<com.example.data.local.CachedAyahEntity>()
        val defaultReciter = "ar.alafasy"

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val sura = obj.optInt("sura", obj.optInt("s"))
            val ayah = obj.optInt("ayah", obj.optInt("a"))
            val text = obj.optString("arabicText", obj.optString("t"))
            
            quranBatch.add(
                QuranAyahEntity(
                    id = "$sura:$ayah",
                    sura = sura,
                    ayah = ayah,
                    arabicText = text
                )
            )
            
            if (obj.has("translation") && obj.has("audioUrl")) {
                cachedBatch.add(
                    com.example.data.local.CachedAyahEntity(
                        id = "${sura}_${ayah}_${defaultReciter}",
                        surahNumber = sura,
                        numberInSurah = ayah,
                        reciter = defaultReciter,
                        arabicText = text,
                        translation = obj.getString("translation"),
                        audioUrl = obj.getString("audioUrl")
                    )
                )
            }

            if (quranBatch.size >= BATCH_SIZE) {
                dao.insertQuranAyahs(quranBatch)
                quranBatch.clear()
            }
            if (cachedBatch.size >= BATCH_SIZE) {
                dao.insertCachedAyahs(cachedBatch)
                cachedBatch.clear()
            }
        }
        if (quranBatch.isNotEmpty()) {
            dao.insertQuranAyahs(quranBatch)
        }
        if (cachedBatch.isNotEmpty()) {
            dao.insertCachedAyahs(cachedBatch)
        }
    }
}
