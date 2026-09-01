package com.example.data.quran

/**
 * Metadata and helper functions for Quran structural divisions:
 * - 30 Juz' (الأجزاء)
 * - 60 Hizbs (الأحزاب)
 * - 240 Quarters (أرباع القرآن)
 * - 15 Sujud al-Tilawah positions (سجدات التلاوة)
 */

enum class RubType {
    JUZ_START,      // Start of a Juz and start of an odd Hizb (e.g. Hizb 1, 3, 5...)
    HIZB_START,     // Start of an even Hizb inside the Juz (e.g. Hizb 2, 4, 6...)
    RUB_FIRST,      // 1st Quarter: ربع الحزب (Quarter 1 of 4 in this Hizb)
    NISF_HIZB,      // 2nd Quarter: نصف الحزب (Quarter 2 of 4 in this Hizb)
    THULUTH_HIZB    // 3rd Quarter: ثلاثة أرباع الحزب (Quarter 3 of 4 in this Hizb)
}

data class QuranDivisionMarker(
    val juzNumber: Int,
    val hizbNumber: Int,
    val rubIndex: Int,          // 0..239
    val rubType: RubType,
    val titleArabic: String,
    val titleEnglish: String,
    val suraAyahTextArabic: String = ""
)

data class JuzInfo(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val startSurahNumber: Int,
    val startAyahNumber: Int,
    val startSurahNameArabic: String,
    val startSurahNameEnglish: String,
    val endSurahNumber: Int,
    val endAyahNumber: Int,
    val endSurahNameArabic: String,
    val endSurahNameEnglish: String
)

data class SajdahInfo(
    val number: Int,             // 1..15
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val isObligatory: Boolean = false
)

object QuranStructureData {

    private val quarters: Array<SuraAyah> = MadaniDataSource.quartersArray

    // Lookup map: (surah, ayah) -> quarterIndex (0..239)
    private val quarterMap: Map<Pair<Int, Int>, Int> = quarters.mapIndexed { index, sa ->
        (sa.sura to sa.ayah) to index
    }.toMap()

    // ── 15 Sajdah Positions ──────────────────────────────────────────
    val ALL_SAJDAHS: List<SajdahInfo> = listOf(
        SajdahInfo(1, 7, 206, "الأعراف", "Al-A'raf"),
        SajdahInfo(2, 13, 15, "الرعد", "Ar-Ra'd"),
        SajdahInfo(3, 16, 50, "النحل", "An-Nahl"),
        SajdahInfo(4, 17, 109, "الإسراء", "Al-Isra'"),
        SajdahInfo(5, 19, 58, "مريم", "Maryam"),
        SajdahInfo(6, 22, 18, "الحج", "Al-Hajj (1)"),
        SajdahInfo(7, 22, 77, "الحج", "Al-Hajj (2)"),
        SajdahInfo(8, 25, 60, "الفرقان", "Al-Furqan"),
        SajdahInfo(9, 27, 26, "النمل", "An-Naml"),
        SajdahInfo(10, 32, 15, "السجدة", "As-Sajdah"),
        SajdahInfo(11, 38, 24, "ص", "Sad"),
        SajdahInfo(12, 41, 38, "فصلت", "Fussilat"),
        SajdahInfo(13, 53, 62, "النجم", "An-Najm"),
        SajdahInfo(14, 84, 21, "الانشقاق", "Al-Inshiqaq"),
        SajdahInfo(15, 96, 19, "العلق", "Al-Alaq")
    )

    private val sajdahMap: Map<Pair<Int, Int>, SajdahInfo> = ALL_SAJDAHS.associateBy {
        it.surahNumber to it.ayahNumber
    }

    /** Returns Sajdah metadata if the given Ayah contains Sujud al-Tilawah. */
    fun getSajdahForAyah(surah: Int, ayah: Int): SajdahInfo? = sajdahMap[surah to ayah]

