<div align="center">

# 📚 YKS Takip

**A comprehensive Android app for professionally managing your YKS (Turkish University Entrance Exam) preparation process.**

Topic tracking, net score calculation, study timers, Pomodoro, AI motivation coach, and more — all in one app.

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

> ⚠️ Screenshots will be added soon.

<!--
After adding screenshots to the `screenshots/` folder, uncomment the lines below:

<div align="center">
  <img src="screenshots/dashboard.png" width="200" />
  <img src="screenshots/subjects.png" width="200" />
  <img src="screenshots/pomodoro.png" width="200" />
  <img src="screenshots/profile.png" width="200" />
</div>
-->

---

## ✨ Features

### 📋 Topic Tracking System
- All **TYT & AYT** subject topics are pre-loaded (Mathematics, Physics, Chemistry, Biology, Turkish, Geometry, History, Geography, Philosophy, Literature, Religious Studies, and more)
- Support for **Science (Sayısal), Equal Weight (Eşit Ağırlık), and Verbal (Sözel)** tracks
- Mark topics as completed and track overall progress
- Per-subject progress bars with percentage indicators

### ⏱️ Pomodoro & Stopwatch
- Built-in Pomodoro timer for focused study sessions
- Separate stopwatch mode
- Save study durations by subject and topic
- Historical study statistics

### 📊 Net Score Tracking & Rank Estimation
- Record net scores for TYT, AYT, and individual subjects
- Visualize your progress with net score trend charts
- **YKS rank estimation** — calculate estimated ranking using GPA (OBP), TYT and AYT net scores
- Calculations supported by historical score distribution data (via Supabase)
- Calculation history

### ❌ Wrong Question Tracker
- Take photos of wrong questions and save them
- Filter by subject and exam type (TYT/AYT)
- Mark as solved

### 💊 Knowledge Snippets (Hap Bilgi)
- Topic-based short knowledge cards from Supabase
- Like, Mark as Known, and Save interactions
- Filter snippets by subject
- Separate screens for saved, liked, and known snippets

### 🤖 AI Motivation Coach
- Personalized motivation messages powered by **Gemini 1.5 Flash**
- Dynamic suggestions based on your current progress
- Addresses the user with their preferred title

> **Note:** The Gemini AI feature is still under development and may not be fully stable. To use it, add `GEMINI_API_KEY` to your `local.properties` file.

### 🔔 Notification System
- Customizable reminders by day and time
- Notifications showing days remaining until YKS
- Weekly recurring alarm support

### 🎖️ Achievement / Reward System
- Automatic rewards based on topic completion and study goals
- Beginner → Experienced → Expert levels
- Toast notification when a new reward is earned

### 👤 Profile & Settings
- User profile: First name, last name, title, track, exam year, GPA (OBP)
- Customizable display name (e.g., "Engineer Faruk")
- Light / Dark / System theme support
- Adjustable font size
- Data reset

### ☁️ Cloud Sync
- User authentication via Supabase Auth
- Topic progress, study times, net scores, and snippet interactions are backed up to the cloud
- Data is automatically restored on device change

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Local Database** | Room (SQLite) |
| **Preferences** | DataStore Preferences |
| **Backend / Auth** | Supabase (PostgreSQL + GoTrue Auth) |
| **AI** | Google Gemini 1.5 Flash |
| **Navigation** | Jetpack Navigation Compose |
| **Splash Screen** | AndroidX Core Splash Screen |
| **Notifications** | AlarmManager + NotificationManager |
| **Min SDK** | 27 (Android 8.1) |
| **Target SDK** | 35 |

---

## 🏗️ Architecture / Technical Details

The project follows the **MVVM (Model-View-ViewModel)** architecture and is built entirely with **Jetpack Compose**.

```
com.omerfaruk.ykstakip/
├── MainActivity.kt              # Navigation host, theming, factory
├── ai/
│   └── GeminiService.kt         # Gemini AI motivation service
├── data/
│   ├── SupabaseRepository.kt    # Supabase REST API (Auth, CRUD, Sync)
│   ├── local/
│   │   ├── YksDatabase.kt       # Room DB, Entities, DAO
│   │   ├── PreferenceManager.kt # DataStore preferences & auth tokens
│   │   ├── Reward.kt            # Reward data class
│   │   └── CalculationHistoryEntity.kt
│   └── model/
│       └── Models.kt            # Subject & Topic domain models
├── notification/
│   ├── NotificationHelper.kt    # Alarm scheduling & channel creation
│   └── NotificationReceiver.kt  # BroadcastReceiver
└── ui/
    ├── Screens.kt               # All Composable screens (~8200 lines)
    └── YksViewModel.kt          # Business logic, state management, sync
```

