package com.example.data.quran

import org.junit.Assert.*
import org.junit.Test

class QuranStructureTest {

    @Test
    fun testAll30JuzExist() {
        val allJuz = QuranStructureData.ALL_JUZ
        assertEquals(30, allJuz.size)
        allJuz.forEachIndexed { index, juz ->
            assertEquals(index + 1, juz.number)
            assertTrue(juz.nameArabic.isNotBlank())
            assertTrue(juz.nameEnglish.isNotBlank())
            assertTrue(juz.startSurahNumber in 1..114)
            assertTrue(juz.endSurahNumber in 1..114)
            assertTrue(juz.startAyahNumber >= 1)
            assertTrue(juz.endAyahNumber >= 1)
        }
        // Check first and last Juz
        assertEquals("الم", allJuz.first().nameArabic)
        assertEquals(1, allJuz.first().startSurahNumber)
        assertEquals(1, allJuz.first().startAyahNumber)

        assertEquals("عَمَّ يَتَسَاءَلُونَ", allJuz.last().nameArabic)
        assertEquals(78, allJuz.last().startSurahNumber)
        assertEquals(114, allJuz.last().endSurahNumber)
    }

    @Test
    fun testAll15SajdahsExist() {
        val sajdahs = QuranStructureData.ALL_SAJDAHS
        assertEquals(15, sajdahs.size)
        
        // Spot check known Sajdah verses
        val arafSajdah = QuranStructureData.getSajdahForAyah(7, 206)
        assertNotNull(arafSajdah)
        assertEquals(1, arafSajdah?.number)
        assertEquals("الأعراف", arafSajdah?.surahNameArabic)

        val alaqSajdah = QuranStructureData.getSajdahForAyah(96, 19)
        assertNotNull(alaqSajdah)
        assertEquals(15, alaqSajdah?.number)
        assertEquals("العلق", alaqSajdah?.surahNameArabic)

        val nonSajdah = QuranStructureData.getSajdahForAyah(1, 1)
        assertNull(nonSajdah)
    }

    @Test
    fun testGetDivisionMarkerForMilestones() {
        // Juz 1 / Hizb 1 start: Surah 1:1
        val markerJuz1 = QuranStructureData.getDivisionMarkerForAyah(1, 1)
        assertNotNull(markerJuz1)
        assertEquals(1, markerJuz1?.juzNumber)
        assertEquals(1, markerJuz1?.hizbNumber)
        assertEquals(RubType.JUZ_START, markerJuz1?.rubType)

        // Rub' 1 of Hizb 1: Surah 2:26
        val markerRub1 = QuranStructureData.getDivisionMarkerForAyah(2, 26)
        assertNotNull(markerRub1)
        assertEquals(1, markerRub1?.juzNumber)
        assertEquals(1, markerRub1?.hizbNumber)
        assertEquals(RubType.RUB_FIRST, markerRub1?.rubType)

        // Hizb 2 start: Surah 2:75
        val markerHizb2 = QuranStructureData.getDivisionMarkerForAyah(2, 75)
        assertNotNull(markerHizb2)
        assertEquals(1, markerHizb2?.juzNumber)
        assertEquals(2, markerHizb2?.hizbNumber)
        assertEquals(RubType.HIZB_START, markerHizb2?.rubType)

        // Juz 2 / Hizb 3 start: Surah 2:142
        val markerJuz2 = QuranStructureData.getDivisionMarkerForAyah(2, 142)
        assertNotNull(markerJuz2)
        assertEquals(2, markerJuz2?.juzNumber)
        assertEquals(3, markerJuz2?.hizbNumber)
        assertEquals(RubType.JUZ_START, markerJuz2?.rubType)

        // Non-milestone ayah
        val nonMarker = QuranStructureData.getDivisionMarkerForAyah(2, 2)
        assertNull(nonMarker)
    }

    @Test
    fun testGetJuzAndHizbForAyah() {
        // Fatihah 1 -> Juz 1, Hizb 1
        val (juz1, hizb1) = QuranStructureData.getJuzAndHizbForAyah(1, 1)
        assertEquals(1, juz1)
        assertEquals(1, hizb1)

        // Baqarah 142 -> Juz 2, Hizb 3
        val (juz2, hizb3) = QuranStructureData.getJuzAndHizbForAyah(2, 142)
        assertEquals(2, juz2)
        assertEquals(3, hizb3)

        // An-Nas 1 -> Juz 30, Hizb 60
        val (juz30, hizb60) = QuranStructureData.getJuzAndHizbForAyah(114, 1)
        assertEquals(30, juz30)
        assertEquals(60, hizb60)
    }

    @Test
    fun testArabicNumberConverter() {
        assertEquals("١", QuranStructureData.toArabicNumber(1))
        assertEquals("١٥", QuranStructureData.toArabicNumber(15))
        assertEquals("٣٠", QuranStructureData.toArabicNumber(30))
        assertEquals("١١٤", QuranStructureData.toArabicNumber(114))
    }
}
