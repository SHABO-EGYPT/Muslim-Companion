package com.example.domain

/**
 * Pure mapper for prayer calculation methods.
 * Maps display names to AlAdhan API method IDs.
 */
object CalculationMethodMapper {

    data class Method(
        val id: Int,
        val displayName: String
    )

    val allMethods: List<Method> = listOf(
        Method(1, "University of Islamic Sciences, Karachi"),
        Method(2, "Islamic Society of North America (ISNA)"),
        Method(3, "Muslim World League"),
        Method(4, "Umm Al-Qura University, Makkah"),
        Method(5, "Egyptian General Authority"),
        Method(7, "Institute of Geophysics, University of Tehran"),
        Method(8, "Gulf Region"),
        Method(9, "Kuwait"),
        Method(10, "Qatar"),
        Method(11, "Majlis Ugama Islam Singapura, Singapore"),
        Method(12, "Union Organization Islamic de France"),
        Method(13, "Diyanet İşleri Başkanlığı, Turkey"),
        Method(14, "Spiritual Administration of Muslims of Russia"),
        Method(15, "Moonsighting Committee Worldwide")
    )

    private val methodMap: Map<String, Int> = allMethods.associate { it.displayName to it.id }

    fun getMethodId(displayName: String): Int {
        return methodMap[displayName] ?: 5 // Default to Egyptian General Authority
    }

    fun getDefaultMethod(): Method = Method(5, "Egyptian General Authority")
}
