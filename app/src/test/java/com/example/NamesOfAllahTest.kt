package com.example

import com.example.domain.model.NameOfAllah
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NamesOfAllahTest {

    @Test
    fun testNamesOfAllahJsonHas99ItemsWithExplanation() {
        val file = File("src/main/assets/names_of_allah.json")
        assertTrue("names_of_allah.json file must exist", file.exists())

        val jsonString = file.readText(Charsets.UTF_8)
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, NameOfAllah::class.java)
        val adapter = moshi.adapter<List<NameOfAllah>>(type)

        val list = adapter.fromJson(jsonString)
        assertNotNull("Names list should not be null", list)
        assertEquals("Should contain exactly 99 names of Allah", 99, list!!.size)

        val first = list.first()
        assertEquals(1, first.id)
        assertEquals("الله", first.name)
        assertEquals("Allah", first.transliteration)
        assertTrue("Explanation (شرح المعنى) must not be empty", first.explanation.isNotBlank())
        assertTrue("Quranic evidence must not be empty", first.evidence.isNotBlank())
    }
}
