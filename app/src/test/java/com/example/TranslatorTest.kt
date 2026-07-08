package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import com.example.ui.Translator

class TranslatorTest {

    @Test
    fun `translate returns English for English language`() {
        assertEquals("Home", Translator.translate("home", "English"))
        assertEquals("Quran", Translator.translate("quran", "English"))
        assertEquals("Prayer", Translator.translate("prayer", "English"))
    }

    @Test
    fun `translate returns Arabic for Arabic language`() {
        assertEquals("الرئيسية", Translator.translate("home", "Arabic"))
        assertEquals("القرآن", Translator.translate("quran", "Arabic"))
        assertEquals("الصلاة", Translator.translate("prayer", "Arabic"))
    }

    @Test
    fun `translate falls back to English for unknown language`() {
        assertEquals("Home", Translator.translate("home", "French"))
    }

    @Test
    fun `translate returns key itself when key not found`() {
        assertEquals("nonexistent_key", Translator.translate("nonexistent_key", "English"))
    }

    @Test
    fun `translate handles all defined keys without returning the key`() {
        val englishKeys = listOf(
            "home", "quran", "azkar", "prayer", "profile", "settings",
            "notifications", "prayer_notifications", "dark_theme", "language",
            "reciter", "prayer_calculation", "text_size", "next_prayer",
            "until_adhan", "digital_tasbih", "morning_azkar", "evening_azkar",
            "sound_type", "silent", "subtle", "full_adhan"
        )
        englishKeys.forEach { key ->
            val result = Translator.translate(key, "English")
            assertNotEquals("Key '$key' should not fall back to itself in English", key, result)
        }
    }

    @Test
    fun `translate all Arabic keys are non-empty and different from English`() {
        val keys = listOf(
            "home", "quran", "azkar", "prayer", "profile", "settings"
        )
        keys.forEach { key ->
            val english = Translator.translate(key, "English")
            val arabic = Translator.translate(key, "Arabic")
            assert(arabic.isNotBlank()) { "Arabic translation for '$key' should not be blank" }
            assert(arabic != english) { "Arabic translation for '$key' should differ from English" }
        }
    }
}
