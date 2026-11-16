# Weed Guru / Cyber Tarot

Футуристический киберпанк-оракул и коллекционная карточная система для Android (Kotlin + Jetpack Compose).

**Текущий статус:** продвинутый прототип. Офлайн-генератор раскладов, локальная шифрованная БД Room, криптографические утилиты (AES-256-GCM, PBKDF2, ECDH/ECDSA, BIP-39) и UI полностью функциональны; модули NFT-минта (read-only RPC / Local Sandbox), P2P-реестра, Tor-оверлея и микроплатежей работают в режиме симуляции (Sandbox).

## Структура репозитория

- `app/` — исходный код Android-приложения (Kotlin + Jetpack Compose + Room + Retrofit/OkHttp + Firebase)
- `app/src/main/resources/` — канонический словарь `bip39_english.txt`
- `docs/` — проектная документация, комплаенс и отчёты (см. [docs/README.md](docs/README.md))
- `gradle/` — Version Catalog (`libs.versions.toml`) и настройки сборки
- `.github/workflows/android-ci.yml` — CI-проверки (сигнатуры бинарников, тесты, `lintRelease`, сборка)
- `firestore.rules`, `firestore.test.js`, `package.json` — правила безопасности Firestore и тесты к ним

## Сборка и запуск тестов

### Требования
- **JDK:** 17 или 21 (Gradle 9.3.1+)
- **Android SDK:** `compileSdk 36`, `minSdk 24`
- **Конфигурация ключей:**
  - Скопируйте `.env.example` в `.env` (или задайте переменную в панели Secrets) и при необходимости укажите `GEMINI_API_KEY=...` (без ключа приложение использует локальный детерминированный генератор).
  - Для Firebase-синхронизации используется `app/google-services.json`.

### Команды Gradle
```bash
# Сборка Debug APK и проверка Release Lint (R8 включён для release)
gradle :app:assembleDebug :app:lintRelease

# Запуск локальных unit- и Robolectric-тестов (без Firebase Emulator)
gradle :app:testDebugUnitTest \
  --tests "com.example.data.GeminiServiceTest" \
  --tests "com.example.data.crypto.CryptoHelperTest" \
  --tests "com.example.data.security.EncryptedMessageSecurityTest" \
  --tests "com.example.data.security.PinSecurityTest" \
  --tests "com.example.data.NftMintingServiceTest" \
  --tests "com.example.data.AuditSecurityRegressionsTest" \
  --tests "com.example.data.Bip39Test" \
  --tests "com.example.data.DeckIntegrityTest" \
  --tests "com.example.TarotReadingTest" \
  --tests "com.example.ExampleUnitTest" \
  --tests "com.example.ExampleRobolectricTest" \
  --tests "com.example.ui.components.WeedGuruTarotCardTest"
```

Для запуска `FirestoreRulesTest` / `npm run test:firestore` требуется локально запущенный Firebase Emulator Suite (`firebase emulators:start --only firestore,auth`).
