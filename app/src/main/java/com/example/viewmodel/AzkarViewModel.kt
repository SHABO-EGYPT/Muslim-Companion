package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.data.repository.AzkarRepository
import com.example.data.repository.CompanionRepository
import com.example.data.repository.QuranRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.json.JSONObject
import org.json.JSONArray
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Content
import com.example.data.remote.Part
import com.example.data.remote.GenerationConfig
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import kotlinx.coroutines.delay

// 4. Azkar ViewModel
@HiltViewModel
class AzkarViewModel @Inject constructor(private val repository: CompanionRepository) : ViewModel() {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val azkarCategories: StateFlow<List<AzkarCategory>> = repository.getUserProgressFlow()
        .flatMapLatest { repository.azkarRepository.getAzkarCategoriesFlow(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow<AzkarCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _currentAzkarList = MutableStateFlow<List<DhikrItem>>(emptyList())
    val currentAzkarList = _currentAzkarList.asStateFlow()

    private val _assistantAzkar = MutableStateFlow<List<DhikrItem>>(emptyList())
    val assistantAzkar = _assistantAzkar.asStateFlow()

    private val _assistantLoading = MutableStateFlow(false)
    val assistantLoading = _assistantLoading.asStateFlow()

    private val _assistantError = MutableStateFlow<String?>(null)
    val assistantError = _assistantError.asStateFlow()

    private val _flowIndex = MutableStateFlow(0)
    val flowIndex = _flowIndex.asStateFlow()

    fun selectCategory(category: AzkarCategory) {
        _selectedCategory.value = category
        _flowIndex.value = 0
        viewModelScope.launch {
            repository.azkarRepository.getDhikrItemsFlow(category.id).collect {
                _currentAzkarList.value = it
            }
        }
    }

    fun nextStep() {
        _flowIndex.value++
    }

    fun prevStep() {
        if (_flowIndex.value > 0) _flowIndex.value--
    }

    fun setFlowIndex(index: Int) {
        _flowIndex.value = index
    }

    fun updateAzkarProgress(categoryId: String, itemId: Int, count: Int) {
        viewModelScope.launch {
            val progress = repository.getUserProgressDirect() ?: UserProgressEntity()
            val newProgress = when {
                categoryId.contains("الصباح") -> progress.copy(morningDone = count)
                categoryId.contains("المساء") -> progress.copy(eveningDone = count)
                categoryId.contains("النوم") -> progress.copy(sleepDone = count)
                categoryId.contains("الصلاة") -> progress.copy(afterPrayerDone = count)
                else -> progress
            }
            repository.saveUserProgress(newProgress)
        }
    }

    fun completeAzkarFlow(categoryId: String, total: Int) {
        updateAzkarProgress(categoryId, 0, total)
    }

    data class ChatMessage(
        val id: String = java.util.UUID.randomUUID().toString(),
        val isUser: Boolean,
        val text: String,
        val azkar: List<DhikrItem> = emptyList(),
        val isError: Boolean = false
    )

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    fun searchAzkar(query: String) {
        if (query.isBlank()) return

        val userMessage = ChatMessage(isUser = true, text = query)
        _chatHistory.value = _chatHistory.value + userMessage

        viewModelScope.launch {
            _assistantLoading.value = true
            _assistantError.value = null

            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val systemInstruction = "You are a helpful, compassionate Islamic supplications assistant. Respond to the user's message with comforting and relevant advice or context, and then recommend a list of relevant Azkar (supplications) from Quran and Sunnah. You must respond ONLY with a JSON object matching this schema: { \"message\": \"Your conversational response here\", \"azkar\": [ { \"arabicText\": \"string (in Arabic script)\", \"repetitionCount\": integer, \"virtue\": \"string (English translation/virtue)\", \"category\": \"string\" } ] }"
                    
                    // Build conversation history for context
                    val contents = _chatHistory.value.map { msg ->
                        Content(
                            role = if (msg.isUser) "user" else "model",
                            parts = listOf(Part(text = if (msg.isUser) msg.text else msg.text + "\n(Suggested ${msg.azkar.size} azkar)"))
                        )
                    }

                    val request = GenerateContentRequest(
                        contents = contents,
                        systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
                        generationConfig = GenerationConfig(responseMimeType = "application/json")
                    )
                    
                    val apiResponse = GeminiApiService.instance.generateContent(apiKey, request)
                    val jsonText = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!jsonText.isNullOrBlank()) {
                        val responseObj = JSONObject(jsonText)
                        val messageText = responseObj.optString("message", "Here are some supplications that might help:")
                        val azkarArray = responseObj.optJSONArray("azkar") ?: JSONArray()
                        
                        val recommendedAzkar = mutableListOf<DhikrItem>()
                        for (idx in 0 until azkarArray.length()) {
                            val item = azkarArray.getJSONObject(idx)
                            recommendedAzkar.add(
                                DhikrItem(
                                    id = idx + 1,
                                    arabicText = item.optString("arabicText", ""),
                                    englishTranslation = item.optString("virtue", ""),
                                    translit = "",
                                    repeatTarget = item.optInt("repetitionCount", 1)
                                )
                            )
                        }
                        
                        _chatHistory.value = _chatHistory.value + ChatMessage(
                            isUser = false,
                            text = messageText,
                            azkar = recommendedAzkar
                        )
                        _assistantLoading.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Local fallback search
            try {
                repository.azkarRepository.searchDhikrItems(query).collect { results ->
                    if (results.isNotEmpty()) {
                        _chatHistory.value = _chatHistory.value + ChatMessage(
                            isUser = false,
                            text = "Here are some supplications I found locally:",
                            azkar = results
                        )
                    } else {
                        _chatHistory.value = _chatHistory.value + ChatMessage(
                            isUser = false,
                            text = "I couldn't find any specific supplications for that locally. Please try different keywords."
                        )
                    }
                }
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    isUser = false,
                    text = "Sorry, an error occurred while searching: ${e.message}",
                    isError = true
                )
            } finally {
                _assistantLoading.value = false
            }
        }
    }

    val userProgress: StateFlow<UserProgressEntity> = repository.getUserProgressFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgressEntity()
        )

    val settings: StateFlow<AppSettingEntity> = repository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingEntity()
        )
}
