# Пост-фикс аудит WEED GURU / Cyber Tarot — финальный отчёт

*Дата аудита: 16.11.2026 (UTC). Проверено по живому рабочему дереву проекта после завершения этапов исправлений (Batches 1–5): 59 основных `.kt` файлов (34 952 строки), 14 тестовых `.kt` файлов (1 631 строка), 48 WebP-ресурсов в `drawable-nodpi` (13 МБ).*

---

## 1. Сводка проверенных сборок и тестов

| Проверка | Команда / Инструмент | Результат |
| --- | --- | --- |
| Debug-сборка и компиляция | `compile_applet` (`:app:compileDebugKotlin`, упаковка) | **PASSED** (`Build succeeded`) |
| Release-компиляция + Android Lint | `gradle :app:lintRelease` | **PASSED** (`0 errors`, 110 warnings; `:app:compileReleaseKotlin` OK) |
| Офлайн unit- и Robolectric-тесты (12 наборов) | `gradle :app:testDebugUnitTest --tests ...` | **37 / 37 PASSED** (`BUILD SUCCESSFUL`) |
| Проверка сигнатур бинарных файлов | Python header check (`*.png`, `*.jpg`, `*.webp`, `*.ttf`) | **PASSED** (`BAD: []`; 10 PNG, 1 JPG, 48 WEBP, 2 TTF валидны) |
| Запрещённые паттерны в `app/src/main/java` | `grep` (`fallbackToDestructiveMigration`, `collectAsState(`, `runBlocking(`, `!!`) | **PASSED** (`0` вхождений) |
| Эмуляторные тесты Firestore Rules | `FirestoreRulesTest` / `firestore.test.js` | **Требуется Firebase Emulator** (без локального эмулятора ожидаемо не подключается) |

---

## 2. Статус находок исходного аудита

### Критичные находки (`C1–C6`)

| ID | Исходная проблема | Текущий статус | Фактическая реализация в коде |
| --- | --- | --- | --- |
| **C1** | 13 битых бинарных файлов, отсутствие `.gitattributes` и CI-проверки | **Исправлено** | Все PNG/JPG/WEBP/TTF имеют корректные заголовки (`BAD: []`); 106 JPG заменены на 48 WebP в `drawable-nodpi` (13 МБ); добавлены `.gitattributes` и CI-шаг проверки заголовков в `.github/workflows/android-ci.yml`. |
| **C2** | Открытые seed, PIN, ФИО/ДР в Room, отсутствие защиты экрана | **Исправлено** | Секреты шифруются через `SecureVaultHelper` (Android Keystore AES-256-GCM с JVM-фолбэком для тестов); PIN хранится в виде PBKDF2-HMAC-SHA256 хэша (210 000 итераций) с constant-time проверкой (`CryptoHelper.constantTimeEquals`); `SecureScreenWindowEffect` (`FLAG_SECURE`) и `EXTRA_IS_SENSITIVE` с автоочисткой буфера за 45 с; `PinSecurityTest` проверяет отсутствие plaintext PIN в БД. |
| **C3** | Слабая энтропия seed (brain wallet) и «подпись» на SHA-256 | **Исправлено** | `Bip39Helper` генерирует энтропию через `SecureRandom` и реализует BIP-39 `PBKDF2WithHmacSHA512` (2048 итераций), покрыт официальными тест-векторами в `Bip39Test`; подпись в P2P-реестре заменена на реальную `SHA256withECDSA` на базе Keystore EC P-256 ключа. |
| **C4** | Открытый TCP-сервер Swarm (9945) отдавал расшифрованные шарды без аутентификации | **Исправлено** | `SwarmStorageManager` по умолчанию выключен (`nodeActive = false`); TCP-сервер отдаёт **только** сырые AES-256-GCM шифр-фреймы (`readEncryptedShardFrame`), требует 32-байтный HMAC-SHA256 challenge-response (при ошибке возвращает `-2`), имеет таймауты и семафор соединений; подписанные UDP-анонсы на порту 9944. Покрыто `AuditSecurityRegressionsTest`. |
| **C5** | `fallbackToDestructiveMigration(true)` и `exportSchema = false` | **Исправлено** | `fallbackToDestructiveMigration` удалён; в `TarotDatabase` включён `exportSchema = true`, сохранён экспорт схемы `app/schemas/.../16.json`, добавлена `MIGRATION_15_16` и регрессионный тест `c5_migration15To16_preservesUserProfileAndAddsCloudRewardClaimedColumn`. |
| **C6** | Симуляции P2P, NFT и Tor подавались как реальные операции | **Исправлено** | `NftMintingService` честно разделяет read-only RPC без подписанта (`success = false`, без фейковых URL Etherscan) и явный режим `Local Sandbox (Offline)`; P2P-транзакции и лобби маркированы как `Sandbox` и выполняются без уничтожения реальных активов; тексты в UI и документации приведены к фактам. |

