<div align="center">

# 📚 YKS Takip

**YKS sınavına hazırlık sürecini profesyonelce yönetmenizi sağlayan kapsamlı Android uygulaması.**

Konu takibi, net hesaplama, çalışma süreleri, Pomodoro zamanlayıcı, AI motivasyon koçu ve daha fazlası — hepsi tek bir uygulamada.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-Backend-3FCF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![Room](https://img.shields.io/badge/Room-Local_DB-FF6F00?logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Gemini AI](https://img.shields.io/badge/Gemini_AI-1.5_Flash-8E75B2?logo=googlegemini&logoColor=white)](https://ai.google.dev/)
[![Min SDK](https://img.shields.io/badge/Min_SDK-27-green)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📸 Screenshots

> ⚠️ Ekran görüntüleri yakında eklenecektir.

<!--
Ekran görüntülerini `screenshots/` klasörüne ekledikten sonra aşağıdaki satırları aktif edin:

<div align="center">
  <img src="screenshots/dashboard.png" width="200" />
  <img src="screenshots/subjects.png" width="200" />
  <img src="screenshots/pomodoro.png" width="200" />
  <img src="screenshots/profile.png" width="200" />
</div>
-->

---

## ✨ Features

### 📋 Konu Takip Sistemi
- **TYT & AYT** tüm derslerin konuları önceden yüklenmiş (Matematik, Fizik, Kimya, Biyoloji, Türkçe, Geometri, Tarih, Coğrafya, Felsefe, Edebiyat, Din Kültürü ve dahası)
- **Sayısal, Eşit Ağırlık ve Sözel** alan desteği
- Konu tamamlama durumunu işaretle, genel ilerlemeyi takip et
- Ders bazlı ilerleme çubukları ve yüzde gösterimi

### ⏱️ Pomodoro & Kronometre
- Yerleşik Pomodoro zamanlayıcısı ile odaklanma seansları
- Ayrı kronometre modu
- Çalışma sürelerini ders ve konu bazlı kaydetme
- Geçmiş çalışma istatistikleri

### 📊 Net Takibi & Sıralama Hesaplama
- TYT, AYT ve branş bazlı net sonuçlarını kaydetme
- Net gelişim grafiği ile ilerlemenizi görselleştirme
- **YKS sıralama hesaplama** — OBP, TYT ve AYT netleriyle tahmini sıralama
- Yığılma verileri ile desteklenen hesaplamalar (Supabase üzerinden)
- Hesaplama geçmişi

### ❌ Yanlış Soru Takibi
- Yanlış soruların fotoğrafını çekerek kaydetme
- Ders ve sınav türüne göre (TYT/AYT) filtreleme
- Çözüldü olarak işaretleme

### 💊 Hap Bilgi (Knowledge Snippets)
- Supabase'deki konu tabanlı kısa bilgi kartları
- Beğen, Biliyorum ve Kaydet etkileşimleri
- Ders bazlı snippet filtreleme
- Kaydedilenler, beğenilenler ve bilinenler ayrı ekranlarda

### 🤖 AI Motivasyon Koçu
- **Gemini 1.5 Flash** ile kişiselleştirilmiş motivasyon mesajları
- İlerleme durumunuza göre dinamik öneriler
- Kullanıcıya belirlediği hitap şekliyle seslenme

> **Not:** Gemini AI özelliği henüz geliştirme aşamasındadır ve tam stabil çalışmayabilir. Kullanmak için `local.properties` dosyasına `GEMINI_API_KEY` eklenmelidir.

### 🔔 Bildirim Sistemi
- Gün ve saat bazlı özelleştirilebilir hatırlatıcılar
- YKS'ye kalan gün sayısını gösteren bildirimler
- Haftalık tekrarlayan alarm desteği

### 🎖️ Ödül / Başarım Sistemi
- Konu tamamlama ve çalışma hedeflerine göre otomatik ödüller
- Acemi → Tecrübeli → Uzman seviyeleri
- Yeni ödül kazanıldığında Toast bildirimi

### 👤 Profil & Ayarlar
- Kullanıcı profili: Ad, soyad, unvan, alan, sınav yılı, OBP
- Kişiselleştirilebilir hitap şekli (ör: "Mühendis Faruk")
- Light / Dark / System tema desteği
- Ayarlanabilir yazı boyutu
- Veri sıfırlama

### ☁️ Bulut Senkronizasyonu
- Supabase Auth ile kullanıcı kimlik doğrulama
- Konu ilerlemeleri, çalışma süreleri, net sonuçları, snippet etkileşimleri buluta yedeklenir
- Cihaz değişikliğinde veriler otomatik geri yüklenir

---

## 🛠️ Tech Stack

| Katman | Teknoloji |
|--------|-----------|
| **Dil** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Mimari** | MVVM (ViewModel + StateFlow) |
| **Lokal Veritabanı** | Room (SQLite) |
| **Tercihler** | DataStore Preferences |
| **Backend / Auth** | Supabase (PostgreSQL + GoTrue Auth) |
| **AI** | Google Gemini 1.5 Flash |
| **Navigation** | Jetpack Navigation Compose |
| **Splash Screen** | AndroidX Core Splash Screen |
| **Bildirimler** | AlarmManager + NotificationManager |
| **Min SDK** | 27 (Android 8.1) |
| **Target SDK** | 35 |

---

## 🏗️ Architecture / Technical Details

Proje **MVVM (Model-View-ViewModel)** mimarisini takip eder ve tamamen **Jetpack Compose** ile oluşturulmuştur.

```
com.omerfaruk.ykstakip/
├── MainActivity.kt              # Navigation host, tema, factory
├── ai/
│   └── GeminiService.kt         # Gemini AI motivasyon servisi
├── data/
│   ├── SupabaseRepository.kt    # Supabase REST API (Auth, CRUD, Sync)
│   ├── local/
│   │   ├── YksDatabase.kt       # Room DB, Entity'ler, DAO
│   │   ├── PreferenceManager.kt # DataStore ile tercihler & auth token
│   │   ├── Reward.kt            # Ödül data class
│   │   └── CalculationHistoryEntity.kt
│   └── model/
│       └── Models.kt            # Subject & Topic domain modelleri
├── notification/
│   ├── NotificationHelper.kt    # Alarm planlama & kanal oluşturma
│   └── NotificationReceiver.kt  # BroadcastReceiver
└── ui/
    ├── Screens.kt               # Tüm Composable ekranlar (~8200 satır)
    └── YksViewModel.kt          # İş mantığı, state yönetimi, sync
```

### Öne Çıkan Teknik Detaylar

- **Çift Katmanlı Veri Stratejisi:** Offline-first yaklaşım. Veriler önce Room'a yazılır, ardından Supabase'e senkronize edilir. Uygulama açıldığında buluttan geri yükleme yapılır.
- **Supabase REST API:** HTTP bağlantıları `HttpURLConnection` ile doğrudan yapılır — ek SDK bağımlılığı yok.
- **Reactive UI:** `StateFlow` ve `Flow` ile tüm veri değişiklikleri gerçek zamanlı olarak UI'ya yansır.
- **Dinamik Tipografi:** Kullanıcı font boyutu çarpanını ayarlayabilir, tüm `Typography` seviyelerine uygulanır.
- **Güvenli API Key Yönetimi:** Hassas anahtarlar `local.properties` → `BuildConfig` üzerinden derleme zamanında enjekte edilir; kaynak kodda hiçbir secret hardcode edilmez.

---

## 🗄️ Database

### Lokal Veritabanı (Room)

Uygulama 8 tabloluk bir Room (SQLite) veritabanı kullanır:

```
┌─────────────────────┐     ┌──────────────────────┐
│       topics        │     │     net_results       │
├─────────────────────┤     ├──────────────────────┤
│ id (PK, auto)       │     │ id (PK, auto)        │
│ subjectName         │     │ date                  │
│ title               │     │ type (TYT/AYT/BRANS) │
│ isCompleted         │     │ totalNet              │
│ category            │     │ details               │
│ (unique: subjectName│     └──────────────────────┘
│  + title + category)│
└─────────────────────┘
                              ┌──────────────────────┐
┌─────────────────────┐       │   wrong_questions     │
│    study_times      │       ├──────────────────────┤
├─────────────────────┤       │ id (PK, auto)        │
│ id (PK, auto)       │       │ subjectName          │
│ date                │       │ examType (TYT/AYT)   │
│ subjectName         │       │ topicTitle            │
│ topicTitle          │       │ imagePath             │
│ durationSeconds     │       │ isSolved              │
└─────────────────────┘       │ addedAt               │
                              └──────────────────────┘

┌──────────────────────┐     ┌──────────────────────┐
│snippet_interactions  │     │  followed_subjects    │
├──────────────────────┤     ├──────────────────────┤
│ snippetId (PK)       │     │ subjectName (PK)     │
│ topicId              │     │ isFollowed            │
│ subjectName          │     │ updatedAt             │
│ isLiked              │     └──────────────────────┘
│ isKnown              │
│ isSaved              │     ┌──────────────────────┐
│ updatedAt            │     │  question_logs        │
└──────────────────────┘     ├──────────────────────┤
                             │ id (PK, auto)        │
┌──────────────────────┐     │ date                  │
│calculation_history   │     │ subjectName           │
├──────────────────────┤     │ topicTitle             │
│ id (PK, auto)        │     │ correctCount          │
│ date                 │     │ wrongCount             │
│ obp                  │     │ updatedAt              │
│ inputsJson           │     └──────────────────────┘
│ resultsJson          │
└──────────────────────┘
```

### Bulut Veritabanı (Supabase / PostgreSQL)

Supabase üzerinde aşağıdaki tablolar mevcuttur:

| Tablo | Açıklama |
|-------|----------|
| `topics` | TYT/AYT konu havuzu |
| `snippets` | Konu bazlı bilgi kartları (Hap Bilgi) |
| `profiles` | Kullanıcı profil bilgileri |
| `user_topic_progress` | Konu tamamlama ilerlemeleri |
| `user_study_times` | Çalışma süreleri |
| `user_net_results` | Net sonuçları |
| `user_calculations` | Sıralama hesaplama geçmişi |
| `user_snippet_interactions` | Snippet beğeni/kaydet/biliyorum |
| `user_followed_subjects` | Takip edilen dersler |
| `user_question_logs` | Soru çözüm logları |
| `yigilma_verileri` | Yığılma/sıralama verileri |

> ⚠️ **Güvenlik Notu:** Supabase URL, API Key, Gemini API Key gibi hassas bilgiler `local.properties` dosyasında tutulur ve `.gitignore` ile versiyon kontrolünden hariç tutulmuştur. Bu repo'da hiçbir secret paylaşılmamaktadır.

---

## 🚀 Installation

### Gereksinimler
- Android Studio Koala (2024.1.1) veya üzeri
- JDK 11+
- Android SDK 35
- Minimum SDK 27 destekli bir cihaz veya emülatör

### Adımlar

```bash
# 1. Repoyu klonlayın
git clone https://github.com/Taruq157/yks-takip.git
cd yks-takip

# 2. local.properties dosyasını oluşturun
#    (Bu dosya .gitignore'da olduğu için repoda yer almaz)
```

`local.properties` dosyasına aşağıdaki anahtarları ekleyin:

```properties
sdk.dir=C\:\\Users\\KULLANICI_ADINIZ\\AppData\\Local\\Android\\Sdk

# Supabase (zorunlu — bulut senkronizasyonu ve auth için)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your_supabase_anon_key

# Gemini AI (opsiyonel — motivasyon koçu özelliği için)
GEMINI_API_KEY=your_gemini_api_key
```

```bash
# 3. Projeyi build edin ve çalıştırın
#    Android Studio'da "Run" butonuna basın
#    veya terminal üzerinden:
./gradlew assembleDebug
```

> **Not:** Supabase anahtarları olmadan uygulama çalışır ancak bulut senkronizasyonu ve auth özellikleri devre dışı kalır. Gemini API Key olmadan AI motivasyon özelliği fallback mesajlar gösterir.

---

## 📱 Ekranlar

| Ekran | Açıklama |
|-------|----------|
| `AuthScreen` | Supabase giriş/kayıt ekranı |
| `OnboardingScreen` | Profil oluşturma (ad, alan, unvan, sınav yılı) |
| `MainDashboard` | Ana ekran — ilerleme, dersler, AI motivasyon |
| `SubjectDetailScreen` | Ders konuları ve tamamlama durumları |
| `HapScreen` | Bilgi kartları (snippets) ana ekranı |
| `SubjectSnippetsScreen` | Ders bazlı snippet listesi |
| `ProfileScreen` | Kullanıcı profili ve ödüller |
| `PomodoroScreen` | Pomodoro zamanlayıcısı |
| `StopwatchScreen` | Kronometre |
| `NetTrackingScreen` | Net sonuçları takibi |
| `ScoreCalculationScreen` | YKS sıralama hesaplama |
| `WrongQuestionsScreen` | Yanlış soru takibi |
| `ExamTimerScreen` | Sınav zamanlayıcısı |
| `QuestionTrackingScreen` | Soru çözüm takibi |
| `SettingsScreen` | Tema, font, bildirim, veri sıfırlama |
| `RewardsScreen` | Başarım / ödül galerisi |

---

## 🤝 Contributing

Pull request'ler memnuniyetle karşılanır. Büyük değişiklikler için lütfen önce bir issue açarak neyi değiştirmek istediğinizi tartışın.

---

## 📄 License

Bu proje [MIT](LICENSE) lisansı ile lisanslanmıştır.

---

<div align="center">

**YKS Takip** ile hedefine bir adım daha yaklaş! 🎯

*Geliştirici: [Ömer Faruk](https://github.com/Taruq157)*

</div>
