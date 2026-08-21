package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteEntity
import com.example.data.local.ProjectEntity
import com.example.data.repository.SocialContentRepository
import com.example.domain.model.*
import com.example.engine.ContentQualityEngine
import com.example.network.GeminiApiException
import com.example.network.GeminiApiKeyMissingException
import com.example.ui.components.NavigationTab
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val currentNavTab: NavigationTab = NavigationTab.HOME,
    val isShowingResultScreen: Boolean = false,
    
    // Creator Inputs
    val inputIdea: String = "",
    val contentType: ContentType = ContentType.SHORT_VIDEO,
    val language: LanguageOption = LanguageOption.AUTO,
    val tone: ContentTone = ContentTone.PROFESSIONAL,
    val goal: ContentGoal = ContentGoal.REACH,
    val targetAudience: String = "",
    val selectedPlatforms: Set<SocialPlatform> = SocialPlatform.values().toSet(),
    
    // Generation Status
    val isGenerating: Boolean = false,
    val generationStageText: String = "",
    val generationError: String? = null,
    
    // Result State
    val generatedPackage: SocialPackage? = null,
    val selectedVariation: VariationType = VariationType.A,
    val activeResultPlatform: SocialPlatform? = null, // null means "All"
    
    // Edit & Regen modals
    val editingField: EditFieldContext? = null,
    val regeneratingPlatform: SocialPlatform? = null,
    
    // API Key & Model Management
    val isApiKeyConfigured: Boolean = false,
    val isCustomApiKeySet: Boolean = false,
    val maskedApiKey: String = "No key configured",
    val isTestingApiKey: Boolean = false,
    val apiKeyTestResult: String? = null,
    val apiKeyTestSuccess: Boolean? = null,
    
    // Theme & Settings
    val themeMode: ThemeMode = ThemeMode.DARK,
    val activeBrandProfile: BrandProfile? = null,
    val toastMessage: String? = null,
    val searchQuery: String = ""
)

