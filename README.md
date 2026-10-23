# 💼 Enterprise Expense Management (Android)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2035-brightgreen.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%20%2F%20UDF-orange.svg)](#architecture-overview)
[![Database](https://img.shields.io/badge/DB-MongoDB%20Atlas-forestgreen.svg?logo=mongodb)](https://www.mongodb.com/atlas)
[![Firebase](https://img.shields.io/badge/Config-Firebase%20Remote%20Config-amber.svg?logo=firebase)](https://firebase.google.com)
[![AI](https://img.shields.io/badge/AI-Google%20Gemini%20%26%20OpenAI-blueviolet.svg)](#artificial-intelligence-provisions)

Enterprise-grade, highly dynamic, modular Android Expense Management platform built with **Kotlin**, **Jetpack Compose (Material 3)**, **Clean Architecture**, **MVI (Unidirectional Data Flow)**, **MongoDB**, **Firebase Remote Config**, and dual provisions for **Google Gemini** and **OpenAI GPT**.

---

## 🏛️ Architecture Overview

The system strictly follows **Clean Architecture** principles and Google's recommended Modern Android Architecture (MAD):

```
app/src/main/java/com/enterprise/expensetracker/
├── ExpenseTrackerApp.kt                 # Application class (@HiltAndroidApp)
├── MainActivity.kt                      # Single Activity host with Edge-to-Edge Navigation
│
├── core/                                # Reusable infrastructural layers
│   ├── common/                          # Result<T>, CoroutineDispatchers, MVI BaseViewModel
│   ├── model/                           # Pure Domain Models (Expense, Category, Budget, Currency)
│   ├── database/                        # MongoDB Atlas / Realm abstractions, entities & DAOs
│   ├── remoteconfig/                    # Firebase Remote Config manager & key contracts
│   ├── ai/                              # Unified AI Engine abstraction (Gemini + OpenAI + Simulator)
│   └── designsystem/                    # Material 3 Theme tokens, typography, atomic components
│
├── domain/                              # Pure business logic (Platform independent)
│   ├── repository/                      # Domain repository interfaces
│   └── usecase/                         # Single-responsibility use cases
│
├── data/                                # Data orchestrators
│   └── repository/                      # Repository implementations (MongoDB + Firebase + AI)
│
├── feature/                             # Independent UI feature packages
│   ├── dashboard/                       # Executive KPIs, AI Forecast Banner, Recent Ledger
│   ├── expenses/                        # Expense Ledger, Search, Category filters, Add Modal
│   ├── insights/                        # Predictive Forecasting, Anomaly & Fraud Detection
│   └── settings/                        # Dynamic Remote Config viewer & AI engine switcher
│
└── di/                                  # Dagger Hilt dependency injection modules
```

---

## ⚙️ Core Enterprise Pillars

### 1. 🍃 Database: MongoDB
- **Interface Abstraction**: `MongoDbManager` controls connectivity, authentication, schema lifecycle, and sync state.
- **Data Access**: `ExpenseMongoDao` exposes reactive `Flow<List<ExpenseMongoEntity>>` for real-time local cache and remote synchronization.
- **Resilience**: Operates offline-first with automatic background sync and retry policies.

### 2. 🔥 Dynamic Configuration: Firebase Remote Config
- **Zero-Downtime Updates**: Core operational parameters are pulled dynamically from Firebase without requiring Play Store releases:
  - `ai_active_provider`: Dynamically toggles between `"GEMINI"` and `"OPENAI"`.
  - `max_single_expense_threshold`: Audit cap for compliance approval.
  - `sync_interval_minutes`: Background sync frequency.
  - `feature_ai_insights_enabled`: Global kill-switch for AI features.
- **Fail-safe Defaults**: Uses local enterprise defaults if offline or if Firebase is not yet linked.

### 3. 🤖 AI Engine: Google Gemini & OpenAI Provisions
- **Unified Contract**: `AiEngine` interface defines standard contracts for:
  - `generateMonthlyForecast()`: Multi-factor financial trajectory modeling.
  - `detectAnomalies()`: Real-time fraud and high-spend detection.
  - `suggestCategoryForExpense()`: Automated natural-language expense categorization.
- **Dynamic Factory (`AiEngineFactory`)**: Routes inference calls at runtime to either **Google Gemini 1.5 Flash/Pro** or **OpenAI GPT-4o** based on the active Firebase Remote Config flag.
- **Offline Simulator (`MockAiEngine`)**: Intelligent offline engine allowing immediate testing and demonstration before production API keys are configured.

---

## 🚀 Step-by-Step Upgrade Roadmap (Coming Stages)

This repository is designed to be upgraded incrementally:

| Stage | Focus Area | Planned Enhancements |
|---|---|---|
| **Stage 1 (Current)** | **Core Architecture & Empty Foundation** | Multi-layer Clean Architecture, Gradle Version Catalog, Hilt DI, MongoDB & Firebase abstractions, Gemini/OpenAI contracts, Jetpack Compose UI skeleton. |
| **Stage 2** | **MongoDB Atlas Cloud Binding** | Connect active Atlas App Services ID, configure Device Sync partition/flexible sync rules, enable conflict resolution. |
| **Stage 3** | **Firebase Remote Config Live Sync** | Plug in production `google-services.json`, set up A/B testing variants and real-time remote configuration updates. |
| **Stage 4** | **Google Gemini & OpenAI Live Keys** | Inject production Gemini and OpenAI API keys via encrypted Secret Gradle properties / remote config; enable multi-turn receipt OCR parsing. |
| **Stage 5** | **Biometric Security & Exporting** | Add Android BiometricPrompt authentication, PDF/CSV audit reports generation, and corporate card feed integration. |

---

## 🛠️ Getting Started & GitHub Push

### 1. Initialize Git & Push to Remote Repository
Run the following commands in the project directory:

```bash
# 1. Initialize git repository
git init

# 2. Stage all architectural files
git add .

# 3. Create initial commit
git commit -m "feat: initial enterprise Android expense tracker architecture with MongoDB, Firebase & AI provisions"

# 4. Set main branch
git branch -M main

# 5. Add your GitHub repository remote
git remote add origin https://github.com/<YOUR_ORGANIZATION_OR_USERNAME>/<YOUR_REPOSITORY_NAME>.git

# 6. Push to GitHub
git push -u origin main
```

### 2. Opening in Android Studio
1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala, Ladybug or newer).
2. Select **Open** and choose this project directory.
3. Gradle will sync automatically using the configured `gradle-wrapper.properties` (Gradle 8.10.2) and Java 17.
4. Run on an emulator or connected device with API 24+.

---

## 🔒 Security Best Practices
- Never commit active `google-services.json` or live AI API keys to public repositories.
- Use `app/google-services.json.example` as a template for team onboarding.
- All secrets are excluded in `.gitignore`.
