package com.omerfaruk.ykstakip.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omerfaruk.ykstakip.data.local.PreferenceManager
import com.omerfaruk.ykstakip.data.local.Reward
import com.omerfaruk.ykstakip.data.local.TopicEntity
import com.omerfaruk.ykstakip.data.local.UserInfo
import com.omerfaruk.ykstakip.data.local.YksDao
import com.omerfaruk.ykstakip.data.local.NetResultEntity
import com.omerfaruk.ykstakip.data.local.WrongQuestionEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.omerfaruk.ykstakip.data.local.CalculationHistoryEntity
import com.omerfaruk.ykstakip.data.SupabaseYigilma
import org.json.JSONObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow

class YksViewModel(
    private val yksDao: YksDao,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val calculationHistory: StateFlow<List<CalculationHistoryEntity>> = yksDao.getAllCalculationHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _yigilmaData = kotlinx.coroutines.flow.MutableStateFlow<List<SupabaseYigilma>>(emptyList())
    val yigilmaData = _yigilmaData.asStateFlow()

    fun fetchYigilmaData(token: String? = accessToken.value) {
        viewModelScope.launch {
            try {
                val data = com.omerfaruk.ykstakip.data.SupabaseRepository.getYigilmaVerileri(token)
                _yigilmaData.value = data
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "Yigilma verileri cekilemedi", e)
            }
        }
    }

    val userInfo: StateFlow<UserInfo?> = preferenceManager.userInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOnboardingCompleted: StateFlow<Boolean> = preferenceManager.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val accessToken: StateFlow<String?> = preferenceManager.accessToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            accessToken.collect { token ->
                fetchYigilmaData(token)
                if (token != null) {
                    restoreProgressFromCloud()
                    restoreStudyTimesFromCloud()
                    restoreNetResultsFromCloud()
                    restoreCalculationsFromCloud()
                    restoreInteractionsFromCloud()
                    restoreFollowedSubjectsFromCloud()
                    restoreQuestionLogsFromCloud()
                }
            }
        }
    }

    fun saveAuthTokens(access: String, refresh: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            preferenceManager.saveAuthTokens(access, refresh)
            var hasProfile = false
            try {
                val profile = com.omerfaruk.ykstakip.data.SupabaseRepository.getProfile(access)
                if (profile != null && profile.has("first_name") && profile.optString("first_name").isNotEmpty()) {
                    val fName = profile.optString("first_name")
                    val lName = profile.optString("last_name")
                    val title = profile.optString("title")
                    val examYear = profile.optString("exam_year", "2027")
                    val major = profile.optString("major", "Sayısal")
                    val displayName = profile.optString("display_name", fName)
                    val obp = profile.optString("obp", "100.0")
                    preferenceManager.saveUserInfo(fName, lName, title, major, displayName, examYear, obp)
                    hasProfile = true
                }
                // Konu ilerlemelerini buluttan geri yükle
                restoreProgressFromCloud()
                // Çalışma sürelerini buluttan geri yükle
                restoreStudyTimesFromCloud()
                // Net sonuçlarını buluttan geri yükle
                restoreNetResultsFromCloud()
                // Sıralama hesaplamalarını buluttan geri yükle
                restoreCalculationsFromCloud()
                // Snippet etkileşimlerini buluttan geri yükle
                restoreInteractionsFromCloud()
                // Yıldızlı dersleri buluttan geri yükle
                restoreFollowedSubjectsFromCloud()
                // Soru loglarını buluttan geri yükle
                restoreQuestionLogsFromCloud()
            } catch(e: Exception) {}
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onComplete?.invoke(hasProfile)
            }
        }
    }

    val allTopics: StateFlow<List<TopicEntity>> = yksDao.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getMajorAytCategories(major: String): List<String> {
        return when(major) {
            "Sayısal" -> listOf("AYT_MAT", "AYT_GEO", "AYT_FIZ", "AYT_KIM", "AYT_BIY")
            "Eşit Ağırlık" -> listOf("AYT_MAT", "AYT_GEO", "AYT_EDB", "AYT_TAR1", "AYT_COG1")
            "Sözel" -> listOf("AYT_EDB", "AYT_TAR1", "AYT_COG1", "AYT_TAR2", "AYT_COG2", "AYT_FEL", "AYT_DIN")
            else -> listOf("AYT_MAT", "AYT_GEO", "AYT_FIZ", "AYT_KIM", "AYT_BIY")
        }
    }

    val subjectsWithProgress: StateFlow<List<SubjectUiModel>> = combine(allTopics, userInfo) { topics, info ->
        if (info == null) return@combine emptyList<SubjectUiModel>()
        
        val major = info.major
        val majorAytCats = getMajorAytCategories(major)
        
        val filteredTopics = topics.filter { it.category == "TYT" || it.category in majorAytCats }
        
        filteredTopics.groupBy { it.subjectName to it.category }
            .map { (pair, subjectTopics) ->
                val (name, category) = pair
                val uniqueTopics = subjectTopics.distinctBy { it.title }
                val progress = if (uniqueTopics.isEmpty()) 0f else uniqueTopics.count { it.isCompleted }.toFloat() / uniqueTopics.size
                SubjectUiModel(name, progress, uniqueTopics, category)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _newRewardAchieved = MutableSharedFlow<Reward>(extraBufferCapacity = 64)
    val newRewardAchieved = _newRewardAchieved.asSharedFlow()

    private var previousAchievedTitles: Set<String>? = null

    val rewards: StateFlow<List<Reward>> = combine(allTopics, userInfo) { topics, info ->
        if (info == null || topics.isEmpty()) return@combine emptyList<Reward>()

        val major = info.major
        val majorAytCats = getMajorAytCategories(major)

        val rewardList = mutableListOf<Reward>()

        // TYT Ödülleri
        val tytSubjects = topics.filter { it.category == "TYT" }.groupBy { it.subjectName }
        tytSubjects.forEach { (subjectName, subjectTopics) ->
            val uniqueTopics = subjectTopics.distinctBy { it.title }
            if (uniqueTopics.isNotEmpty()) {
                val progress = uniqueTopics.count { it.isCompleted }.toFloat() / uniqueTopics.size
                
                rewardList.add(Reward("Acemi $subjectName\ncı", progress >= 0.01f, 1))
                rewardList.add(Reward("Tecrübeli $subjectName\ncı", progress >= 0.50f, 2))
                rewardList.add(Reward("Uzman $subjectName\ncı", progress >= 1.0f, 3))
            }
        }

        // AYT Ödülleri
        val aytSubjects = topics.filter { it.category in majorAytCats }.groupBy { it.subjectName }
        aytSubjects.forEach { (subjectName, subjectTopics) ->
            val uniqueTopics = subjectTopics.distinctBy { it.title }
            if (uniqueTopics.isNotEmpty()) {
                val progress = uniqueTopics.count { it.isCompleted }.toFloat() / uniqueTopics.size
                
                rewardList.add(Reward("$subjectName Akademisyeni", progress >= 0.01f, 1))
                rewardList.add(Reward("$subjectName Doktoru", progress >= 0.50f, 2))
                rewardList.add(Reward("$subjectName Profesörü", progress >= 1.0f, 3))
            }
        }

        // Genel İlerleme: YKS Canavarı
        val relevantTopics = topics.filter { it.category == "TYT" || it.category in majorAytCats }
        if (relevantTopics.isNotEmpty()) {
            val overallProg = relevantTopics.count { it.isCompleted }.toFloat() / relevantTopics.size
            rewardList.add(Reward("YKS Canavarı", overallProg >= 1.0f, 3))
        }

        val sortedRewards = rewardList.sortedWith(compareByDescending<Reward> { it.isAchieved }.thenBy { it.level })
        
        val currentAchieved = sortedRewards.filter { it.isAchieved }.map { it.title }.toSet()
        
        if (previousAchievedTitles != null) {
            val newlyAchieved = sortedRewards.filter { it.isAchieved && !previousAchievedTitles!!.contains(it.title) }
            newlyAchieved.forEach { reward ->
                viewModelScope.launch {
                    _newRewardAchieved.emit(reward)
                }
            }
        }
        
        previousAchievedTitles = currentAchieved
        sortedRewards
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallProgress: StateFlow<Float> = subjectsWithProgress.map { subjects ->
        val allRelevantTopics = subjects.flatMap { it.topics }
        if (allRelevantTopics.isEmpty()) 0f else allRelevantTopics.count { it.isCompleted }.toFloat() / allRelevantTopics.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val allStudyTimes: StateFlow<List<com.omerfaruk.ykstakip.data.local.StudyTimeEntity>> = yksDao.getAllStudyTimes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSnippetInteractions: StateFlow<List<com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity>> = yksDao.getAllSnippetInteractions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFollowedSubjects: StateFlow<List<com.omerfaruk.ykstakip.data.local.FollowedSubjectEntity>> = yksDao.getAllFollowedSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestionLogs: StateFlow<List<com.omerfaruk.ykstakip.data.local.QuestionLogEntity>> = yksDao.getAllQuestionLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveUserInfo(firstName: String, lastName: String, title: String, major: String, displayName: String, examYear: String, obp: String, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            val current = preferenceManager.userInfo.first()
            val hasChanged = current == null ||
                    current.firstName != firstName ||
                    current.lastName != lastName ||
                    current.title != title ||
                    current.major != major ||
                    current.displayName != displayName ||
                    current.examYear != examYear ||
                    current.obp != obp

            preferenceManager.saveUserInfo(firstName, lastName, title, major, displayName, examYear, obp)

            if (!hasChanged) {
                // Herhangi bir değişiklik yoksa Supabase çağrısını atla
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult?.invoke(true, null)
                }
                return@launch
            }

            val token = accessToken.value
            if (token != null) {
                val (success, errorMsg) = com.omerfaruk.ykstakip.data.SupabaseRepository.updateProfile(
                    accessToken = token,
                    firstName = firstName,
                    lastName = lastName,
                    title = title,
                    examYear = examYear,
                    major = major,
                    displayName = displayName,
                    obp = obp.toFloatOrNull() ?: 100f
                )
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult?.invoke(success, errorMsg)
                }
            } else {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult?.invoke(false, "Token bulunamadı!")
                }
            }
        }
    }

    fun toggleTopicCompletion(topic: TopicEntity) {
        viewModelScope.launch {
            val newState = !topic.isCompleted
            yksDao.updateTopic(topic.copy(isCompleted = newState))
            
            // Arka planda Supabase'e senkronize et
            val token = accessToken.value
            if (token != null) {
                com.omerfaruk.ykstakip.data.SupabaseRepository.syncTopicProgress(
                    accessToken = token,
                    topicTitle = topic.title,
                    subjectName = topic.subjectName,
                    category = topic.category,
                    isCompleted = newState
                )
            }
        }
    }

    fun restoreProgressFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            val completedTopics = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllProgress(token)
            if (completedTopics.isNotEmpty()) {
                val allLocal = allTopics.value
                for ((title, subject, category) in completedTopics) {
                    val localTopic = allLocal.find { it.title == title && it.subjectName == subject && it.category == category }
                    if (localTopic != null && !localTopic.isCompleted) {
                        yksDao.updateTopic(localTopic.copy(isCompleted = true))
                    }
                }
            }
        }
    }

    fun restoreStudyTimesFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            try {
                if (yksDao.getAllStudyTimes().first().isNotEmpty()) {
                    android.util.Log.d("YksViewModel", "Local study times not empty, skipping restore.")
                    return@launch
                }
                val cloudStudyTimes = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllStudyTimes(token)
                if (cloudStudyTimes.isNotEmpty()) {
                    for ((title, subject, duration) in cloudStudyTimes) {
                        yksDao.insertStudyTime(
                            com.omerfaruk.ykstakip.data.local.StudyTimeEntity(
                                subjectName = subject,
                                topicTitle = title,
                                durationSeconds = duration
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "Calisma sureleri geri yuklenemedi", e)
            }
        }
    }

    fun restoreNetResultsFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            try {
                if (yksDao.getAllNetResults().first().isNotEmpty()) {
                    android.util.Log.d("YksViewModel", "Local net results not empty, skipping restore.")
                    return@launch
                }
                val cloudNets = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllNetResults(token)
                if (cloudNets.isNotEmpty()) {
                    for (net in cloudNets) {
                        yksDao.insertNetResult(net)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "Net sonuclari geri yuklenemedi", e)
            }
        }
    }


    // Net Tracking
    val tytNetResults: StateFlow<List<NetResultEntity>> = yksDao.getNetResultsByType("TYT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aytNetResults: StateFlow<List<NetResultEntity>> = yksDao.getNetResultsByType("AYT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bransNetResults: StateFlow<List<NetResultEntity>> = yksDao.getNetResultsByType("BRANS")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveNetResult(type: String, totalNet: Float, details: String) {
        viewModelScope.launch {
            val entity = NetResultEntity(
                date = System.currentTimeMillis(),
                type = type,
                totalNet = totalNet,
                details = details
            )
            yksDao.insertNetResult(entity)

            val token = accessToken.value
            if (token != null) {
                com.omerfaruk.ykstakip.data.SupabaseRepository.syncNetResult(token, entity)
            }
        }
    }

    fun updateNetResult(result: NetResultEntity) {
        viewModelScope.launch {
            yksDao.updateNetResult(result)
        }
    }

    fun deleteNetResult(id: Int) {
        viewModelScope.launch {
            yksDao.deleteNetResultById(id)
        }
    }

    // Wrong Questions
    fun getWrongQuestionsBySubjectAndType(subjectName: String, examType: String): kotlinx.coroutines.flow.Flow<List<WrongQuestionEntity>> {
        return yksDao.getWrongQuestionsBySubjectAndType(subjectName, examType)
    }

    fun saveWrongQuestion(subjectName: String, examType: String, topicTitle: String, imagePath: String) {
        viewModelScope.launch {
            val entity = WrongQuestionEntity(
                subjectName = subjectName,
                examType = examType,
                topicTitle = topicTitle,
                imagePath = imagePath
            )
            yksDao.insertWrongQuestion(entity)
        }
    }

    fun deleteWrongQuestion(question: WrongQuestionEntity) {
        viewModelScope.launch {
            // Dosyayı da sil
            try { java.io.File(question.imagePath).delete() } catch (_: Exception) {}
            yksDao.deleteWrongQuestion(question)
        }
    }

    fun toggleWrongQuestionSolved(question: WrongQuestionEntity) {
        viewModelScope.launch {
            val updated = question.copy(isSolved = !question.isSolved)
            yksDao.updateWrongQuestion(updated)
        }
    }

    fun saveStudyTime(subjectName: String, topicTitle: String, durationSeconds: Long) {
        viewModelScope.launch {
            yksDao.insertStudyTime(
                com.omerfaruk.ykstakip.data.local.StudyTimeEntity(
                    subjectName = subjectName,
                    topicTitle = topicTitle,
                    durationSeconds = durationSeconds
                )
            )
            
            // Supabase senkronizasyonu
            val token = accessToken.value
            if (token != null) {
                com.omerfaruk.ykstakip.data.SupabaseRepository.syncStudyTime(
                    accessToken = token,
                    topicTitle = topicTitle,
                    subjectName = subjectName,
                    durationSeconds = durationSeconds
                )
            }
        }
    }

    // Supabase Hap Bilgi
    private val _hapTopics = kotlinx.coroutines.flow.MutableStateFlow<List<com.omerfaruk.ykstakip.data.SupabaseTopic>>(emptyList())
    val hapTopics: StateFlow<List<com.omerfaruk.ykstakip.data.SupabaseTopic>> = _hapTopics.asStateFlow()

    private val _hapSnippets = kotlinx.coroutines.flow.MutableStateFlow<List<com.omerfaruk.ykstakip.data.SupabaseSnippet>>(emptyList())
    val hapSnippets: StateFlow<List<com.omerfaruk.ykstakip.data.SupabaseSnippet>> = _hapSnippets.asStateFlow()

    private val _isLoadingHap = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLoadingHap: StateFlow<Boolean> = _isLoadingHap.asStateFlow()

    fun fetchHapTopics() {
        viewModelScope.launch {
            _isLoadingHap.value = true
            _hapTopics.value = com.omerfaruk.ykstakip.data.SupabaseRepository.getTopics()
            _isLoadingHap.value = false
        }
    }

    fun fetchHapSnippets(topicId: String) {
        viewModelScope.launch {
            _isLoadingHap.value = true
            try {
                val snippets = com.omerfaruk.ykstakip.data.SupabaseRepository.getSnippets(topicId)
                val interactions = yksDao.getAllSnippetInteractions().first()
                val knownSnippets = interactions.filter { it.isKnown }.map { it.snippetId }.toSet()
                _hapSnippets.value = snippets.filter { it.id !in knownSnippets }.shuffled()
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "fetchHapSnippets error", e)
            }
            _isLoadingHap.value = false
        }
    }

    fun fetchAllRandomSnippets(onlyStarred: Boolean = false) {
        viewModelScope.launch {
            _isLoadingHap.value = true
            try {
                val topics = com.omerfaruk.ykstakip.data.SupabaseRepository.getTopics()
                _hapTopics.value = topics
                val allSnippets = com.omerfaruk.ykstakip.data.SupabaseRepository.getAllSnippets()
                
                val followedList = yksDao.getAllFollowedSubjects().first()
                val starredList = followedList.filter { it.isFollowed }
                    .map { it.subjectName }
                    .toSet()

                val snippets = if (onlyStarred) {
                    allSnippets.filter { snippet ->
                        val topic = topics.find { it.id == snippet.topicId }
                        topic?.subjectName in starredList
                    }
                } else {
                    allSnippets
                }
                
                val subjectScores = mutableMapOf<String, Double>()
                val topicScores = mutableMapOf<String, Double>()
                
                val now = System.currentTimeMillis()
                val rawInteractions = yksDao.getAllSnippetInteractions().first()
                val activeKnownSnippetIds = mutableSetOf<String>()
                val savedSnippetExtraWeights = mutableMapOf<String, Double>()
                
                val interactions = rawInteractions.map { interaction ->
                    val days = (now - interaction.updatedAt).toDouble() / (1000 * 60 * 60 * 24)
                    if (interaction.isKnown && days > 30.0) {
                        val resetInteraction = interaction.copy(isKnown = false, updatedAt = now)
                        yksDao.insertOrUpdateInteraction(resetInteraction)
                        syncInteractionWithCloud(resetInteraction)
                        resetInteraction
                    } else {
                        interaction
                    }
                }
                
                for (interaction in interactions) {
                    val topicId = interaction.topicId
                    val subjectName = interaction.subjectName
                    val days = (now - interaction.updatedAt).toDouble() / (1000 * 60 * 60 * 24)
                    
                    if (interaction.isKnown) {
                        activeKnownSnippetIds.add(interaction.snippetId)
                    }
                    
                    val currentTopicScore = topicScores[topicId] ?: 10.0
                    val currentSubjectScore = subjectScores[subjectName] ?: 10.0
                    
                    var newTopicScore = currentTopicScore
                    var newSubjectScore = currentSubjectScore
                    
                    if (interaction.isLiked) {
                        when {
                            days <= 7.0 -> {
                                newTopicScore += 2.0
                                newSubjectScore += 1.0
                            }
                            days <= 30.0 -> {
                                newTopicScore += 1.0
                                newSubjectScore += 0.5
                            }
                        }
                    }
                    
                    if (interaction.isSaved) {
                        when {
                            days <= 7.0 -> {
                                newTopicScore += 3.0
                                newSubjectScore += 1.0
                                if (!interaction.isKnown) {
                                    savedSnippetExtraWeights[interaction.snippetId] = 25.0
                                }
                            }
                            days <= 30.0 -> {
                                newTopicScore += 1.5
                                newSubjectScore += 0.5
                                if (!interaction.isKnown) {
                                    savedSnippetExtraWeights[interaction.snippetId] = 12.0
                                }
                            }
                        }
                    }
                    
                    if (interaction.isKnown) {
                        when {
                            days <= 7.0 -> {
                                newTopicScore -= 2.0
                            }
                            days <= 30.0 -> {
                                newTopicScore -= 1.0
                            }
                        }
                    }
                    
                    topicScores[topicId] = newTopicScore.coerceIn(1.0, 40.0)
                    subjectScores[subjectName] = newSubjectScore.coerceIn(1.0, 40.0)
                }

                val filtered = snippets.filter { it.id !in activeKnownSnippetIds }
                
                if (filtered.isEmpty()) {
                    _hapSnippets.value = emptyList()
                } else {
                    val withWeights = filtered.map { snippet ->
                        val topic = topics.find { it.id == snippet.topicId }
                        val subjectName = topic?.subjectName ?: "Genel"
                        
                        val tScore = topicScores[snippet.topicId] ?: 10.0
                        var sScore = subjectScores[subjectName] ?: 10.0
                        if (subjectName in starredList) {
                            sScore += 3.0
                        }
                        val extraWeight = savedSnippetExtraWeights[snippet.id] ?: 0.0
                        val totalWeight = tScore + sScore + extraWeight
                        
                        val r = kotlin.random.Random.nextDouble(0.0001, 1.0)
                        val score = Math.pow(r, 1.0 / totalWeight)
                        snippet to score
                    }
                    
                    val sortedByWeight = withWeights.sortedByDescending { it.second }.map { it.first }
                    
                    val targetSize = sortedByWeight.size
                    val weightedCount = (targetSize * 0.75).toInt().coerceIn(0, targetSize)
                    val randomCount = targetSize - weightedCount
                    
                    val weightedSubList = sortedByWeight.take(weightedCount)
                    val remainingSubList = sortedByWeight.drop(weightedCount)
                    val randomSubList = remainingSubList.shuffled().take(randomCount)
                    
                    val finalFeed = (weightedSubList + randomSubList).shuffled()
                    _hapSnippets.value = finalFeed
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "fetchAllRandomSnippets error", e)
            }
            _isLoadingHap.value = false
        }
    }

    fun toggleSnippetLike(snippetId: String, topicId: String, subjectName: String) {
        viewModelScope.launch {
            val existing = yksDao.getSnippetInteraction(snippetId)
            val updated = existing?.copy(isLiked = !existing.isLiked, updatedAt = System.currentTimeMillis())
                ?: com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity(
                    snippetId = snippetId,
                    topicId = topicId,
                    subjectName = subjectName,
                    isLiked = true
                )
            yksDao.insertOrUpdateInteraction(updated)
            syncInteractionWithCloud(updated)
            
            // Local listeyi de güncelleyelim ki beğeni sayısı arayüzde anında artsın/azalsın!
            _hapSnippets.value = _hapSnippets.value.map {
                if (it.id == snippetId) {
                    val diff = if (updated.isLiked) 1 else -1
                    it.copy(likesCount = (it.likesCount + diff).coerceAtLeast(0))
                } else {
                    it
                }
            }
        }
    }

    fun toggleSnippetKnown(snippetId: String, topicId: String, subjectName: String) {
        viewModelScope.launch {
            val existing = yksDao.getSnippetInteraction(snippetId)
            val updated = existing?.copy(isKnown = !existing.isKnown, updatedAt = System.currentTimeMillis())
                ?: com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity(
                    snippetId = snippetId,
                    topicId = topicId,
                    subjectName = subjectName,
                    isKnown = true
                )
            yksDao.insertOrUpdateInteraction(updated)
            syncInteractionWithCloud(updated)
            
            // Bilinen hapı hemen mevcut akıştan da çıkaralım
            _hapSnippets.value = _hapSnippets.value.filter { it.id != snippetId }
        }
    }

    fun toggleSnippetSaved(snippetId: String, topicId: String, subjectName: String) {
        viewModelScope.launch {
            val existing = yksDao.getSnippetInteraction(snippetId)
            val updated = existing?.copy(isSaved = !existing.isSaved, updatedAt = System.currentTimeMillis())
                ?: com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity(
                    snippetId = snippetId,
                    topicId = topicId,
                    subjectName = subjectName,
                    isSaved = true
                )
            yksDao.insertOrUpdateInteraction(updated)
            syncInteractionWithCloud(updated)
            
            // Local listeyi de güncelleyelim ki kaydetme sayısı arayüzde anında artsın/azalsın!
            _hapSnippets.value = _hapSnippets.value.map {
                if (it.id == snippetId) {
                    val diff = if (updated.isSaved) 1 else -1
                    it.copy(savesCount = (it.savesCount + diff).coerceAtLeast(0))
                } else {
                    it
                }
            }
        }
    }

    private fun syncInteractionWithCloud(interaction: com.omerfaruk.ykstakip.data.local.SnippetInteractionEntity) {
        val token = accessToken.value
        if (token != null) {
            viewModelScope.launch {
                com.omerfaruk.ykstakip.data.SupabaseRepository.syncSnippetInteraction(
                    accessToken = token,
                    snippetId = interaction.snippetId,
                    topicId = interaction.topicId,
                    subjectName = interaction.subjectName,
                    isLiked = interaction.isLiked,
                    isKnown = interaction.isKnown,
                    isSaved = interaction.isSaved
                )
            }
        }
    }

    fun reportSnippet(snippetId: String, reason: String, explanation: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = accessToken.value
            val success = com.omerfaruk.ykstakip.data.SupabaseRepository.reportSnippet(
                accessToken = token,
                snippetId = snippetId,
                reason = reason,
                explanation = explanation
            )
            onComplete(success)
        }
    }

    fun restoreInteractionsFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            try {
                val cloudInteractions = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllSnippetInteractions(token)
                if (cloudInteractions.isNotEmpty()) {
                    for (interaction in cloudInteractions) {
                        yksDao.insertOrUpdateInteraction(interaction)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "restoreInteractionsFromCloud error", e)
            }
        }
    }

    fun toggleSubjectFollow(subjectName: String) {
        viewModelScope.launch {
            val existing = yksDao.getFollowedSubject(subjectName)
            val updated = existing?.copy(isFollowed = !existing.isFollowed, updatedAt = System.currentTimeMillis())
                ?: com.omerfaruk.ykstakip.data.local.FollowedSubjectEntity(
                    subjectName = subjectName,
                    isFollowed = true
                )
            yksDao.insertOrUpdateFollowedSubject(updated)
            syncSubjectFollowWithCloud(updated)
        }
    }

    private fun syncSubjectFollowWithCloud(followedSubject: com.omerfaruk.ykstakip.data.local.FollowedSubjectEntity) {
        val token = accessToken.value
        if (token != null) {
            viewModelScope.launch {
                com.omerfaruk.ykstakip.data.SupabaseRepository.syncFollowedSubject(
                    accessToken = token,
                    subjectName = followedSubject.subjectName,
                    isFollowed = followedSubject.isFollowed
                )
            }
        }
    }

    fun restoreFollowedSubjectsFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            try {
                val cloudFollows = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllFollowedSubjects(token)
                if (cloudFollows.isNotEmpty()) {
                    for (follow in cloudFollows) {
                        yksDao.insertOrUpdateFollowedSubject(follow)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "restoreFollowedSubjectsFromCloud error", e)
            }
        }
    }

    fun addQuestionLog(subjectName: String, topicTitle: String, correctCount: Int, wrongCount: Int) {
        viewModelScope.launch {
            val dateMillis = (System.currentTimeMillis() / 1000) * 1000
            val log = com.omerfaruk.ykstakip.data.local.QuestionLogEntity(
                date = dateMillis,
                subjectName = subjectName,
                topicTitle = topicTitle,
                correctCount = correctCount,
                wrongCount = wrongCount,
                updatedAt = dateMillis
            )
            yksDao.insertOrUpdateQuestionLog(log)

            val token = accessToken.value
            if (token != null) {
                try {
                    com.omerfaruk.ykstakip.data.SupabaseRepository.syncQuestionLog(
                        accessToken = token,
                        subjectName = subjectName,
                        topicTitle = topicTitle,
                        correctCount = correctCount,
                        wrongCount = wrongCount,
                        dateMillis = dateMillis
                    )
                } catch (e: Exception) {
                    android.util.Log.e("YksViewModel", "Sync question log failed", e)
                }
            }
        }
    }

    fun deleteQuestionLog(log: com.omerfaruk.ykstakip.data.local.QuestionLogEntity) {
        viewModelScope.launch {
            yksDao.deleteQuestionLog(log)
            val token = accessToken.value
            if (token != null) {
                try {
                    com.omerfaruk.ykstakip.data.SupabaseRepository.deleteQuestionLog(token, log.date)
                } catch (e: Exception) {
                    android.util.Log.e("YksViewModel", "Sync delete question log failed", e)
                }
            }
        }
    }

    fun updateQuestionLog(log: com.omerfaruk.ykstakip.data.local.QuestionLogEntity, correctCount: Int, wrongCount: Int) {
        viewModelScope.launch {
            val updated = log.copy(correctCount = correctCount, wrongCount = wrongCount, updatedAt = System.currentTimeMillis())
            yksDao.insertOrUpdateQuestionLog(updated)
            val token = accessToken.value
            if (token != null) {
                try {
                    com.omerfaruk.ykstakip.data.SupabaseRepository.updateQuestionLog(token, log.date, correctCount, wrongCount)
                } catch (e: Exception) {
                    android.util.Log.e("YksViewModel", "Sync update question log failed", e)
                }
            }
        }
    }

    fun restoreQuestionLogsFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            try {
                // De-duplicate existing local database logs first (within 5 seconds threshold)
                val localLogs = yksDao.getAllQuestionLogs().first()
                val toDelete = mutableListOf<com.omerfaruk.ykstakip.data.local.QuestionLogEntity>()
                val visited = mutableListOf<com.omerfaruk.ykstakip.data.local.QuestionLogEntity>()
                for (log in localLogs) {
                    val duplicate = visited.find { 
                        it.subjectName == log.subjectName && 
                        it.topicTitle == log.topicTitle && 
                        Math.abs(it.date - log.date) < 5000 
                    }
                    if (duplicate != null) {
                        toDelete.add(log)
                    } else {
                        visited.add(log)
                    }
                }
                for (log in toDelete) {
                    yksDao.deleteQuestionLog(log)
                }

                val cloudLogs = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllQuestionLogs(token)
                if (cloudLogs.isNotEmpty()) {
                    val refreshedLocalLogs = yksDao.getAllQuestionLogs().first()
                    for (log in cloudLogs) {
                        val existing = refreshedLocalLogs.find { 
                            it.subjectName == log.subjectName && 
                            it.topicTitle == log.topicTitle && 
                            Math.abs(it.date - log.date) < 5000 
                        }
                        if (existing != null) {
                            val updated = log.copy(id = existing.id)
                            yksDao.insertOrUpdateQuestionLog(updated)
                        } else {
                            yksDao.insertOrUpdateQuestionLog(log)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "restoreQuestionLogsFromCloud error", e)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                preferenceManager.clearAll()
                yksDao.resetAllTopicsProgress()
                yksDao.deleteAllNetResults()
                yksDao.deleteAllWrongQuestions()
                yksDao.deleteAllStudyTimes()
                yksDao.deleteAllCalculationHistory()
                yksDao.deleteAllSnippetInteractions()
                yksDao.deleteAllFollowedSubjects()
                yksDao.deleteAllQuestionLogs()
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "logout error", e)
            }
            onComplete()
        }
    }

    suspend fun getSubjectFollowerCount(subjectName: String): Int {
        val token = accessToken.value
        return com.omerfaruk.ykstakip.data.SupabaseRepository.getSubjectFollowerCount(token, subjectName)
    }

    fun calculateAndSaveRankings(
        obp: Float,
        inputs: Map<String, Pair<Float, Float>>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val tytTurkceNet = getNet(inputs, "TYT Türkçe")
                val tytSosyalNet = getNet(inputs, "TYT Sosyal")
                val tytMatNet = getNet(inputs, "TYT Matematik")
                val tytFenNet = getNet(inputs, "TYT Fen")

                val aytMatNet = getNet(inputs, "AYT Matematik")
                val aytFizikNet = getNet(inputs, "AYT Fizik")
                val aytKimyaNet = getNet(inputs, "AYT Kimya")
                val aytBiyolojiNet = getNet(inputs, "AYT Biyoloji")
                val aytEdebiyatNet = getNet(inputs, "AYT Edebiyat")
                val aytTarih1Net = getNet(inputs, "AYT Tarih-1")
                val aytCografya1Net = getNet(inputs, "AYT Coğrafya-1")
                val aytTarih2Net = getNet(inputs, "AYT Tarih-2")
                val aytCografya2Net = getNet(inputs, "AYT Coğrafya-2")
                val aytFelsefeNet = getNet(inputs, "AYT Felsefe Grb.")
                val aytDinNet = getNet(inputs, "AYT Din")

                val years = listOf(2023, 2024, 2025)
                val resultsObj = JSONObject()
                val yigilmaList = _yigilmaData.value

                for (year in years) {
                    val yearObj = JSONObject()

                    val tytScore = when (year) {
                        2023 -> clampScore(141.898f + tytTurkceNet * 2.890f + tytSosyalNet * 3.024f + tytMatNet * 3.021f + tytFenNet * 3.057f)
                        2024 -> clampScore(142.15f + tytTurkceNet * 2.91f + tytSosyalNet * 2.98f + tytMatNet * 2.99f + tytFenNet * 3.02f)
                        else -> clampScore(142.0f + tytTurkceNet * 2.90f + tytSosyalNet * 3.00f + tytMatNet * 3.00f + tytFenNet * 3.00f)
                    }

                    val sayScore = when (year) {
                        2023 -> {
                            val aytScore = clampScore(118.868f + aytMatNet * 4.70f + aytFizikNet * 4.13f + aytKimyaNet * 4.90f + aytBiyolojiNet * 5.17f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                        2024 -> {
                            val aytScore = clampScore(119.15f + aytMatNet * 4.80f + aytFizikNet * 4.50f + aytKimyaNet * 4.80f + aytBiyolojiNet * 4.80f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                        else -> {
                            val aytScore = clampScore(119.00f + aytMatNet * 5.00f + aytFizikNet * 4.76f + aytKimyaNet * 5.128f + aytBiyolojiNet * 5.128f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                    }

                    val eaScore = when (year) {
                        2023 -> {
                            val aytScore = clampScore(118.868f + aytMatNet * 4.70f + aytEdebiyatNet * 4.70f + aytTarih1Net * 4.38f + aytCografya1Net * 5.22f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                        2024 -> {
                            val aytScore = clampScore(119.15f + aytMatNet * 4.80f + aytEdebiyatNet * 4.80f + aytTarih1Net * 4.50f + aytCografya1Net * 5.20f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                        else -> {
                            val aytScore = clampScore(119.00f + aytMatNet * 5.00f + aytEdebiyatNet * 5.00f + aytTarih1Net * 4.667f + aytCografya1Net * 5.556f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                    }

                    val sozScore = when (year) {
                        2023 -> {
                            val aytScore = clampScore(118.868f + aytEdebiyatNet * 4.70f + aytTarih1Net * 4.38f + aytCografya1Net * 5.22f + aytTarih2Net * 4.57f + aytCografya2Net * 4.57f + aytFelsefeNet * 4.70f + aytDinNet * 5.22f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                        2024 -> {
                            val aytScore = clampScore(119.15f + aytEdebiyatNet * 4.80f + aytTarih1Net * 4.50f + aytCografya1Net * 5.20f + aytTarih2Net * 4.70f + aytCografya2Net * 4.70f + aytFelsefeNet * 4.80f + aytDinNet * 5.20f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                        else -> {
                            val aytScore = clampScore(119.00f + aytEdebiyatNet * 5.00f + aytTarih1Net * 4.667f + aytCografya1Net * 5.556f + aytTarih2Net * 4.85f + aytCografya2Net * 4.85f + aytFelsefeNet * 5.00f + aytDinNet * 5.55f)
                            clampScore(tytScore * 0.4f + aytScore * 0.6f)
                        }
                    }

                    val obpContribution = obp * 0.6f
                    val yTytScore = tytScore + obpContribution
                    val ySayScore = sayScore + obpContribution
                    val yEaScore = eaScore + obpContribution
                    val ySozScore = sozScore + obpContribution

                    val tytRank = interpolateRank(yigilmaList, "TYT", year, tytScore, false)
                    val yTytRank = interpolateRank(yigilmaList, "TYT", year, yTytScore, true)

                    val sayRank = interpolateRank(yigilmaList, "SAY", year, sayScore, false)
                    val ySayRank = interpolateRank(yigilmaList, "SAY", year, ySayScore, true)

                    val eaRank = interpolateRank(yigilmaList, "EA", year, eaScore, false)
                    val yEaRank = interpolateRank(yigilmaList, "EA", year, yEaScore, true)

                    val sozRank = interpolateRank(yigilmaList, "SOZ", year, sozScore, false)
                    val ySozRank = interpolateRank(yigilmaList, "SOZ", year, ySozScore, true)

                    yearObj.put("TYT_Score", tytScore.toDouble())
                    yearObj.put("SAY_Score", sayScore.toDouble())
                    yearObj.put("EA_Score", eaScore.toDouble())
                    yearObj.put("SOZ_Score", sozScore.toDouble())
                    yearObj.put("Y_TYT_Score", yTytScore.toDouble())
                    yearObj.put("Y_SAY_Score", ySayScore.toDouble())
                    yearObj.put("Y_EA_Score", yEaScore.toDouble())
                    yearObj.put("Y_SOZ_Score", ySozScore.toDouble())

                    yearObj.put("TYT_Rank", tytRank)
                    yearObj.put("Y_TYT_Rank", yTytRank)
                    yearObj.put("SAY_Rank", sayRank)
                    yearObj.put("Y_SAY_Rank", ySayRank)
                    yearObj.put("EA_Rank", eaRank)
                    yearObj.put("Y_EA_Rank", yEaRank)
                    yearObj.put("SOZ_Rank", sozRank)
                    yearObj.put("Y_SOZ_Rank", ySozRank)

                    resultsObj.put(year.toString(), yearObj)
                }

                val inputsObj = JSONObject()
                inputs.forEach { (subject, pair) ->
                    val pairObj = JSONObject()
                    pairObj.put("d", pair.first.toDouble())
                    pairObj.put("y", pair.second.toDouble())
                    inputsObj.put(subject, pairObj)
                }

                val inputsJson = inputsObj.toString()
                val resultsJson = resultsObj.toString()

                val entity = CalculationHistoryEntity(
                    obp = obp,
                    inputsJson = inputsJson,
                    resultsJson = resultsJson,
                    date = System.currentTimeMillis()
                )
                yksDao.insertCalculationHistory(entity)

                val token = accessToken.value
                if (token != null) {
                    com.omerfaruk.ykstakip.data.SupabaseRepository.syncCalculation(token, obp, inputsJson, resultsJson)
                }

                onComplete(true, null)
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "Hesaplama hatası", e)
                onComplete(false, e.message)
            }
        }
    }

    fun deleteCalculationHistory(id: Int) {
        viewModelScope.launch {
            yksDao.deleteCalculationHistoryById(id)
        }
    }

    fun restoreCalculationsFromCloud() {
        viewModelScope.launch {
            val token = accessToken.value ?: return@launch
            try {
                if (yksDao.getAllCalculationHistory().first().isNotEmpty()) {
                    android.util.Log.d("YksViewModel", "Local calculation history not empty, skipping restore.")
                    return@launch
                }
                val cloudCalculations = com.omerfaruk.ykstakip.data.SupabaseRepository.fetchAllCalculations(token)
                if (cloudCalculations.isNotEmpty()) {
                    for (calc in cloudCalculations) {
                        yksDao.insertCalculationHistory(calc)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YksViewModel", "Buluttan hesaplamalar geri yuklenemedi", e)
            }
        }
    }

    private fun interpolateRank(
        yigilmaList: List<SupabaseYigilma>,
        puanTuru: String,
        yil: Int,
        score: Float,
        isPlacement: Boolean
    ): Int {
        val actualPuanTuru = if (isPlacement) {
            val prefixOptions = listOf("Y-$puanTuru", "Y_$puanTuru", "Y$puanTuru")
            prefixOptions.find { option ->
                yigilmaList.any { it.puanTuru.equals(option, ignoreCase = true) && it.yil == yil }
            } ?: "Y-$puanTuru"
        } else {
            puanTuru
        }

        val points = yigilmaList.filter { it.puanTuru.equals(actualPuanTuru, ignoreCase = true) && it.yil == yil }
            .sortedBy { it.puan }

        if (points.isEmpty()) {
            return estimateRankFallback(puanTuru, score, yil, isPlacement)
        }

        val target = score

        if (target <= points.first().puan) {
            return points.first().siralama
        }
        if (target >= points.last().puan) {
            return points.last().siralama
        }

        for (i in 0 until points.size - 1) {
            val pA = points[i]
            val pB = points[i + 1]
            if (target >= pA.puan && target <= pB.puan) {
                val diffPuan = pB.puan - pA.puan
                if (diffPuan == 0f) return pA.siralama
                val diffSiralama = pB.siralama - pA.siralama
                val rank = pA.siralama + (target - pA.puan) * diffSiralama / diffPuan
                return rank.toInt()
            }
        }

        return points.last().siralama
    }

    private fun estimateRankFallback(puanTuru: String, targetScore: Float, yil: Int, isPlacement: Boolean): Int {
        val maxPoints = if (isPlacement) 560f else 500f
        val minPoints = 100f
        val pct = ((maxPoints - targetScore) / (maxPoints - minPoints)).coerceIn(0f, 1f)
        
        val maxRank = when (puanTuru) {
            "TYT" -> 3000000.0
            "SAY" -> 1500000.0
            "EA" -> 1800000.0
            "SOZ" -> 1600000.0
            else -> 2000000.0
        }
        
        val exponent = when (yil) {
            2023 -> 3.6
            2024 -> 3.3
            2025 -> 3.5
            else -> 3.5
        }
        
        val rank = Math.pow(pct.toDouble(), exponent) * maxRank
        return rank.toInt().coerceAtLeast(1)
    }

    private fun getNet(inputs: Map<String, Pair<Float, Float>>, subject: String): Float {
        val pair = inputs[subject] ?: return 0f
        return pair.first - 0.25f * pair.second
    }

    private fun clampScore(score: Float): Float {
        return score.coerceIn(100f, 500f)
    }
}

data class SubjectUiModel(
    val name: String,
    val progress: Float,
    val topics: List<TopicEntity>,
    val category: String
)