data class EditFieldContext(
    val platform: SocialPlatform,
    val fieldName: String,
    val currentContent: String,
    val maxLimit: Int,
    val updateAction: (String) -> Unit
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SocialContentRepository.getInstance(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val savedProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brandProfiles: StateFlow<List<BrandProfile>> = repository.allBrandProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadActiveBrandProfile()
        refreshApiKeyState()
    }

    private fun loadActiveBrandProfile() {
        viewModelScope.launch {
            val active = repository.getActiveBrandProfile()
            _uiState.update { it.copy(activeBrandProfile = active) }
        }
    }

    fun refreshApiKeyState() {
        val hasKey = repository.hasApiKey()
        val masked = repository.getMaskedApiKey()
        val isCustom = repository.isCustomKeySet()
        _uiState.update {
            it.copy(
                isApiKeyConfigured = hasKey,
                maskedApiKey = masked,
                isCustomApiKeySet = isCustom
            )
        }
    }

    fun saveApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        if (trimmed.isBlank()) {
            showToast("API Key cannot be empty.")
            return
        }
        val saved = repository.saveApiKey(trimmed)
        if (saved) {
            refreshApiKeyState()
            showToast("Gemini API key securely saved!")
        } else {
            showToast("Failed to save API key.")
        }
    }

    fun removeApiKey() {
        repository.removeApiKey()
        refreshApiKeyState()
        showToast("Gemini API key removed.")
    }

    fun testApiKey(apiKeyToTest: String? = null) {
        val key = apiKeyToTest?.trim()?.takeIf { it.isNotBlank() } ?: repository.getApiKey()
        if (key.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isTestingApiKey = false,
                    apiKeyTestResult = "Please enter an API key to test.",
                    apiKeyTestSuccess = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestingApiKey = true,
                    apiKeyTestResult = "Connecting to Gemini 3.6 Flash...",
                    apiKeyTestSuccess = null
                )
            }

            val result = repository.testApiKey(key)
            result.onSuccess { msg ->
                _uiState.update {
                    it.copy(
                        isTestingApiKey = false,
                        apiKeyTestResult = "✓ $msg",
                        apiKeyTestSuccess = true
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isTestingApiKey = false,
                        apiKeyTestResult = "✗ ${err.localizedMessage ?: "Failed to connect to Gemini API."}",
                        apiKeyTestSuccess = false
                    )
                }
            }
        }
    }

    fun clearApiKeyTestResult() {
        _uiState.update { it.copy(apiKeyTestResult = null, apiKeyTestSuccess = null) }
    }

    fun setNavTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentNavTab = tab) }
    }

    fun setInputIdea(idea: String) {
        _uiState.update { it.copy(inputIdea = idea) }
    }

    fun setContentType(type: ContentType) {
        _uiState.update { it.copy(contentType = type) }
    }

    fun setLanguage(lang: LanguageOption) {
        _uiState.update { it.copy(language = lang) }
    }

    fun setTone(tone: ContentTone) {
        _uiState.update { it.copy(tone = tone) }
    }

    fun setGoal(goal: ContentGoal) {
        _uiState.update { it.copy(goal = goal) }
    }

    fun setTargetAudience(audience: String) {
        _uiState.update { it.copy(targetAudience = audience) }
    }

    fun togglePlatform(platform: SocialPlatform) {
        _uiState.update { state ->
            val current = state.selectedPlatforms.toMutableSet()
            if (current.contains(platform)) {
                if (current.size > 1) current.remove(platform)
            } else {
                current.add(platform)
            }
            state.copy(selectedPlatforms = current)
        }
    }

    fun selectAllPlatforms() {
        _uiState.update { it.copy(selectedPlatforms = SocialPlatform.values().toSet()) }
    }

    fun clearAllPlatforms() {
        _uiState.update { it.copy(selectedPlatforms = setOf(SocialPlatform.YOUTUBE)) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setVariation(variation: VariationType) {
        _uiState.update { it.copy(selectedVariation = variation) }
    }

    fun setResultPlatformTab(platform: SocialPlatform?) {
        _uiState.update { it.copy(activeResultPlatform = platform) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun generateContent() {
        val state = _uiState.value
        val idea = state.inputIdea.trim()
        if (idea.isBlank()) {
            showToast("Please enter your content idea or topic first!")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationStageText = "Connecting to Gemini 3.6 Flash...",
                    generationError = null
                )
            }

            delay(300)
            _uiState.update { it.copy(generationStageText = "Synthesizing platform-native hooks & angles...") }

            delay(300)
            _uiState.update { it.copy(generationStageText = "Formatting YouTube, TikTok, IG, Threads, LinkedIn, FB & X...") }

            try {
                val socialPkg = repository.generateCompletePackage(
                    idea = idea,
                    contentType = state.contentType,
                    language = state.language,
                    tone = state.tone,
                    goal = state.goal,
                    targetAudience = state.targetAudience,
                    brandProfile = state.activeBrandProfile,
                    selectedPlatforms = state.selectedPlatforms
                )

                // Auto-save project to local history
                repository.saveProject(socialPkg)

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedPackage = socialPkg,
                        isShowingResultScreen = true,
                        selectedVariation = VariationType.A,
                        activeResultPlatform = null // All tab
                    )
                }
            } catch (e: GeminiApiKeyMissingException) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationError = "Gemini API key is required. Please go to Settings -> Gemini API to add your API key."
                    )
                }
            } catch (e: GeminiApiException) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationError = e.localizedMessage ?: "Gemini API generation error. Please try again."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationError = "Generation failed: ${e.localizedMessage ?: "Please check connection or API key."}"
                    )
                }
            }
        }
    }

    fun openEditField(
        platform: SocialPlatform,
        fieldName: String,
        currentContent: String,
        maxLimit: Int,
        updateAction: (String) -> Unit
    ) {
        _uiState.update {
            it.copy(
                editingField = EditFieldContext(
                    platform = platform,
                    fieldName = fieldName,
                    currentContent = currentContent,
                    maxLimit = maxLimit,
                    updateAction = updateAction
                )
            )
        }
    }

    fun closeEditField() {
        _uiState.update { it.copy(editingField = null) }
    }

    fun openRegenerateSheet(platform: SocialPlatform) {
        _uiState.update { it.copy(regeneratingPlatform = platform) }
    }

    fun closeRegenerateSheet() {
        _uiState.update { it.copy(regeneratingPlatform = null) }
    }

    fun regeneratePlatformContent(styleInstruction: String) {
        val state = _uiState.value
        val platform = state.regeneratingPlatform ?: return
        val currentPackage = state.generatedPackage ?: return

        viewModelScope.launch {
            closeRegenerateSheet()
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationStageText = "Regenerating ${platform.displayName} via Gemini 3.6 Flash..."
                )
            }

            try {
                val activeVersion = when (state.selectedVariation) {
                    VariationType.A -> currentPackage.versionA
                    VariationType.B -> currentPackage.versionB
                    VariationType.C -> currentPackage.versionC
                }

                val updatedPlatformPkg = repository.regeneratePlatformContent(
                    idea = currentPackage.originalIdea,
                    contentType = currentPackage.contentType,
                    language = currentPackage.language,
                    tone = currentPackage.tone,
                    goal = currentPackage.goal,
                    targetAudience = currentPackage.targetAudience,
                    brandProfile = state.activeBrandProfile,
                    platform = platform,
                    currentPkg = activeVersion,
                    styleInstruction = styleInstruction
                )

                val updatedPackage = when (state.selectedVariation) {
                    VariationType.A -> currentPackage.copy(versionA = updatedPlatformPkg)
                    VariationType.B -> currentPackage.copy(versionB = updatedPlatformPkg)
                    VariationType.C -> currentPackage.copy(versionC = updatedPlatformPkg)
                }

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedPackage = updatedPackage
                    )
                }
                showToast("${platform.displayName} regenerated with Gemini 3.6 Flash!")
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false) }
                showToast("Regeneration error: ${e.localizedMessage}")
            }
        }
    }

    fun fixTextToFit(platform: SocialPlatform, fieldName: String, originalText: String, maxLimit: Int) {
        val trimmed = ContentQualityEngine.fixToFit(originalText, maxLimit)
        updatePlatformField(platform, fieldName, trimmed)
        showToast("Content shortened to fit $maxLimit characters!")
    }

    fun updatePlatformField(platform: SocialPlatform, fieldName: String, newContent: String) {
        val currentPackage = _uiState.value.generatedPackage ?: return
        val currentVersion = when (_uiState.value.selectedVariation) {
            VariationType.A -> currentPackage.versionA
            VariationType.B -> currentPackage.versionB
            VariationType.C -> currentPackage.versionC
        }

        val updatedVersion = when (platform) {
            SocialPlatform.YOUTUBE -> when (fieldName.lowercase()) {
                "title" -> currentVersion.copy(youtube = currentVersion.youtube.copy(title = newContent))
                "description" -> currentVersion.copy(youtube = currentVersion.youtube.copy(description = newContent))
                else -> currentVersion
            }
            SocialPlatform.TIKTOK -> when (fieldName.lowercase()) {
                "caption" -> currentVersion.copy(tiktok = currentVersion.tiktok.copy(caption = newContent))
                "hook" -> currentVersion.copy(tiktok = currentVersion.tiktok.copy(hookText = newContent))
                else -> currentVersion
            }
            SocialPlatform.INSTAGRAM -> when (fieldName.lowercase()) {
                "caption" -> currentVersion.copy(instagram = currentVersion.instagram.copy(caption = newContent))
                "hook" -> currentVersion.copy(instagram = currentVersion.instagram.copy(firstLineHook = newContent))
                else -> currentVersion
            }
            SocialPlatform.THREADS -> currentVersion.copy(threads = currentVersion.threads.copy(primaryPost = newContent))
            SocialPlatform.LINKEDIN -> when (fieldName.lowercase()) {
                "post", "main post" -> currentVersion.copy(linkedin = currentVersion.linkedin.copy(mainPost = newContent))
                "hook" -> currentVersion.copy(linkedin = currentVersion.linkedin.copy(hook = newContent))
                else -> currentVersion
            }
            SocialPlatform.FACEBOOK -> currentVersion.copy(facebook = currentVersion.facebook.copy(mainPost = newContent))
            SocialPlatform.X -> currentVersion.copy(x = currentVersion.x.copy(standardPost = newContent))
        }

        val updatedSocialPackage = when (_uiState.value.selectedVariation) {
            VariationType.A -> currentPackage.copy(versionA = updatedVersion)
            VariationType.B -> currentPackage.copy(versionB = updatedVersion)
            VariationType.C -> currentPackage.copy(versionC = updatedVersion)
        }

        _uiState.update { it.copy(generatedPackage = updatedSocialPackage) }
    }

    fun copyToClipboard(text: String, label: String = "Content") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showToast("Copied to clipboard!")
    }

    fun shareText(text: String, title: String = "Share Content") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooser)
    }

    fun saveToFavorites(title: String, content: String, category: String, platform: String, sourceIdea: String) {
        viewModelScope.launch {
            repository.addFavorite(
                title = title,
                content = content,
                category = category,
                platform = platform,
                sourceIdea = sourceIdea
            )
            showToast("Saved to Favorites ⭐")
        }
    }

    fun deleteFavorite(id: Long) {
        viewModelScope.launch {
            repository.deleteFavorite(id)
            showToast("Removed from Favorites")
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
            showToast("Cleared all favorites")
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            showToast("Project deleted")
        }
    }

    fun clearAllProjects() {
        viewModelScope.launch {
            repository.clearAllProjects()
            showToast("Cleared project history")
        }
    }

    fun loadProject(project: ProjectEntity) {
        val vA = repository.parseStoredPackage(project.versionAJson) ?: PlatformPackage()
        val vB = repository.parseStoredPackage(project.versionBJson) ?: PlatformPackage()
        val vC = repository.parseStoredPackage(project.versionCJson) ?: PlatformPackage()

        val platforms = project.selectedPlatformsJson.split(",")
            .mapNotNull { runCatching { SocialPlatform.valueOf(it) }.getOrNull() }
            .toSet()

        val socialPkg = SocialPackage(
            originalIdea = project.originalIdea,
            contentType = runCatching { ContentType.valueOf(project.contentType) }.getOrDefault(ContentType.SHORT_VIDEO),
            language = runCatching { LanguageOption.valueOf(project.language) }.getOrDefault(LanguageOption.AUTO),
            tone = runCatching { ContentTone.valueOf(project.tone) }.getOrDefault(ContentTone.PROFESSIONAL),
            goal = runCatching { ContentGoal.valueOf(project.goal) }.getOrDefault(ContentGoal.REACH),
            targetAudience = project.targetAudience,
            brandName = project.brandName,
            creatorName = project.creatorName,
            productName = "",
            websiteUrl = "",
            mainKeyword = "",
            location = "",
            selectedPlatforms = if (platforms.isNotEmpty()) platforms else SocialPlatform.values().toSet(),
            versionA = vA,
            versionB = vB,
            versionC = vC,
            qualityScore = project.qualityScore
        )

        _uiState.update {
            it.copy(
                generatedPackage = socialPkg,
                isShowingResultScreen = true,
                selectedVariation = VariationType.A,
                activeResultPlatform = null,
                inputIdea = project.originalIdea
            )
        }
    }

    fun duplicateProject(project: ProjectEntity) {
        viewModelScope.launch {
            val duplicate = project.copy(
                id = 0,
                title = "Copy of ${project.title}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val vA = repository.parseStoredPackage(project.versionAJson) ?: PlatformPackage()
            val vB = repository.parseStoredPackage(project.versionBJson) ?: PlatformPackage()
            val vC = repository.parseStoredPackage(project.versionCJson) ?: PlatformPackage()
            val platforms = project.selectedPlatformsJson.split(",")
                .mapNotNull { runCatching { SocialPlatform.valueOf(it) }.getOrNull() }
                .toSet()

            val socialPkg = SocialPackage(
                originalIdea = duplicate.originalIdea,
                contentType = runCatching { ContentType.valueOf(duplicate.contentType) }.getOrDefault(ContentType.SHORT_VIDEO),
                language = runCatching { LanguageOption.valueOf(duplicate.language) }.getOrDefault(LanguageOption.AUTO),
                tone = runCatching { ContentTone.valueOf(duplicate.tone) }.getOrDefault(ContentTone.PROFESSIONAL),
                goal = runCatching { ContentGoal.valueOf(duplicate.goal) }.getOrDefault(ContentGoal.REACH),
                targetAudience = duplicate.targetAudience,
                brandName = duplicate.brandName,
                creatorName = duplicate.creatorName,
                productName = "",
                websiteUrl = "",
                mainKeyword = "",
                location = "",
                selectedPlatforms = platforms,
                versionA = vA,
                versionB = vB,
                versionC = vC,
                qualityScore = duplicate.qualityScore
            )
            repository.saveProject(socialPkg)
            showToast("Project duplicated!")
        }
    }

    fun saveBrandProfile(profile: BrandProfile) {
        viewModelScope.launch {
            repository.saveBrandProfile(profile)
            loadActiveBrandProfile()
            showToast("Brand Profile saved!")
        }
    }

    fun setActiveBrandProfile(id: Long) {
        viewModelScope.launch {
            repository.setActiveBrandProfile(id)
            loadActiveBrandProfile()
            showToast("Active brand profile updated!")
        }
    }

    fun deleteBrandProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteBrandProfile(id)
            loadActiveBrandProfile()
            showToast("Brand profile removed")
        }
    }

    fun navigateBackToHome() {
        _uiState.update { it.copy(isShowingResultScreen = false) }
    }
}
