package com.example.data.quran

/**
 * Interface describing all structural data arrays needed to navigate the Quran.
 * Mirrors the Quran Android QuranDataSource interface (section 2.1 of reference doc).
 */
interface QuranDataSource {
    val numberOfPages: Int
    /** [114] — starting page for each sura (index 0 = sura 1) */
    val pageForSuraArray: IntArray
    /** [604] — sura number for each page (index 0 = page 1) */
    val suraForPageArray: IntArray
    /** [604] — starting ayah for each page (index 0 = page 1) */
    val ayahForPageArray: IntArray
    /** [30] — starting page for each juz */
    val pageForJuzArray: IntArray
    /** page → override display juz (currently pages 121→6, 201→10) */
    val juzDisplayPageArrayOverride: Map<Int, Int>
    /** [114] — ayah count per sura */
    val numberOfAyahsForSuraArray: IntArray
    /** [114] — true = Makki, false = Madani */
    val isMakkiBySuraArray: BooleanArray
    /** [604] — hizb quarter index per page (-1 = none) */
    val quarterStartByPage: IntArray
    /** [240] — 240 hizb quarter positions (60 hizbs × 4 quarters) */
    val quartersArray: Array<SuraAyah>
    /** Empty for Madani layout */
    val manzilPageArray: Array<Int>
    val haveSidelines: Boolean
    val pagesToSkip: Int
}
