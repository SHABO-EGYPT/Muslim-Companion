package com.example.data.mapper

object MethodMapper {
    private val methodMap = mapOf(
        "University of Islamic Sciences, Karachi" to 1,
        "Islamic Society of North America (ISNA)" to 2,
        "Muslim World League" to 3,
        "Umm Al-Qura University, Makkah" to 4,
        "Egyptian General Authority" to 5,
        "Institute of Geophysics, University of Tehran" to 7,
        "Gulf Region" to 8,
        "Kuwait" to 9,
        "Qatar" to 10,
        "Majlis Ugama Islam Singapura, Singapore" to 11,
        "Union Organization Islamic de France" to 12,
        "Diyanet İşleri Başkanlığı, Turkey" to 13,
        "Spiritual Administration of Muslims of Russia" to 14,
        "Moonsighting Committee Worldwide" to 15
    )

    fun getMethodId(displayName: String): Int = methodMap[displayName] ?: 5
}
