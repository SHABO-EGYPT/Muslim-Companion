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
        private const val ASSET_PATH = "quran/quran_uthmani.json"
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
        val json = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val batch = mutableListOf<QuranAyahEntity>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val sura = obj.getInt("s")
            val ayah = obj.getInt("a")
            val text = obj.getString("t")
            batch.add(
                QuranAyahEntity(
                    id = "$sura:$ayah",
                    sura = sura,
                    ayah = ayah,
                    arabicText = text
                )
            )
            if (batch.size >= BATCH_SIZE) {
                dao.insertQuranAyahs(batch)
                batch.clear()
            }
        }
        if (batch.isNotEmpty()) {
            dao.insertQuranAyahs(batch)
        }
    }
}
