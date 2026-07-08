package com.example.fake

import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.domain.model.AzkarCategory
import com.example.domain.model.DhikrItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake AzkarRepository for unit testing ViewModels.
 * All data is in-memory and fully controllable from tests.
 */
class FakeAzkarRepository : AzkarRepository {

    private val _categories = MutableStateFlow<List<AzkarCategory>>(emptyList())
    private val _dhikrItems = mutableMapOf<String, List<DhikrItem>>()
    private val _tasbihPhrases = MutableStateFlow<List<DhikrItem>>(emptyList())

    fun setCategories(categories: List<AzkarCategory>) {
        _categories.value = categories
    }

    fun setDhikrItems(categoryId: String, items: List<DhikrItem>) {
        _dhikrItems[categoryId] = items
    }

    fun setTasbihPhrases(phrases: List<DhikrItem>) {
        _tasbihPhrases.value = phrases
    }

    override fun getAzkarCategoriesFlow(progress: UserProgressEntity): Flow<List<AzkarCategory>> {
        return _categories
    }

    override fun getDhikrItemsFlow(categoryId: String): Flow<List<DhikrItem>> {
        return MutableStateFlow(_dhikrItems[categoryId] ?: emptyList())
    }

    override fun getTasbihPhrasesFlow(): Flow<List<DhikrItem>> {
        return _tasbihPhrases
    }

    override fun searchDhikrItems(query: String): Flow<List<DhikrItem>> {
        val allItems = _dhikrItems.values.flatten().distinctBy { it.id }
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            allItems.filter { 
                it.englishTranslation.contains(query, ignoreCase = true) || 
                it.translit.contains(query, ignoreCase = true) || 
                it.arabicText.contains(query, ignoreCase = true)
            }
        }
        return kotlinx.coroutines.flow.flowOf(filtered)
    }
}
