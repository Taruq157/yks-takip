package com.omerfaruk.ykstakip

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.omerfaruk.ykstakip.data.local.PreferenceManager
import com.omerfaruk.ykstakip.data.local.TopicEntity
import com.omerfaruk.ykstakip.data.local.YksDao
import com.omerfaruk.ykstakip.data.local.YksDatabase
import com.omerfaruk.ykstakip.notification.NotificationHelper
import com.omerfaruk.ykstakip.ui.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class YksViewModelFactory(
    private val yksDao: YksDao,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return YksViewModel(yksDao, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, YksDatabase::class.java, "yks-db")
            .fallbackToDestructiveMigration()
            .build()
        val prefManager = PreferenceManager(applicationContext)
        val notifHelper = NotificationHelper(applicationContext)
        notifHelper.createNotificationChannel()

        setContent {
            val viewModel: YksViewModel = viewModel(
                factory = YksViewModelFactory(db.yksDao(), prefManager)
            )
            
            val userInfoFlow = remember { viewModel.userInfo }
            val themeMode by userInfoFlow.map { "System" }.collectAsState(initial = "System") 
            val themeModeSettingsFlow = remember { prefManager.themeMode }
            val currentThemeMode by themeModeSettingsFlow.collectAsState(initial = "System")

            val fontSizeMultiplier by prefManager.fontSizeMultiplier.collectAsState(initial = 1.0f)
            val notifSettings by prefManager.notificationSettings.collectAsState(initial = null)
            
            val isDarkTheme = when (currentThemeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            val baseTypography = Typography()
            val dynamicTypography = Typography(
                displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontSizeMultiplier),
                displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontSizeMultiplier),
                displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontSizeMultiplier),
                headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontSizeMultiplier),
                headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontSizeMultiplier),
                headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * fontSizeMultiplier),
                titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontSizeMultiplier),
                titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontSizeMultiplier),
                titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * fontSizeMultiplier),
                bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontSizeMultiplier),
                bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontSizeMultiplier),
                bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * fontSizeMultiplier),
                labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontSizeMultiplier),
                labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * fontSizeMultiplier),
                labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * fontSizeMultiplier)
            )

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted && notifSettings != null) {
                    notifHelper.scheduleNotifications(notifSettings!!)
                }
            }

            LaunchedEffect(Unit) {
                viewModel.newRewardAchieved.collectLatest { reward ->
                    Toast.makeText(this@MainActivity, "🏆 Yeni Ödül: ${reward.title.replace("\n", " ")} alındı!", Toast.LENGTH_LONG).show()
                }
            }

            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
                typography = dynamicTypography
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val accessToken by viewModel.accessToken.collectAsState()
                    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
                    val userInfo by viewModel.userInfo.collectAsState()
                    val overallProgress by viewModel.overallProgress.collectAsState()
                    val subjects by viewModel.subjectsWithProgress.collectAsState()
                    val rewards by viewModel.rewards.collectAsState()
                    
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        val currentTopics = db.yksDao().getAllTopics().first()
                        if (currentTopics.size < 150) { 
                            db.yksDao().deleteAllTopics()
                            db.yksDao().insertTopics(getInitialData())
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = if (accessToken == null) "auth" else if (isOnboardingCompleted) "dashboard" else "onboarding"
                        ) {
                            composable("auth") {
                                AuthScreen(
                                    onLoginSuccess = { access, refresh ->
                                        scope.launch {
                                            val currentTopics = db.yksDao().getAllTopics().first()
                                            if (currentTopics.size < 150) {
                                                db.yksDao().deleteAllTopics()
                                                db.yksDao().insertTopics(getInitialData())
                                            }
                                            viewModel.saveAuthTokens(access, refresh) { hasProfile ->
                                                navController.navigate(if (hasProfile || isOnboardingCompleted) "dashboard" else "onboarding") {
                                                    popUpTo("auth") { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            composable("onboarding") {
                                OnboardingScreen { fName, lName, title, major, displayName, examYear, obp ->
                                    viewModel.saveUserInfo(fName, lName, title, major, displayName, examYear, obp) { success, error ->
                                        if (success) {
                                            Toast.makeText(this@MainActivity, "Profil kaydedildi!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@MainActivity, "Hata: $error", Toast.LENGTH_LONG).show()
                                        }
                                        navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } }
                                    }
                                }
                            }
                            composable("dashboard") {
                                MainDashboard(
                                     userInfo = userInfo,
                                     overallProgress = overallProgress,
                                     subjects = subjects,
                                     notifSettings = notifSettings,
                                     newRewardFlow = viewModel.newRewardAchieved,
                                     allStudyTimes = viewModel.allStudyTimes.collectAsState().value,
                                     allQuestionLogs = viewModel.allQuestionLogs.collectAsState().value,
                                     allTopics = viewModel.allTopics.collectAsState().value,
                                     onSubjectClick = { subjectName, category ->
                                         navController.navigate("detail/$subjectName/$category")
                                     },
                                     onUpdateNotif = { settings ->
                                         scope.launch { 
                                             prefManager.updateNotificationSettings(settings)
                                             if (settings.isEnabled) {
                                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                     permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                 } else {
                                                     notifHelper.scheduleNotifications(settings)
                                                 }
                                             } else {
                                                 notifHelper.scheduleNotifications(settings)
                                             }
                                         }
                                     },
                                     onAddQuestionLog = { subject, topic, correct, wrong ->
                                         viewModel.addQuestionLog(subject, topic, correct, wrong)
                                     }
                                 )
                            }
                            composable("hap") {
                                HapScreen(
                                    viewModel = viewModel,
                                    onSubjectClick = { subjectName ->
                                        navController.navigate("subject_snippets/$subjectName")
                                    },
                                    onNavigateToSaved = {
                                        navController.navigate("saved_snippets")
                                    },
                                    onNavigateToLiked = {
                                        navController.navigate("liked_snippets")
                                    },
                                    onNavigateToKnown = {
                                        navController.navigate("known_snippets")
                                    }
                                )
                            }
                            composable(
                                "subject_snippets/{subjectName}",
                                arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                                SubjectSnippetsScreen(
                                    viewModel = viewModel,
                                    subjectName = subjectName,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSaved = {
                                        navController.navigate("saved_snippets")
                                    },
                                    onNavigateToLiked = {
                                        navController.navigate("liked_snippets")
                                    },
                                    onNavigateToKnown = {
                                        navController.navigate("known_snippets")
                                    }
                                )
                            }
                            composable("saved_snippets") {
                                SavedSnippetsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("liked_snippets") {
                                LikedSnippetsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("known_snippets") {
                                KnownSnippetsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                             composable("profile") {
                                 ProfileScreen(
                                     userInfo = userInfo,
                                     overallProgress = overallProgress,
                                     rewards = rewards,
                                     onEditClick = { navController.navigate("edit_profile") },
                                     onRewardsClick = { navController.navigate("rewards") },
                                     onLogoutClick = {
                                         viewModel.logout {
                                             navController.navigate("auth") {
                                                 popUpTo(0) { inclusive = true }
                                             }
                                         }
                                     }
                                 )
                             }
                            composable("apps") {
                                ExtraAppsScreen(onAppClick = { route ->
                                    navController.navigate(route)
                                })
                            }
                            composable("score_calculation") {
                                ScoreCalculationScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("pomodoro") {
                                PomodoroScreen(
                                    onBack = { navController.popBackStack() },
                                    onSaveTime = { totalSeconds -> navController.navigate("save_time/$totalSeconds") }
                                )
                            }
                            composable("stopwatch") {
                                StopwatchScreen(
                                    onBack = { navController.popBackStack() },
                                    onSaveTime = { totalSeconds -> navController.navigate("save_time/$totalSeconds") }
                                )
                            }
                            composable(
                                "save_time/{totalSeconds}",
                                arguments = listOf(navArgument("totalSeconds") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val totalSeconds = backStackEntry.arguments?.getLong("totalSeconds") ?: 0L
                                SaveTimeScreen(
                                    viewModel = viewModel,
                                    totalSeconds = totalSeconds,
                                    onBack = { navController.popBackStack() },
                                    onSave = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                             composable("net_tracking") {
                                 NetTrackingScreen(viewModel = viewModel, userInfo = userInfo, onBack = { navController.popBackStack() })
                             }
                             composable("question_tracking") {
                                 QuestionTrackingScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                             }
                            composable("wrong_questions") {
                                WrongQuestionsScreen(viewModel = viewModel, userInfo = userInfo, onBack = { navController.popBackStack() })
                            }
                            composable("exam_timer") {
                                ExamTimerScreen(onBack = { navController.popBackStack() })
                            }
                            composable("edit_profile") {
                                EditProfileScreen(
                                    userInfo = userInfo,
                                    onSave = { fName, lName, title, major, displayName, examYear, obp ->
                                        viewModel.saveUserInfo(fName, lName, title, major, displayName, examYear, obp) { success, error ->
                                            if (success) {
                                                Toast.makeText(this@MainActivity, "Profil kaydedildi!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@MainActivity, "Hata: $error", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("rewards") {
                                RewardsScreen(
                                    rewards = rewards,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    currentTheme = currentThemeMode,
                                    onThemeChange = { mode ->
                                        scope.launch { prefManager.setThemeMode(mode) }
                                    },
                                    currentFontSizeMultiplier = fontSizeMultiplier,
                                    onFontSizeChange = { multiplier ->
                                        scope.launch { prefManager.setFontSizeMultiplier(multiplier) }
                                    },
                                    onResetData = {
                                        scope.launch {
                                            db.yksDao().deleteAllTopics()
                                            db.yksDao().insertTopics(getInitialData())
                                        }
                                    }
                                )
                            }
                            composable(
                                route = "detail/{subjectName}/{category}",
                                arguments = listOf(
                                    navArgument("subjectName") { type = NavType.StringType },
                                    navArgument("category") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                                val category = backStackEntry.arguments?.getString("category") ?: ""
                                val topics by viewModel.allTopics.collectAsState()
                                val filteredTopics = topics.filter { it.subjectName == subjectName && it.category == category }
                                
                                SubjectDetailScreen(
                                    subjectName = subjectName,
                                    topics = filteredTopics,
                                    allStudyTimes = viewModel.allStudyTimes.collectAsState().value,
                                    onToggleTopic = { topic ->
                                        viewModel.toggleTopicCompletion(topic)
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Alt Navigasyon Çubuğu (Belirli ekranlarda göster)
                        val mainScreens = listOf("dashboard", "hap", "profile", "apps", "settings")
                        if (isOnboardingCompleted && currentRoute in mainScreens) {
                            FloatingNavbar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getInitialData(): List<TopicEntity> {
        val topics = mutableListOf<TopicEntity>()
        
        // TYT Subjects
        val tytMat = listOf("Temel Kavramlar", "Sayı Basamakları", "Bölme ve Bölünebilme", "EBOB-EKOK", "Rasyonel Sayılar", "Basit Eşitsizlikler", "Mutlak Değer", "Üslü Sayılar", "Köklü Sayılar", "Çarpanlara Ayırma", "Oran-Orantı", "Denklem Çözme", "Problemler", "Kümeler", "Fonksiyonlar", "Permütasyon-Kombinasyon", "Olasılık", "İstatistik", "Polinomlar")
        val tytTur = listOf("Sözcükte Anlam", "Cümlede Anlam", "Paragraf", "Ses Bilgisi", "Yazım Kuralları", "Noktalama İşaretleri", "Sözcük Yapısı", "Sözcük Türleri", "Cümlenin Ögeleri", "Fiiller", "Anlatım Bozukluğu")
        val tytFiz = listOf("Fizik Bilimine Giriş", "Madde ve Özellikleri", "Hareket ve Kuvvet", "Enerji", "Isı ve Sıcaklık", "Elektrostatik", "Elektrik ve Manyetizma", "Basinç ve Kaldırma Kuvveti", "Dalgalar", "Optik")
        val tytGeo = listOf("Doğruda ve Üçgende Açılar", "Dik Üçgen", "İkizkenar ve Eşkenar Üçgen", "Üçgende Alan ve Benzerlik", "Açı Kenar Bağıntıları", "Çokgenler", "Dörtgenler", "Yamuk", "Paralelkenar", "Eşkenar Dörtgen", "Dikdörtgen", "Kare", "Deltoid", "Çember ve Daire", "Analitik Geometri", "Katı Cisimler")
        val tytKim = listOf("Kimya Bilimi", "Atom ve Periyodik Sistem", "Kimyasal Türler Arası Etkileşimler", "Maddenin Halleri", "Doğa ve Kimya", "Kimyanın Temel Kanunları", "Karışımlar", "Asitler, Bazlar ve Tuzlar", "Kimya Her Yerde")
        val tytBiy = listOf("Canlıların Ortak Özellikleri", "Canlıların Temel Bileşenleri", "Hücre", "Canlılar Dünyası", "Hücre Bölünmeleri", "Kalıtım", "Ekosistem Ekolojisi ve Güncel Çevre Sorunları")
        val tytTar = listOf("Tarih ve Zaman", "İnsanlığın İlk Dönemleri", "Orta Çağ’da Dünya", "İlk ve Orta Çağlarda Türk Dünyası", "İslam Medeniyetinin Doğuşu", "İlk Türk İslam Devletleri", "Yerleşme ve Devletleşme Sürecinde Selçuklu", "Beylikten Devlete Osmanlı", "Dünya Gücü Osmanlı", "Yeni Çağda Avrupa", "Yakın Çağda Avrupa", "Osmanlı Kültür ve Medeniyeti", "19. Yüzyılda Osmanlı", "20. Yüzyıl Başlarında Osmanlı", "Birinci Dünya Savaşı", "Kurtuluş Savaşı Hazırlık Dönemi", "Milli Mücede", "Atatürkçülük ve Atatürk İlkeleri")
        val tytCog = listOf("Doğa ve İnsan", "Dünya’nın Şekli ve Hareketleri", "Yer ve Zaman", "Harita Bilgisi", "Atmosfer ve İklim", "Yerin Şekillenmesi", "Doğanın Varlıkları", "Beşeri Sistemler", "Bölgeler ve Ülkeler", "Çevre ve Toplum")
        val tytFel = listOf("Felsefe ile Tanışma", "Bilgi Felsefesi", "Varlık Felsefesi", "Ahlak Felsefesi", "Sanat Felsefesi", "Din Felsefesi", "Siyaset Felsefesi", "Bilim Felsefesi")
        val tytDin = listOf("Bilgi ve İnanç", "Din ve İslam", "İslam ve İbadet", "Gençlik ve Değerler", "Allah İnsan İlişkisi", "Hz. Muhammed ve Gençlik", "Din ve Hayat", "İslam Düşüncesinde Yorumlar")

        tytMat.forEach { topics.add(TopicEntity(title = it, subjectName = "Matematik", category = "TYT")) }
        tytTur.forEach { topics.add(TopicEntity(title = it, subjectName = "Türkçe", category = "TYT")) }
        tytFiz.forEach { topics.add(TopicEntity(title = it, subjectName = "Fizik", category = "TYT")) }
        tytGeo.forEach { topics.add(TopicEntity(title = it, subjectName = "Geometri", category = "TYT")) }
        tytKim.forEach { topics.add(TopicEntity(title = it, subjectName = "Kimya", category = "TYT")) }
        tytBiy.forEach { topics.add(TopicEntity(title = it, subjectName = "Biyoloji", category = "TYT")) }
        tytTar.forEach { topics.add(TopicEntity(title = it, subjectName = "Tarih", category = "TYT")) }
        tytCog.forEach { topics.add(TopicEntity(title = it, subjectName = "Coğrafya", category = "TYT")) }
        tytFel.forEach { topics.add(TopicEntity(title = it, subjectName = "Felsefe", category = "TYT")) }
        tytDin.forEach { topics.add(TopicEntity(title = it, subjectName = "Din Kültürü", category = "TYT")) }

        // AYT Subjects (Categorized by Branch)
        val aytMat = listOf("Karmaşık Sayılar", "İkinci Dereceden Denklemler", "Parabol", "Eşitsizlikler", "Logaritma", "Diziler", "Trigonometri", "Limit ve Süreklilik", "Türev", "İntegral")
        val aytFiz = listOf("Vektörler", "Bağıl Hareket", "Newton'ın Hareket Yasaları", "Atışlar", "Enerji ve İş", "İtme ve Momentum", "Tork ve Denge", "Elektriksel Alan ve Potansiyel", "Manyetizma ve Elektromanyetik İndükleme", "Alternatif Akım", "Çembersel Hareket", "Basit Harmonik Hareket", "Modern Fizik")
        val aytKim = listOf("Modern Atom Teorisi", "Gazlar", "Sıvı Çözeltiler ve Çözünürlük", "Kimyasal Tepkimelerde Enerji", "Kimyasal Tepkimelerde Hız", "Kimyasal Tepkimelerde Hız", "Kimyasal Tepkimelerde Denge", "Asit-Baz Dengesi", "Çözünürlük Dengesi", "Kimya ve Elektrik", "Karbon Kimyasına Giriş", "Organik Bileşikler", "Enerji Kaynakları ve Bilimsel Gelişmeler")
        val aytBiy = listOf("Denetleyici ve Düzenleyici Sistem", "Duyu Organları", "Destek ve Hareket Sistemi", "Sindirim Sistemi", "Dolaşım ve Bağışıklık Sistemi", "Solunum Sistemi", "Boşaltım Sistemi", "Üreme Sistemi ve Embriyonik Gelişim", "Komünite ve Popülasyon Ekolojisi", "Genden Proteine", "Canlılarda Enerji Dönüşümleri", "Bitki Biyolojisi", "Canlılar ve Çevre")
        val aytEdb = listOf("Güzel Sanatlar ve Edebiyat", "Coşku ve Heyecanı Dile Getiren Metinler (Şiir)", "Olay Çevresinde Oluşan Edebi Metinler", "Öğretici Metinler", "Tanzimat Edebiyatı", "Servet-i Fünun Edebiyatı", "Fecr-i Ati Edebiyatı", "Milli Edebiyat", "Cumhuriyet Dönemi Edebiyatı", "Batı Edebiyatı")
        val aytTar = listOf("Tarih Bilimi", "Uygarlığın Doğuşu ve İlk Uygarlıklar", "İlk Türk Devletleri", "İslam Tarihi ve Medeniyeti", "Türk-İslam Devletleri", "Türkiye Tarihi", "Beylikten Devlete (1300-1453)", "Dünya Gücü: Osmanlı Devleti (1453-1600)", "Arayış Yılları (17. Yüzyıl)", "Avrupa ve Osmanlı Devleti (18. Yüzyıl)", "En Uzun Yüzyıl (1800-1922)", "20. Yüzyıl Başlarında Osmanlı Devleti", "1. Dünya Savaşı – Milli Mücede Hazırlık Dönemi", "Kurtuluş Savaşında Cepheler", "Türk İnkılabı", "Atatürkçülük ve Atatürk İlkeleri", "Atatürk Dönemi Türk Dış Politikası", "Atatürk'ün Ölümü", "Yüzyılın Eşiğinde Dünya ve Türkiye", "2. Dünya Savaşı", "Soğuk Savaş Dönemi", "Yumuşama Dönemi ve Sonrası", "Küreselleşen Dünya", "XXI. Yüzyılın Eşiğinde Türkiye ve Dünya")
        val aytCog = listOf("Doğa ve İnsan", "Ekosistem ve Madde Dönüşüsü", "Nüfus Politikaları", "Türkiye’de Nüfus ve Yerleşme", "Ekomik Faaliyetler", "Türkiye Ekonomisi", "Türkiye’nin Kültür Mirası", "Küresel Ortam: Bölgeler ve Ülkeler", "Çevre ve Toplum", "Beşeri Sistemler")
        val aytFel = listOf("Felsefenin Alanı", "Bilgi Felsefesi", "Varlık Felsefesi", "Ahlak Felsefesi", "Sanat Felsefesi", "Din Felsefesi", "Siyaset Felsefesi", "Bilim Felsefesi", "Mantığa Giriş", "Klasik Mantık", "Mantık ve Dil", "Sembolik Mantık", "Psikoloji Bilimini Tanıyalım", "Psikolojinin Temel Süreçleri", "Öğrenme Bellek Düşünme", "Ruh Sağlığının Temelleri", "Sosyolojiye Giriş", "Birey ve Toplum", "Toplumsal Yapı", "Toplumsal Değişme ve Gelişme", "Toplum ve Kültür", "Toplumsal Kurumlar")
        val aytDin = listOf("Dünya ve Ahiret", "Tövbe ve Bağışlama", "İslam ve İbadet", "İslam Düşüncesinde Yorumlar, Mezhepler", "İslam Medeniyetinin Analizi", "Güncel Dini Meseleler", "Hint ve Çin Dinleri", "Yahudilik ve Hristiyanlık")

        aytMat.forEach { topics.add(TopicEntity(title = it, subjectName = "Matematik", category = "AYT_MAT")) }
        aytFiz.forEach { topics.add(TopicEntity(title = it, subjectName = "Fizik", category = "AYT_FIZ")) }
        aytKim.forEach { topics.add(TopicEntity(title = it, subjectName = "Kimya", category = "AYT_KIM")) }
        aytBiy.forEach { topics.add(TopicEntity(title = it, subjectName = "Biyoloji", category = "AYT_BIY")) }
        aytEdb.forEach { topics.add(TopicEntity(title = it, subjectName = "Edebiyat", category = "AYT_EDB")) }
        aytTar.forEach { topics.add(TopicEntity(title = it, subjectName = "Tarih-1", category = "AYT_TAR1")) }
        aytCog.forEach { topics.add(TopicEntity(title = it, subjectName = "Coğrafya-1", category = "AYT_COG1")) }
        aytTar.forEach { topics.add(TopicEntity(title = it, subjectName = "Tarih-2", category = "AYT_TAR2")) }
        aytCog.forEach { topics.add(TopicEntity(title = it, subjectName = "Coğrafya-2", category = "AYT_COG2")) }
        aytFel.forEach { topics.add(TopicEntity(title = it, subjectName = "Felsefe Grubu", category = "AYT_FEL")) }
        aytDin.forEach { topics.add(TopicEntity(title = it, subjectName = "Din Kültürü", category = "AYT_DIN")) }
        
        // GEO topics for AYT
        tytGeo.forEach { topics.add(TopicEntity(title = it, subjectName = "Geometri", category = "AYT_GEO")) }

        return topics
    }
}

@Composable
fun FloatingNavbar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavigationItem("dashboard", Icons.Default.Home, "Anasayfa"),
        NavigationItem("hap", Icons.Default.CheckCircle, "Hap"),
        NavigationItem("profile", Icons.Default.Person, "Profil"),
        NavigationItem("apps", Icons.Default.Menu, "Ek Uygulamalar"),
        NavigationItem("settings", Icons.Default.Settings, "Ayarlar")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(32.dp)),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route || 
                        (item.route == "profile" && (currentRoute == "edit_profile" || currentRoute == "rewards"))
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onNavigate(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon!!,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val icon: ImageVector?,
    val label: String
)
