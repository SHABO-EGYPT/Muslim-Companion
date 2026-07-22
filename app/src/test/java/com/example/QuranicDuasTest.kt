package com.example

import com.example.domain.model.QuranicDua
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class QuranicDuasTest {

    @Test
    fun testQuranicDuasJsonHasItemsWithValidSurahAndAyah() {
        val file = File("src/main/assets/quranic_duas.json")
        assertTrue("quranic_duas.json file must exist", file.exists())

        val jsonString = file.readText(Charsets.UTF_8)
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, QuranicDua::class.java)
        val adapter = moshi.adapter<List<QuranicDua>>(type)

        val list = adapter.fromJson(jsonString)
        assertNotNull("Quranic Duas list should not be null", list)
        assertTrue("Should contain at least 40 Quranic supplications", list!!.size >= 40)

        val first = list.first()
        assertEquals(1, first.id)
        assertEquals("البقرة", first.surah)
        assertEquals(201, first.ayah)
        assertTrue("Dua text must not be empty", first.text.isNotBlank())
    }
}
