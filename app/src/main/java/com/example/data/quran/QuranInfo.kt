package com.example.data.quran

/**
 * Business logic over [QuranDataSource] arrays.
 * Mirrors the Quran Android QuranInfo class (section 4 of reference doc).
 * All methods are O(1) or O(30)/O(114) at most — no network, no DB.
 */
class QuranInfo(private val dataSource: QuranDataSource = MadaniDataSource) {

    val numberOfPages: Int = dataSource.numberOfPages
    val quarters: Array<SuraAyah> = dataSource.quartersArray

    private val suraPageStart = dataSource.pageForSuraArray       // [114]
    private val pageSuraStart = dataSource.suraForPageArray        // [604]
    private val pageAyahStart = dataSource.ayahForPageArray        // [604]
    private val juzPageStart  = dataSource.pageForJuzArray         // [30]
    private val juzPageOverride = dataSource.juzDisplayPageArrayOverride
    private val suraNumAyahs  = dataSource.numberOfAyahsForSuraArray // [114]
    private val suraIsMakki   = dataSource.isMakkiBySuraArray      // [114]
    private val pageRub3Start = dataSource.quarterStartByPage      // [604]

    // ── Page ↔ Sura / Ayah ────────────────────────────────────

    /** Returns the first page of [sura] (1-indexed). O(1). */
    fun getPageNumberForSura(sura: Int): Int = suraPageStart[sura - 1]

    /** Returns the sura that **starts** on [page] (from the page→sura array). O(1). */
    fun getSuraOnPage(page: Int): Int = pageSuraStart[page - 1]

    /** Returns the first ayah shown on [page]. O(1). */
    fun getFirstAyahOnPage(page: Int): Int = pageAyahStart[page - 1]

    /**
     * Returns the page that contains sura [sura], ayah [ayah].
     * Walks forward from the sura's first page. O(pages per sura).
     */
    fun getPageFromSuraAyah(sura: Int, ayah: Int): Int {
        var index = suraPageStart[sura - 1] - 1
        while (index < numberOfPages - 1) {
            val nextSura = pageSuraStart[index + 1]
            val nextAyah = pageAyahStart[index + 1]
            if (nextSura > sura || (nextSura == sura && nextAyah > ayah)) break
            index++
        }
        return index + 1
    }

    /** Returns the sura number that [page] belongs to (last sura to have started). O(114). */
    fun getSuraNumberFromPage(page: Int): Int {
        for (i in suraPageStart.indices.reversed()) {
            if (suraPageStart[i] <= page) return i + 1
        }
        return 1
    }

    // ── Juz' ────────────────────────────────────────────────

    /** Returns the first page of juz [juz] (1-indexed). O(1). */
    fun getStartingPageForJuz(juz: Int): Int = juzPageStart[juz - 1]

    /** Returns the juz' number that [page] belongs to. O(30). */
    fun getJuzFromPage(page: Int): Int {
        for (i in juzPageStart.indices) {
            if (juzPageStart[i] > page) return i  // i is 0-indexed → juz i
        }
        return 30
    }

    /**
     * Returns the juz' to display at the top of [page], applying the two overrides
     * (page 121 → juz 6, page 201 → juz 10).
     */
    fun getJuzForDisplayFromPage(page: Int): Int =
        juzPageOverride[page] ?: getJuzFromPage(page)

    // ── Ayah ID (global) ────────────────────────────────────

    /** Returns a global 1-based ayah ID across the entire Quran. O(114). */
    fun getAyahId(sura: Int, ayah: Int): Int {
        var id = 0
        for (i in 0 until sura - 1) id += suraNumAyahs[i]
        return id + ayah
    }

    // ── Sura info ────────────────────────────────────────────

    /** Returns the number of ayahs in [sura]. O(1). -1 if out of bounds. */
    fun getNumberOfAyahs(sura: Int): Int =
        if (sura in 1..114) suraNumAyahs[sura - 1] else -1

    /** Total ayahs in the Quran (6236). */
    fun getNumberOfAyahsInQuran(): Int = suraNumAyahs.sum()

    /** Returns true if [sura] was revealed in Makkah. O(1). */
    fun isMakki(sura: Int): Boolean = suraIsMakki[sura - 1]

    /** Returns hizb quarter index on [page]; -1 if none starts on this page. O(1). */
    fun getRub3FromPage(page: Int): Int = pageRub3Start[page - 1]

    /** Returns all suras whose first page is exactly [page]. */
    fun getListOfSurahWithStartingOnPage(page: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (i in suraPageStart.indices) {
            if (suraPageStart[i] == page) result.add(i + 1)
            else if (suraPageStart[i] > page) break
        }
        return result
    }
}
