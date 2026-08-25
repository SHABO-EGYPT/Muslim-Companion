package com.example.fake

import com.example.data.repository.CustomDhikrRepository
import com.example.domain.model.ChainDhikrItem
import com.example.domain.model.CustomDhikrChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCustomDhikrRepository : CustomDhikrRepository {

    private val _chains = MutableStateFlow<List<CustomDhikrChain>>(emptyList())
    private var nextId = 1

    override fun getAllChainsFlow(): Flow<List<CustomDhikrChain>> = _chains.asStateFlow()

    override suspend fun getChainById(id: Int): CustomDhikrChain? {
        return _chains.value.find { it.id == id }
    }

    override suspend fun saveChain(chain: CustomDhikrChain): Long {
        val current = _chains.value.toMutableList()
        val assignedId = if (chain.id == 0) nextId++ else chain.id
        val updated = chain.copy(id = assignedId)
        val existingIndex = current.indexOfFirst { it.id == assignedId }
        if (existingIndex >= 0) {
            current[existingIndex] = updated
        } else {
            current.add(updated)
        }
        _chains.value = current
        return assignedId.toLong()
    }

    override suspend fun deleteChain(id: Int) {
        _chains.value = _chains.value.filterNot { it.id == id }
    }

    override suspend fun recordChainCompletion(id: Int) {
        val current = _chains.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = current[index].copy(timesCompleted = current[index].timesCompleted + 1)
            _chains.value = current
        }
    }

    override suspend fun seedDefaultTemplatesIfEmpty() {
        if (_chains.value.isEmpty()) {
            saveChain(
                CustomDhikrChain(
                    id = 1,
                    title = "Daily Core",
                    items = listOf(
                        ChainDhikrItem(1, "سُبْحَانَ اللَّهِ", 33),
                        ChainDhikrItem(2, "الْحَمْدُ لِلَّهِ", 33),
                        ChainDhikrItem(3, "اللَّهُ أَكْبَرُ", 33)
                    ),
                    totalCount = 99
                )
            )
        }
    }
}