### Высокие риски (`H1–H7`)

| ID | Исходная проблема | Текущий статус | Фактическая реализация / Примечание |
| --- | --- | --- | --- |
| **H1** | Ключ Gemini в APK и в URL | **Смягчено (частично открыто)** | Ключ перенесён из URL query string в HTTP-заголовок `x-goog-api-key` (`GeminiService`). Клиентский режим чтения `BuildConfig.GEMINI_API_KEY` из `.env` сохранён для обратной совместимости; для продакшена без ключа в APK рекомендуется переход на Firebase AI Logic + App Check или серверный прокси. |
| **H2** | «E2E»-шифрование сообщений работало только на одном устройстве | **Исправлено** | В `CryptoHelper` реализованы реальный обмен ключами **ECDH (P-256) + HKDF** и подпись **ECDSA**; в UI/доках убраны преувеличения; добавлен кросс-устройственный тест `ecdhAndEcdsa_crossPartyKeyAgreement_andSignatureVerification`. |
| **H3** | Клиентская экономика: бесконечный фарм + отсутствие транзакций | **Исправлено (локально)** | Балансовые операции в `TarotRepository` переведены в `database.withTransaction { ... }`; `updateCloudAllocation` сделана идемпотентной через флаг `cloudRewardClaimed` (проверено тестом `h3_updateCloudAllocation_isIdempotent_awardsBonusOnlyOnce`). *Примечание:* при запуске реальной монетизации авторитетным источником баланса должен стать сервер. |
| **H4** | PIN как экранный замок + `runBlocking` в UI | **Исправлено** | `runBlocking` убран (`suspend fun unlockApp`); счётчик попыток и блокировка персистируются зашифрованными в `SecureVaultHelper` (переживают перезапуск, проверено в `PinSecurityTest`); panic-wipe по decoy PIN убран. |
| **H5** | Release без R8, `usesCleartextTraffic="true"`, безусловный `BODY`-логгер | **Исправлено** | В `app/build.gradle.kts` для `release` включены `isMinifyEnabled = true` и `isShrinkResources = true` с keep-правилами в `proguard-rules.pro`; подключён `network_security_config.xml` (cleartext запрещён везде, кроме `localhost`/`127.0.0.1`); `HttpLoggingInterceptor` установлен в `BASIC` только для `BuildConfig.DEBUG` и `NONE` в release. |
| **H6** | Однопоточный `server.py`, раздача всего каталога, нет `Range` | **Исправлено** | `server.py` переведён на `ThreadingHTTPServer`, раздачу строго по `ALLOWED_DOWNLOADS` и поддержку HTTP `Range` (`206 Partial Content` / `416`). Для публичного продакшен-дистрибутива по-прежнему лучше использовать Google Play / Firebase App Distribution / HTTPS CDN. |
| **H7** | Мёртвый Firebase-код и дырявые `firestore.rules` | **Исправлено** | Правила `firestore.rules` запрещают прямое изменение `weed` клиентом (`request.resource.data.weed == resource.data.weed`), валидируют схемы и защищают покупки в `market`; `FirebaseAuthManager` и `FirestoreSyncManager` доработаны (устранены `StaticFieldLeak` и `CredentialManagerMisuse`, добавлен `NoCredentialException` и передача `Activity` контекста) и подключены в `TarotViewModel` и карточку Cloud Identity & Sync в `SecurityScreen`. |

### Средние и низкие замечания (`M1–M9`, Low)