    // ── 30 Juz' Metadata ─────────────────────────────────────────────
    val ALL_JUZ: List<JuzInfo> = listOf(
        JuzInfo(1, "الم", "Alif Lam Meem", 1, 1, "الفاتحة", "Al-Fatihah", 2, 141, "البقرة", "Al-Baqarah"),
        JuzInfo(2, "سَيَقُولُ", "Sayaqool", 2, 142, "البقرة", "Al-Baqarah", 2, 252, "البقرة", "Al-Baqarah"),
        JuzInfo(3, "تِلْكَ الرُّسُلُ", "Tilka ar-Rusul", 2, 253, "البقرة", "Al-Baqarah", 3, 92, "آل عمران", "Ali 'Imran"),
        JuzInfo(4, "لَنْ تَنَالُوا", "Lan Tanaaloo", 3, 93, "آل عمران", "Ali 'Imran", 4, 23, "النساء", "An-Nisa"),
        JuzInfo(5, "وَالْمُحْصَنَاتُ", "Wal-Muhsanat", 4, 24, "النساء", "An-Nisa", 4, 147, "النساء", "An-Nisa"),
        JuzInfo(6, "لَا يُحِبُّ اللَّهُ", "La Yuhibbu Allah", 4, 148, "النساء", "An-Nisa", 5, 81, "المائدة", "Al-Ma'idah"),
        JuzInfo(7, "وَإِذَا سَمِعُوا", "Wa Iza Sami'oo", 5, 82, "المائدة", "Al-Ma'idah", 6, 110, "الأنعام", "Al-An'am"),
        JuzInfo(8, "وَلَوْ أَنَّنَا", "Wa Law Annana", 6, 111, "الأنعام", "Al-An'am", 7, 87, "الأعراف", "Al-A'raf"),
        JuzInfo(9, "قَالَ الْمَلَأُ", "Qal al-Mala'", 7, 88, "الأعراف", "Al-A'raf", 8, 40, "الأنفال", "Al-Anfal"),
        JuzInfo(10, "وَاعْلَمُوا", "Wa'lamoo", 8, 41, "الأنفال", "Al-Anfal", 9, 92, "التوبة", "At-Tawbah"),
        JuzInfo(11, "يَعْتَذِرُونَ", "Ya'taziroon", 9, 93, "التوبة", "At-Tawbah", 11, 5, "هود", "Hud"),
        JuzInfo(12, "وَمَا مِنْ دَابَّةٍ", "Wa Ma Min Dabbah", 11, 6, "هود", "Hud", 12, 52, "يوسف", "Yusuf"),
        JuzInfo(13, "وَمَا أُبَرِّئُ", "Wa Ma Ubarri'u", 12, 53, "يوسف", "Yusuf", 14, 52, "إبراهيم", "Ibrahim"),
        JuzInfo(14, "رُبَمَا", "Rubama", 15, 1, "الحجر", "Al-Hijr", 16, 128, "النحل", "An-Nahl"),
        JuzInfo(15, "سُبْحَانَ الَّذِي", "Subhana Alladhi", 17, 1, "الإسراء", "Al-Isra'", 18, 74, "الكهف", "Al-Kahf"),
        JuzInfo(16, "قَالَ أَلَمْ", "Qal Alam", 18, 75, "الكهف", "Al-Kahf", 20, 135, "طه", "Ta-Ha"),
        JuzInfo(17, "اقْتَرَبَ لِلنَّاسِ", "Iqtaraba lin-Nas", 21, 1, "الأنبياء", "Al-Anbiya", 22, 78, "الحج", "Al-Hajj"),
        JuzInfo(18, "قَدْ أَفْلَحَ", "Qad Aflaha", 23, 1, "المؤمنون", "Al-Mu'minun", 25, 20, "الفرقان", "Al-Furqan"),
        JuzInfo(19, "وَقَالَ الَّذِينَ", "Wa Qal alladhina", 25, 21, "الفرقان", "Al-Furqan", 27, 55, "النمل", "An-Naml"),
        JuzInfo(20, "أَمَّنْ خَلَقَ", "Amman Khalaqa", 27, 56, "النمل", "An-Naml", 29, 45, "العنكبوت", "Al-'Ankabut"),
        JuzInfo(21, "اتْلُ مَا أُوحِيَ", "Utlu Ma Oohiya", 29, 46, "العنكبوت", "Al-'Ankabut", 33, 30, "الأحزاب", "Al-Ahzab"),
        JuzInfo(22, "وَمَنْ يَقْنُتْ", "Wa Man Yaqnut", 33, 31, "الأحزاب", "Al-Ahzab", 36, 27, "يس", "Ya-Sin"),
        JuzInfo(23, "وَمَا أَنْزَلْنَا", "Wa Ma Anzalna", 36, 28, "يس", "Ya-Sin", 39, 31, "الزمر", "Az-Zumar"),
        JuzInfo(24, "فَمَنْ أَظْلَمُ", "Fa Man Azlam", 39, 32, "الزمر", "Az-Zumar", 41, 46, "فصلت", "Fussilat"),
        JuzInfo(25, "إِلَيْهِ يُرَدُّ", "Ilayhi Yuraddu", 41, 47, "فصلت", "Fussilat", 45, 37, "الجاثية", "Al-Jathiyah"),
        JuzInfo(26, "حم", "Ha Meem", 46, 1, "الأحقاف", "Al-Ahqaf", 51, 30, "الذاريات", "Adh-Dhariyat"),
        JuzInfo(27, "قَالَ فَمَا خَطْبُكُمْ", "Qala Fama Khatbukum", 51, 31, "الذاريات", "Adh-Dhariyat", 57, 29, "الحديد", "Al-Hadid"),
        JuzInfo(28, "قَدْ سَمِعَ اللَّهُ", "Qad Sami'a Allah", 58, 1, "المجادلة", "Al-Mujadilah", 66, 12, "التحريم", "At-Tahrim"),
        JuzInfo(29, "تَبَارَكَ الَّذِي", "Tabaraka Alladhi", 67, 1, "الملك", "Al-Mulk", 77, 50, "المرسلات", "Al-Mursalat"),
        JuzInfo(30, "عَمَّ يَتَسَاءَلُونَ", "'Amma Yatasa'aloon", 78, 1, "النبأ", "An-Naba", 114, 6, "الناس", "An-Nas")
    )

