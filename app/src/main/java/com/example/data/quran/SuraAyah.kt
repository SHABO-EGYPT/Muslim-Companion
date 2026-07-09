package com.example.data.quran

/**
 * Identifies a specific ayah by sura (1-indexed) and ayah (1-indexed).
 * Mirrors the Quran Android SuraAyah data class.
 */
data class SuraAyah(val sura: Int, val ayah: Int)