| ID | Что сделано | Примечание по остатку |
| --- | --- | --- |
| **M1** | `CyberTarotScreens.kt` разбит с **14 572** до **2 757** строк; выделены 11 профильных экранных файлов (всего **15 028** строк по 12 файлам); `AvatarGenerator` вынесен из `GeminiService`. | При желании в будущем можно раздробить `TarotViewModel.kt` (~2 150 строк) и `TarotRepository.kt` (~1 900 строк) по доменам и внедрить DI (Hilt/Koin). |
| **M2** | Добавлен `locales_config.xml` для Android 13+; виджет (`TarotAppWidgetProvider`, `widget_cyber_tarot.xml`) локализован по настройке `system_language` для всех 8 языков и обновляется сразу при смене языка; строковые ресурсы для виджета вынесены в `strings.xml` / `values-ru/strings.xml`. | Строки интерфейса внутри Compose-экранов по-прежнему частично обслуживаются `LocalizationHelper` / Room — можно постепенно мигрировать на `stringResource(...)`. |
| **M3** | Графика переведена в 48 WebP в `drawable-nodpi` (суммарно 13 МБ); удалена дублирующая копия словаря из `assets/bip39_english.txt`. | `ic_cybertarot_fg.jpg` (69 КБ) лежит в `drawable/` как foreground адаптивной иконки (Lint `IconLocation` — без ошибок). |
| **M4** | Зависимости согласованы с AGP 9.1.1 / Kotlin 2.2.10; оставлены актуальные стабильные версии без ломающих миграций. | Обновление до Compose BOM 2026.x / Retrofit 3.x можно вынести в отдельный технический PR при наличии регрессионного UI-стенда. |
| **M5** | `GeminiService` рефакторен: единый вызов `executeGeminiCall`, проброс `CancellationException`, ретраи 429/5xx, запрос пользователя вынесен из `systemInstruction`, fail-closed при активном прокси. | См. также примечание к H1 по ключу клиента. |
| **M6** | Добавлены `AuditSecurityRegressionsTest`, тесты `GeminiServiceTest`, векторы `Bip39Test`, честные тесты `NftMintingServiceTest`, кросс-устройственный крипто-тест, `failFast = false`, `package.json` для `firestore.test.js` и CI workflow `.github/workflows/android-ci.yml`. | `FirestoreRulesTest` / `npm run test:firestore` требуют запущенного локального Firebase Emulator Suite. |
| **M7** | Убраны все `!!` в `app/src/main/java`; `observeCloudReadings` корректно закрывает Flow при ошибке; `SwarmStorageManager` получил DCL-синглтон, `.use {}` на файлы, маскировку/ограничение сетевых логов и метод `shutdown()`, вызванный в `TarotViewModel.onCleared()` и проверенный тестом. | Реальное резервирование блоков ОС (`StorageManager.allocateBytes`) помечено Lint как рекомендация (в UI/доках указано честно без обещаний физического резервирования). |
| **M8** | `docs/README.md`, `OPENCODE_DEVELOPER_GUIDE.md`, `PROJECT_INVESTMENT_REPORT_RU.md` и корневой `README.md` приведены к фактическому состоянию прототипа (`Sandbox`, PBKDF2 210k, реальная структура папок и команд). | — |
| **M9** | Создан `docs/compliance/PRIVACY_POLICY_AND_DATA_SAFETY.md` (описание локальных PII, Photo Picker, передачи запросов в Gemini/Firebase/RPC, возрастное ограничение 18+/21+, нефинансовый дисклеймер). | Юридическая проверка перед публикацией в Google Play остаётся на стороне владельца продукта. |
| **Low** | Исправлены 9 deprecated иконок (`Icons.AutoMirrored.Filled.*`), удалена избыточная ветка `AudioTrack < M` в `CyberNoirSynthPlayer`, убран `RedundantLabel` в манифесте, очищены неиспользуемые шаблон-цвета в `colors.xml`, исправлен `RemoteViewLayout` Error в `widget_cyber_tarot.xml` (`<View>` → `<ImageView>`). | Release Lint проходит с **0 errors** (110 некритичных warning: версии библиотек, `DefaultLocale`, `UseKtx`). |

---

## 3. Проверка чек-листа приёмки (Раздел 8)

- [x] Все `.png`, `.jpg`, `.webp`, `.ttf` проходят проверку бинарных заголовков (и добавлены в CI).
- [x] `grep -rn fallbackToDestructiveMigration app/src/main/` возвращает пустой результат; экспорт схемы `16.json` зафиксирован в `app/schemas/`.
- [x] В БД Room не сохраняются открытые `seedPhrase` и `pinCode` (шифрование Keystore + PBKDF2, проверено тестами).
- [x] Неаутентифицированное TCP-подключение к Swarm на порту 9945 отклоняется с `-2`, plaintext не отдаётся (`AuditSecurityRegressionsTest`).
- [x] Повторная облачная аллокация не начисляет повторный бонус `+100 WEED` (`AuditSecurityRegressionsTest`).
- [x] NFT-минт и P2P-перевод без реального подписанта/сети честно возвращают отказ RPC или статус `Sandbox` (`NftMintingServiceTest`).
- [x] Локальные тесты (37 / 37) и `gradle :app:lintRelease` (**0 ошибок**) проходят успешно.
- [ ] **Требует внешнего окружения пользователя:** загрузка собственного release keystore (`KEYSTORE_PATH`) для подписи релизного AAB (`assembleRelease`/`bundleRelease`), запуск локального Firebase Emulator для `FirestoreRulesTest`, и опциональный перенос `GEMINI_API_KEY` на Firebase AI Logic / серверный прокси перед публичным релизом.