    private val ARABIC_DIGITS = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

    private val ARABIC_ORDINALS = arrayOf(
        "", "الأول", "الثاني", "الثالث", "الرابع", "الخامس",
        "السادس", "السابع", "الثامن", "التاسع", "العاشر",
        "الحادي عشر", "الثاني عشر", "الثالث عشر", "الرابع عشر", "الخامس عشر",
        "السادس عشر", "السابع عشر", "الثامن عشر", "التاسع عشر", "العشرون",
        "الحادي والعشرون", "الثاني والعشرون", "الثالث والعشرون", "الرابع والعشرون", "الخامس والعشرون",
        "السادس والعشرون", "السابع والعشرون", "الثامن والعشرون", "التاسع والعشرون", "الثلاثون"
    )

    fun toArabicOrdinal(number: Int): String {
        return if (number in 1..30) ARABIC_ORDINALS[number] else toArabicNumber(number)
    }

    fun toArabicNumber(number: Int): String {
        return number.toString().map { c ->
            if (c in '0'..'9') ARABIC_DIGITS[c - '0'] else c.toString()
        }.joinToString("")
    }

    /**
     * Checks if this (surah, ayah) starts a new Juz, Hizb, or Rub.
     * Returns division marker data if so, or null otherwise.
     */
    fun getDivisionMarkerForAyah(surah: Int, ayah: Int): QuranDivisionMarker? {
        val quarterIndex = quarterMap[surah to ayah] ?: return null
        
        // quarterIndex is 0..239
        val hizbNumber = (quarterIndex / 4) + 1         // 1..60
        val juzNumber = (quarterIndex / 8) + 1          // 1..30
        val rubInHizb = quarterIndex % 4               // 0, 1, 2, 3

        val rubType = when {
            quarterIndex % 8 == 0 -> RubType.JUZ_START
            rubInHizb == 0 -> RubType.HIZB_START
            rubInHizb == 1 -> RubType.RUB_FIRST
            rubInHizb == 2 -> RubType.NISF_HIZB
            else -> RubType.THULUTH_HIZB
        }

        val (titleAr, titleEn) = when (rubType) {
            RubType.JUZ_START -> {
                "الجزء ${toArabicNumber(juzNumber)} · الحزب ${toArabicNumber(hizbNumber)}" to "Juz $juzNumber · Hizb $hizbNumber"
            }
            RubType.HIZB_START -> {
                "الحزب ${toArabicNumber(hizbNumber)}" to "Hizb $hizbNumber"
            }
            RubType.RUB_FIRST -> {
                "ربع الحزب ${toArabicNumber(hizbNumber)}" to "Rub' Hizb $hizbNumber"
            }
            RubType.NISF_HIZB -> {
                "نصف الحزب ${toArabicNumber(hizbNumber)}" to "Half Hizb $hizbNumber"
            }
            RubType.THULUTH_HIZB -> {
                "ثلاثة أرباع الحزب ${toArabicNumber(hizbNumber)}" to "3/4 Hizb $hizbNumber"
            }
        }

        return QuranDivisionMarker(
            juzNumber = juzNumber,
            hizbNumber = hizbNumber,
            rubIndex = quarterIndex,
            rubType = rubType,
            titleArabic = titleAr,
            titleEnglish = titleEn
        )
    }

    /**
     * Finds the Juz (1..30) and Hizb (1..60) for any Ayah in the Quran.
     */
    fun getJuzAndHizbForAyah(surah: Int, ayah: Int): Pair<Int, Int> {
        var lastQuarterIndex = 0
        for (i in quarters.indices) {
            val q = quarters[i]
            if (q.sura < surah || (q.sura == surah && q.ayah <= ayah)) {
                lastQuarterIndex = i
            } else {
                break
            }
        }
        val hizb = (lastQuarterIndex / 4) + 1
        val juz = (lastQuarterIndex / 8) + 1
        return juz to hizb
    }
}
