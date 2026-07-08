package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationMethodMapperTest {

    @Test
    fun `getMethodId returns 5 for Egyptian General Authority`() {
        assertEquals(5, CalculationMethodMapper.getMethodId("Egyptian General Authority"))
    }

    @Test
    fun `getMethodId returns 1 for Karachi`() {
        assertEquals(1, CalculationMethodMapper.getMethodId("University of Islamic Sciences, Karachi"))
    }

    @Test
    fun `getMethodId returns 2 for ISNA`() {
        assertEquals(2, CalculationMethodMapper.getMethodId("Islamic Society of North America (ISNA)"))
    }

    @Test
    fun `getMethodId returns 3 for Muslim World League`() {
        assertEquals(3, CalculationMethodMapper.getMethodId("Muslim World League"))
    }

    @Test
    fun `getMethodId returns 4 for Umm Al-Qura`() {
        assertEquals(4, CalculationMethodMapper.getMethodId("Umm Al-Qura University, Makkah"))
    }

    @Test
    fun `getMethodId returns 7 for Tehran`() {
        assertEquals(7, CalculationMethodMapper.getMethodId("Institute of Geophysics, University of Tehran"))
    }

    @Test
    fun `getMethodId returns 8 for Gulf Region`() {
        assertEquals(8, CalculationMethodMapper.getMethodId("Gulf Region"))
    }

    @Test
    fun `getMethodId returns 9 for Kuwait`() {
        assertEquals(9, CalculationMethodMapper.getMethodId("Kuwait"))
    }

    @Test
    fun `getMethodId returns 10 for Qatar`() {
        assertEquals(10, CalculationMethodMapper.getMethodId("Qatar"))
    }

    @Test
    fun `getMethodId returns 11 for Singapore`() {
        assertEquals(11, CalculationMethodMapper.getMethodId("Majlis Ugama Islam Singapura, Singapore"))
    }

    @Test
    fun `getMethodId returns 12 for France`() {
        assertEquals(12, CalculationMethodMapper.getMethodId("Union Organization Islamic de France"))
    }

    @Test
    fun `getMethodId returns 13 for Turkey`() {
        assertEquals(13, CalculationMethodMapper.getMethodId("Diyanet İşleri Başkanlığı, Turkey"))
    }

    @Test
    fun `getMethodId returns 14 for Russia`() {
        assertEquals(14, CalculationMethodMapper.getMethodId("Spiritual Administration of Muslims of Russia"))
    }

    @Test
    fun `getMethodId returns 15 for Moonsighting`() {
        assertEquals(15, CalculationMethodMapper.getMethodId("Moonsighting Committee Worldwide"))
    }

    @Test
    fun `getMethodId returns default 5 for unknown method`() {
        assertEquals(5, CalculationMethodMapper.getMethodId("Unknown Method"))
    }

    @Test
    fun `allMethods contains exactly 14 entries`() {
        assertEquals(14, CalculationMethodMapper.allMethods.size)
    }

    @Test
    fun `getDefaultMethod returns Egyptian General Authority`() {
        val default = CalculationMethodMapper.getDefaultMethod()
        assertEquals(5, default.id)
        assertEquals("Egyptian General Authority", default.displayName)
    }
}