### Key Technical Highlights

- **Dual-Layer Data Strategy:** Offline-first approach. Data is written to Room first, then synced to Supabase. On app launch, data is restored from the cloud.
- **Supabase REST API:** HTTP connections are made directly via `HttpURLConnection` — no additional SDK dependency required.
- **Reactive UI:** All data changes are reflected in the UI in real-time via `StateFlow` and `Flow`.
- **Dynamic Typography:** Users can adjust the font size multiplier, which is applied across all `Typography` levels.
- **Secure API Key Management:** Sensitive keys are injected at compile time via `local.properties` → `BuildConfig`; no secrets are hardcoded in the source code.

---

## 🗄️ Database

### Local Database (Room)

The app uses a Room (SQLite) database with 8 tables:

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

### Cloud Database (Supabase / PostgreSQL)

The following tables exist on Supabase:

| Table | Description |
|-------|-------------|
| `topics` | TYT/AYT topic pool |
| `snippets` | Topic-based knowledge cards |
| `profiles` | User profile information |
| `user_topic_progress` | Topic completion progress |
| `user_study_times` | Study durations |
| `user_net_results` | Net score results |
| `user_calculations` | Rank estimation history |
| `user_snippet_interactions` | Snippet like/save/known |
| `user_followed_subjects` | Followed subjects |
| `user_question_logs` | Question solving logs |
| `yigilma_verileri` | Historical score distribution data |

> ⚠️ **Security Note:** Sensitive information such as Supabase URL, API Key, and Gemini API Key are stored in `local.properties` and excluded from version control via `.gitignore`. No secrets are shared in this repository.

---

## 🚀 Installation

### Prerequisites
- Android Studio Koala (2024.1.1) or later
- JDK 11+
- Android SDK 35
- A device or emulator supporting Min SDK 27

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Taruq157/yks-takip.git
cd yks-takip

# 2. Create the local.properties file
#    (This file is in .gitignore and is not included in the repo)
```

Add the following keys to `local.properties`:

```properties
sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk

# Supabase (required — for cloud sync and authentication)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your_supabase_anon_key

# Gemini AI (optional — for the motivation coach feature)
GEMINI_API_KEY=your_gemini_api_key
```

```bash
# 3. Build and run the project
#    Press the "Run" button in Android Studio
#    or via terminal:
./gradlew assembleDebug
```

> **Note:** The app will run without Supabase keys, but cloud sync and auth features will be disabled. Without a Gemini API Key, the AI motivation feature will show fallback messages.

---

## 📱 Screens

| Screen | Description |
|--------|-------------|
| `AuthScreen` | Supabase login / registration |
| `OnboardingScreen` | Profile setup (name, track, title, exam year) |
| `MainDashboard` | Home — progress overview, subjects, AI motivation |
| `SubjectDetailScreen` | Subject topics and completion status |
| `HapScreen` | Knowledge snippets main screen |
| `SubjectSnippetsScreen` | Subject-based snippet list |
| `ProfileScreen` | User profile and achievements |
| `PomodoroScreen` | Pomodoro timer |
| `StopwatchScreen` | Stopwatch |
| `NetTrackingScreen` | Net score tracking |
| `ScoreCalculationScreen` | YKS rank estimation |
| `WrongQuestionsScreen` | Wrong question tracker |
| `ExamTimerScreen` | Exam timer |
| `QuestionTrackingScreen` | Question solving tracker |
| `SettingsScreen` | Theme, font, notifications, data reset |
| `RewardsScreen` | Achievement / reward gallery |

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

---

## 📄 License

This project is licensed under the [MIT](LICENSE) License.

---

<div align="center">

**YKS Takip** — Get one step closer to your goal! 🎯

*Developer: [Ömer Faruk](https://github.com/Taruq157)*

</div>
