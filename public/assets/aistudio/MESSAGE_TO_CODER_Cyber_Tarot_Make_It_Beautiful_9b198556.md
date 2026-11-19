# 🌿 Послание кодеру: Делаем Красиво, Друзья!

> **Проект:** Cyber Tarot / Weed Guru (Android, Kotlin + Jetpack Compose)
> **Дата аудита:** 16.11.2026 · **Автор:** Claude
> **Формат:** аудит кода и UI + рецепты, как довести всё до «вау»

Привет, кодер! Я прошёл по твоему архиву: тема, `MainActivity`, дашборд, экран Оракула, компоненты, билд-конфиг, правила Firestore, шрифты, ресурсы. Сначала хорошая новость: **у проекта уже есть душа**. Золотое арт-нуво, изумрудные «печатные платы» и звёздное небо Ван Гога на одной карте — это узнаваемый стиль, которого нет у других.

Плохая новость короткая: код вокруг этого арта пока не знает, что он красивый. Давай это исправим, и это будет весело. 🛠️✨

---

## TL;DR — пять рычагов красоты

| # | Рычаг | Почему важно | Время |
|---|-------|--------------|-------|
| 1 | **Шрифты без кириллицы** | Orbitron и Rajdhani содержат **0 из 64** кириллических букв. Русский UI сейчас рисуется системным шрифтом, и весь киберпанк-характер пропадает | 0,5–1 день |
| 2 | **Ожившая тема** | Тема Material в коде мертва: **0** обращений к `MaterialTheme.colorScheme`, зато **811** цветов `Color(0x…)` (259 уникальных) и **810** литералов `fontSize` | 2–3 дня |
| 3 | **Тише вокруг арта** | Яркие обои + 5 слоёв вуалей + глобальное свечение текста = каша. Арт должен играть, UI — аккомпанировать | 1–2 дня |
| 4 | **Одна звезда — карта** | Карта — самая красивая вещь в приложении. Всё остальное тише, а моменту раскрытия карты отдаём всю драматургию | 2–4 дня |
| 5 | **Один бренд, один язык, одна система** | Сейчас 4 названия, 3 системы строк и 10 разных радиусов скругления | 3–5 дней |

---

## 0. Как проводился аудит (честно)

- **Что сделал:** распаковал архив, прочитал тему, типографику, `MainActivity`, дашборд, `DivinationScreen`, общие компоненты, `build.gradle.kts`, манифест, `network_security_config`, `firestore.rules`, прошлый `AUDIT_FINAL_REPORT_RU.md`. Посчитал паттерны через `grep`, проверил покрытие глифов шрифтов через `fontTools`, измерил контраст, посмотрел арт (рубашка карты, три карты, три фона).
- **Чего не делал:** не собирал проект, не запускал на устройстве или эмуляторе и не гонял тесты. Все визуальные выводы получены из кода и ассетов, **проверь их на реальном телефоне**. Результаты тестов беру из твоего отчёта.
- **Метрики** лежат в приложении A. Скрипты — в приложении B, повтори их у себя.

---

## 1. Что уже прекрасно (не ломать!)

- **Арт-дирекшн.** Золотая филигрань + изумрудная неоновая геометрия + вихри Ван Гога. Рубашка карты с листом в Меркабе — настоящий бренд-символ.
- **Редкость через шейдеры** (`CyberRarityShaderCard.kt`): AGSL для API 33+ и фолбэк на `DrawScope` для старых. Правильная инженерия.
- **Скины** с `primaryColor`, `secondaryColor`, `ambientGlowColor`. Идея верная, просто её надо довести до конца (п. 3.5).
- **Звук и атмосфера:** синтезатор, радио, лунный виджет, haptic-ready архитектура.
- **Честная маркировка симуляций** («Sandbox»). Это редкая и ценная зрелость.
- **Безопасность** выше среднего (см. раздел 6): AES-GCM, PBKDF2-PIN, `FLAG_SECURE`, строгие правила Firestore, `cleartextTrafficPermitted=false`.

---

## 2. Находки по приоритету

### P0 — блокируют красоту

**F1. Шрифты не умеют кириллицу**
`orbitron.ttf` и `rajdhani.ttf`: 0/64 кириллических глифов (проверено `fontTools`). Они используются в 652 местах. Русский текст идёт через системный fallback, поэтому в одной строке смешиваются два шрифта. Дополнительно оба файла статичные и однородные по весу: в `Type.kt` Orbitron зарегистрирован только как `Bold`, Rajdhani только как `SemiBold`. Иерархии начертаний нет, остаются только размеры.

**F2. Тема есть, но её никто не слушает**
- 0 использований `MaterialTheme.colorScheme` и `MaterialTheme.typography` в UI-коде.
- 811 литералов `Color(0xAARRGGBB)`, 259 уникальных. Один только серый `#B0B0C5` встречается 189 раз.
- Дубли «почти одного» цвета: зелёный (`00FF66` ×67, `00FF88`, `39FF14`, `00FFA3`), циан (`00F5FF` ×57, `00F0FF`, `00FFFF`), янтарь (`FFB800`, `FF9900`, `FFD700`).
- `Theme.kt`: `primary = NeonMagenta`, а `Color.kt` говорит `PrimaryCyber = NeonGreen`. Параметры `darkTheme` и `dynamicColor` не используются.
- `Typography` зашивает `color = White` в каждый стиль, и это ломает наследование цвета контента.

**F3. Читаемость над обоями**
Стек слоёв на фоне: обои (α 0,88) → цветная вуаль → Canvas-вихри → радиальная тёмная вуаль → `circuitBackground` → **глобальный** `Shadow(blur 15)` через `LocalTextStyle` на каждом тексте. Сами обои очень яркие и детальные (неоновые города, у скина Jamaica ещё и дневное небо). Мелкий текст на них читается как повезёт.
Контраст на чистом `#070B12`: основной серый `#B0B0C5` 9,3:1 (ок), `Color.Gray` 5,6:1 (ок), `Color.DarkGray` 2,0:1 (нормально только для disabled), фиолетовый `#A020F0` 3,7:1 (мелкий текст не годится). Но реальный контраст определяется обоями за текстом, а не чистым фоном.

**F4. Бренд-хаос**
`app_name = «Шмаль»` (ресурсы), заголовок в UI `CYBERTAROT`, в README и metadata `WEED GURU`, в `LocalizationHelper` `app_title = SHMALL`. Комментарий в `CyberTarotDashboard` обещает «ШМАЛЬ для ru/uk», а код всегда пишет `CYBERTAROT`. Это продуктовое решение, но оно напрямую влияет на «красиво»: логотип, иконка, сплэш и стор должны говорить одно имя. Отдельно: «шмаль» в русском сленге означает наркотик. Проверь это название и каннабис-тематику по политикам Google Play до публикации.

**F5. Скины почти не видны**
Обои выбираются **по вкладке** (захардкожено), а `skin.backgroundResId` реально показывается только на 5-й вкладке. Получается, что скин меняет только цвет акцентов, а не атмосферу. Плюс вкладка CRYPTO показывает лунную обсерваторию, а подзаголовок там называется «Lunar Calendar & Hub». Название вкладки и содержимое не совпадают.

### P1 — заметно портят впечатление

**F6. Доступность**
- 48 иконок и картинок с `contentDescription = null`. Часть из них должна быть `null` (декор), часть нет.
- Декоративные обои имеют описание «Cyber Fantasy … background», и TalkBack зачитывает его на каждом экране.
- 91 место с текстом ≤10sp.
- 98 `.clickable` и всего 1 упоминание `semantics/Role`.
- `CyberNeonButton` при 12sp и вертикальном отступе 8dp получается высотой ≈32dp, что меньше 48dp для касания.
- В `NavigationBar` `indicatorColor = Transparent`, поэтому выбранная вкладка отличается **только цветом**.

**F7. Движение без дисциплины**
14 бесконечных анимаций, 0 проверок «уменьшить анимации», 0 haptic-откликов, переключение вкладок без перехода (`when(selectedTab)`), 0 `animateContentSize`. «Живое» есть, но оно не выбирает, на что смотреть. Плюс искусственный сплэш: `delay(1200) + delay(1000) + delay(400)` = 2,6 секунды на каждом запуске.

**F8. Строки в трёх системах сразу**
0 вызовов `stringResource`. Есть `LocalizationHelper` (17 вызовов в UI), 227 инлайн-тернариков вида `if (isRuOrUk) … else …` и 506 строк с кириллическими литералами прямо в Compose. Заявлено 8 локалей (`locales_config.xml`), а в ресурсах только дефолт и частично `values-ru`. Каждая новая строка сейчас стоит три правки.

**F9. Карта: арт и «хром» перемешаны**
В PNG/WebP уже запечены золотая рамка, римская цифра и **английское** название («I – THE BUDTENDER», «XV – SPIDER MITE ATTACK»). При этом в коде есть `TarotArtNouveauFrameOverlay`, `FramedTarotArtworkWindow`, `getArcanaRomanNumeral` и водяные знаки. Проверь на устройстве, нет ли двойной рамки и двойного названия. Плюс:
- Края карт не едины: у карты XV кремовая кайма, у остальных тёмная.
- Рубашки 687×1024 (0,671), лица 896×1200 (0,747). При 3D-флипе размеры не совпадут.
- Русскоязычный игрок видит английские названия поверх арта.

**F10. «Хром» окна**
`MainActivity` постоянно прячет системные панели, а `DivinationScreen` компенсирует это `padding(top = 54.dp)` под оверлейный топ-бар. Это ломается при увеличенном шрифте и вырезе камеры. Плюс `selectedTab` лежит в `remember`, а не в `rememberSaveable` (0 использований во всём проекте), поэтому вкладка сбрасывается при повороте и смерти процесса. `BackHandler` встречается всего 2 раза.

**F11. Лишние «голоса»**
108 использований `FontFamily.Monospace` (третий шрифт). Эмодзи прямо в строках UI (🌱 🔒 ➡️ ✔️ ❌ 💾): они рисуются по-разному на каждом OEM и бьются с арт-дирекшном.

### P2 — полировка и гигиена

| ID | Находка | Что делать |
|----|---------|-----------|
| F12 | Монолиты: `CyberTarotScreens.kt` 2757 строк, `NetOpsBlockchainPanels.kt` 2322, `SecurityScreen.kt` 1954, `TarotViewModel.kt` 2149, `TarotRepository.kt` 1918 | Разрезать по экранам и фичам. Большие `@Composable` дают большие области рекомпозиции |
| F13 | Память: 48 WebP, ≈13 МБ на диске. В памяти лицо карты 896×1200 — это **≈4,3 МБ** распакованным; 31 карта в сетке ≈ 130 МБ | Миниатюры для сетки, полный размер только во вьюере (п. 4.6) |
| F14 | `AgslShaderCache.getShader`: `return null` внутри `getOrPut` — при ошибке компиляции результат не кэшируется и компиляция повторяется на каждый вызов. Один `RuntimeShader` на tier разделяется между картами | Кэшировать неудачу; проверить uniform'ы в сетке (п. 4.6) |
| F15 | Compose BOM `2024.09.00` при AGP 9.1.1, Kotlin 2.2.10, `compileSdk 36`. 0 `@Preview`, Roborazzi подключён, но плагин закомментирован | Обновить BOM до актуального стабильного, включить скриншот-тесты |
| F16 | `data_extraction_rules.xml` — шаблон с TODO | Явно исключить БД и prefs с зашифрованными ключами |
| F17 | Ключ Gemini по-прежнему в `BuildConfig` (в отчёте это «H1, частично открыто») | Зависимость `firebase-ai` уже подключена. Перейти на Firebase AI Logic + App Check и включить enforcement в консоли |

---

## 3. Дизайн-направление: «Золото × Изумруд × Звёздная ночь»

### 3.1. Палитра берём из арта, а не из головы

Я снял доминантные цвета с рубашки карты и карты XXVI. Арт живёт **не в неоне**:

| Роль | Hex | Откуда |
|------|-----|--------|
| Чернильная ночь (фон) | `#070B12` | у тебя уже есть, совпадает с тёмным фоном арта (`#070A10`) |
| Панель | `#0E1424` / приподнятая `#151D33` | производная от фона |
| Золото-лист (титулы, акценты) | `#D5C68A` | светлая золотая филигрань рамок |
| Антик-золото (рамки, разделители) | `#AB935F` | средний тон золота |
| Бронза (тени золота) | `#6B542A` | глубокий тон рамок |
| Изумруд (жизнь, «активно») | `#459C68` → свечение `#46D68C` | зелёные витки и схемы |
| Сигнал (опасность/ошибка) | `#FF5A6E` | единственный «тревожный» цвет |

**Правило ролей:**
- **Золото = структура** (рамки, заголовки, разделители, иконки состояния «покой»).
- **Изумруд = жизнь** (выбранная вкладка, активная кнопка, прогресс).
- **Неон (`#00F5FF`, `#FF007F`, `#00FF66`) = событие** (редкость Epic+, свечение раскрытия карты, живые данные). Если неон везде, он нигде.

Приём 60/30/10: 60% чернильный фон и панели, 30% золото и нейтральный текст, 10% акцент скина и неон.

### 3.2. Один момент, который запомнят

Потрать смелость в одном месте: **раскрытие карты**. Всё остальное делай тихим и дисциплинированным.

```
┌──────────────────────────────────┐
│ CYBERTAROT           ⚡ 120 WEED │  ← топ-бар: тихий, без свечения
│                                  │
│          ╭────────────╮          │
│          │   рубашка  │          │  ← ГЕРОЙ: карта «дышит» (scale 1.00↔1.015, ~6 c),
│          │   карты    │          │     тап → 3D-флип → золотое кольцо + haptic
│          ╰────────────╯          │
│                                  │
│  Спроси оракула…________________ │  ← поле: панель, золотой hairline
│  [ 1 ]  [ 3 ]  [ 5 🔒 ]  [10 🔒 ] │  ← сегменты с иконкой замка, не только цвет
│                                  │
│  История ▾                       │  ← свёрнута по умолчанию
├──────────────────────────────────┤
│  Оракул  Карты  Арена  Крипто  ⚙ │  ← выбранная: индикатор-«срез» + изумруд
└──────────────────────────────────┘
```

### 3.3. Что НЕ делать

- Не красить неоном всё подряд: неон работает только как «событие».
- Не накладывать свечение (`Shadow`) на весь текст глобально.
- Не делать все панели одинаковыми скруглёнными карточками с одинаковой тенью.
- Не ставить ВСЕ ЗАГЛАВНЫМИ длинные подписи: кириллический капс с трекингом читается тяжело. Капс только для коротких тегов (редкость, статус).
- Не вешать эмодзи на кнопки.

---

## 4. Рецепты

### 4.1. Шрифты (делай первыми)

**Роли (не больше двух семейств + моно для чисел):**

| Роль | Что взять (проверь покрытие скриптом B.1) |
|------|------------------------------------------|
| UI/заголовки | кириллический «техно»-гротеск: Exo 2, Russo One, Tektur или Unbounded |
| Названия карт / «гравировка» | антиква с кириллицей: Cormorant, Philosopher или Forum |
| Текст | Onest или IBM Plex Sans |
| Числа и данные (балансы, хэши) | IBM Plex Mono или JetBrains Mono |

Все перечисленные семейства открытые (OFL), их можно класть в `res/font`. **Я не мог скачать их здесь, поэтому прогони скрипт B.1 на выбранных файлах и убедись, что кириллица 64/64.**

**Orbitron не выбрасывай.** Оставь его для логотипа `CYBERTAROT`, римских цифр арканов и больших латинских чисел (там он прекрасен). Только не на тексте интерфейса.

**Веса:** кладите статические файлы Regular/Medium/SemiBold/Bold. Вариативные шрифты в Compose работают через `FontVariation` только с API 26+, а у вас `minSdk = 24`.

**Шкала (минимум 12sp, 91 место ≤10sp нужно поднять):**

| Стиль | Размер / межстрочный | Применение |
|-------|---------------------|-----------|
| Display | 28–32 / 36 | экран раскрытия, имя карты |
| Title | 18–20 / 26 | заголовки секций |
| Body | 15–16 / 22 | основной текст |
| Label | 12–13 / 16 | подписи, теги (трекинг 0,4–0,8sp только здесь) |

Для тела текста сделайте трекинг 0–0,2sp. Сейчас везде стоит 0,5sp, из-за этого длинные русские абзацы выглядят «рассыпанными».

```kotlin
// ui/design/Type.kt — стили БЕЗ color, цвет приходит из контекста
val CyberTypography = Typography(
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
    titleMedium    = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = 0.1.sp),
    bodyMedium     = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp),
)
```

### 4.2. Токены и тема, которую слушают

Один слой токенов: цвет, пространство, форма, движение. Скин подмешивает три цвета, всё остальное общее.

```kotlin
// ui/design/CyberTheme.kt
@Immutable
data class CyberColors(
    val ink: Color = Color(0xFF070B12),
    val panel: Color = Color(0xFF0E1424),
    val panelRaised: Color = Color(0xFF151D33),
    val goldLeaf: Color = Color(0xFFD5C68A),
    val goldAntique: Color = Color(0xFFAB935F),
    val textHigh: Color = Color(0xFFF2F2F8),
    val textMid: Color = Color(0xFFB0B0C5),
    val danger: Color = Color(0xFFFF5A6E),
    // из скина:
    val accent: Color,
    val accentAlt: Color,
    val glow: Color,
)

val LocalCyberColors = staticCompositionLocalOf<CyberColors> { error("Wrap in CyberTheme") }
object Cyber { val colors: CyberColors @Composable get() = LocalCyberColors.current }

@Composable
fun CyberTheme(skin: CyberSkin, content: @Composable () -> Unit) {
    val c = remember(skin) {
        CyberColors(accent = skin.primaryColor, accentAlt = skin.secondaryColor, glow = skin.ambientGlowColor)
    }
    val scheme = darkColorScheme(
        primary = c.accent, secondary = c.accentAlt, tertiary = c.goldLeaf,
        background = c.ink, surface = c.panel, surfaceVariant = c.panelRaised,
        onSurface = c.textHigh, onSurfaceVariant = c.textMid, error = c.danger,
    )
    CompositionLocalProvider(LocalCyberColors provides c) {
        MaterialTheme(colorScheme = scheme, typography = CyberTypography, content = content)
    }
}
```

Подключи в `MainActivity` как `CyberTheme(skin = currentSkin) { … }` вместо `MyApplicationTheme`. Тогда стандартные Material-компоненты (`NavigationBar`, `TextField`, диалоги) перекрасятся сами.

**Пространство:** оставь шкалу 4/8 (`2, 4, 8, 12, 16, 24, 32`). Сейчас в коде 5.dp (68×), 10.dp (121×), 14.dp (64×), 3.dp (29×) — это шум между соседними значениями.

**Формы:** вместо 10 радиусов сделай 3 уровня плюс «подпись» бренда.

```kotlin
object CyberShapes {
    val chip    = RoundedCornerShape(6.dp)
    val control = RoundedCornerShape(12.dp)
    val sheet   = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    // фирменная «срезанная» панель: перекликается с золотыми уголками арта
    val panel   = CutCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 0.dp, bottomStart = 14.dp)
}
```

### 4.3. Компоненты вместо копипасты

Сейчас в `CyberComponents.kt` всего три компонента (`CyberNeonButton`, `CyberBadge`, `CyberSectionHeader`) на ≈35 000 строк кода, а остальное каждый экран рисует сам. Набор, который закрывает 80% экранов:
`CyberButton` (primary/secondary/ghost), `CyberPanel`, `CyberChip`, `CyberTopBar`, `CyberSheet`, `CyberTextField`, `CyberCardThumb`, `RarityTag`.

```kotlin
@Composable
fun CyberButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, secondary: Boolean = false,
) {
    val c = Cyber.colors
    val tint = if (secondary) c.goldAntique else c.accent
    Surface(
        onClick = onClick, enabled = enabled, shape = CyberShapes.control,
        color = tint.copy(alpha = if (enabled) 0.14f else 0.05f),
        border = BorderStroke(1.dp, tint.copy(alpha = if (enabled) 0.9f else 0.3f)),
        modifier = modifier.heightIn(min = 48.dp),           // цель касания
    ) {
        Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelMedium,
                color = if (enabled) tint else c.textMid.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun CyberPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = Cyber.colors
    Column(
        modifier.clip(CyberShapes.panel)
            .background(c.panel.copy(alpha = 0.92f))
            .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CyberShapes.panel)
            .padding(16.dp),
        content = content,
    )
}
```

### 4.4. Фон: тише, глубже, читаемо

1. **Обои = скин, а не вкладка.** Одна картинка на весь экран из `skin.backgroundResId`. Вкладки отличаются лёгким акцентом, а не сменой мира.
2. **Обои затемнены и размыты как «воздух».** Лучше всего положить в ресурсы заранее затемнённые и размытые варианты (меньше нагрузка). Запасной вариант в рантайме:

```kotlin
@Composable
fun SkinBackdrop(skin: CyberSkin) {
    Box(Modifier.fillMaxSize().background(Cyber.colors.ink)) {
        Image(
            painter = painterResource(skin.backgroundResId),
            contentDescription = null,                       // декор: TalkBack молчит
            contentScale = ContentScale.Crop,
            alpha = 0.55f,
            modifier = Modifier.fillMaxSize().then(
                if (Build.VERSION.SDK_INT >= 31) Modifier.blur(18.dp) else Modifier
            ),
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xCC070B12), Color(0xEE070B12)))
        ))
    }
}
```

3. **Убери глобальное свечение текста** (`LocalTextStyle … Shadow(blur = 15f)`). Если нужно свечение, дай его конкретному заголовку над артом.
4. **«Матовое стекло» в Compose без бэкдроп-блюра не получить:** `Modifier.blur` размывает сам элемент, а не то, что за ним. Комментарий `CyberCharcoal // Frosted luxury glass` обещает больше, чем есть. Решение: плотная панель (≈90% непрозрачности) + золотой hairline. Если очень хочется настоящее стекло, посмотри библиотеку Haze (проверь актуальную версию и поддержку API).
5. **Скин Jamaica** (дневное небо) тестируй отдельно: ему нужна более плотная вуаль, иначе белый текст сольётся с небом.
6. Исправь соответствие «вкладка ↔ содержание» (CRYPTO ≠ лунный календарь), либо переименуй вкладку.

### 4.5. Окно и навигация

- Immersive-режим (скрытые панели) оставь **только** для Арены и полноэкранного просмотра карты. На остальных экранах покажи прозрачные системные панели.
- Убери `padding(top = 54.dp)`: пусть топ-бар будет `topBar`-слотом `Scaffold` и отдаёт `innerPadding`. Учитывай `WindowInsets.displayCutout`.
- Вкладку сохраняй: `var selectedTab by rememberSaveable { mutableIntStateOf(0) }`.
- Добавь переход между вкладками:

```kotlin
AnimatedContent(
    targetState = selectedTab,
    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
    label = "tab",
) { tab ->
    when (tab) { 0 -> DivinationScreen(viewModel) /* … */ }
}
```

- Выбранная вкладка: индикатор-«срез» или золотая линия **плюс** цвет (не только цвет). Это же и доступность.
- Сплэш: делай реальную работу (БД, профиль) и показывай анимацию минимум 600 мс; на тёплом старте пропускай. Сейчас это 2,6 секунды фальшивого ожидания.

### 4.6. Карты (главная звезда)

**Разделить арт и «хром».** Пусть в ассете будет только иллюстрация (без рамки, без цифры, без названия). Рамка, римская цифра и название рисуются в Compose:
- названия локализуются (русскому игроку не нужен английский текст);
- рамка кодирует редкость (золото → серебро → голограмма), то есть несёт информацию, а не только украшает;
- края у всех карт одинаковые (сейчас у XV кремовая кайма);
- текст остаётся чётким на любом размере.

**Один аспект.** Заведи `const val CARD_ASPECT` и подгони лица и рубашки к нему (сейчас 0,747 и 0,671).

**Миниатюры для сетки.** Полный размер только во вьюере:

```python
# scripts/make_thumbs.py
from PIL import Image
import glob
for f in glob.glob("img_card_*.webp"):
    im = Image.open(f)
    im.thumbnail((360, 480))
    im.save(f.replace("img_card_", "thumb_card_"), "WEBP", quality=82)
```
Это ≈0,7 МБ на карту в памяти вместо ≈4,3 МБ. Coil (`coil-compose 2.7.0`) у тебя уже подключён: ему можно задать `size(360, 480)` и грузить ресурсы вне главного потока.

**Имена.** `img_card_01_budtender_v2_1794566668115.webp` → `card_01_budtender.webp`. Epoch-суффиксы в именах ресурсов только мешают.

**Раскрытие карты (сигнатурный момент):**
1. Рубашка «дышит»: scale 1,00↔1,015, ≈6 секунд, едва заметно.
2. Тап → 3D-флип (`graphicsLayer { rotationY … ; cameraDistance … }`), на 90° подменяешь сторону.
3. Для Epic+ золотое кольцо или всплеск неона; для Common тишина.
4. `HapticFeedbackType` разной силы по tier'у.
5. Звук у тебя уже есть (`CyberNoirSynthPlayer`), синхронизируй.

**Шейдер-кэш:**

```kotlin
private val failed = mutableSetOf<CyberRarityTier>()
fun getShader(tier: CyberRarityTier): RuntimeShader? {
    shaders[tier]?.let { return it }
    if (tier in failed) return null
    return try {
        RuntimeShader(sourceFor(tier)).also { shaders[tier] = it }
    } catch (t: Throwable) { failed += tier; null }
}
```
Если в сетке на экране несколько карт одного tier, **проверь**, что uniform'ы (время, размер) не затирают друг друга. И включай `isAnimated` только для карты в фокусе: остальные статичны.

### 4.7. Движение и haptic

**Принцип:** один оркестрованный момент лучше десяти фоновых. Из 14 бесконечных анимаций оставь те, что отвечают на действие или несут информацию (раскрытие карты, радио-визуализатор, прогресс). Остальные убери или сделай статичными.

**Уважай «уменьшить анимации»:**

```kotlin
@Composable
fun rememberMotionEnabled(): Boolean {
    val ctx = LocalContext.current
    return remember {
        Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}
// использование: if (rememberMotionEnabled()) rememberInfiniteTransition() … else статичное значение
```

**Haptic** (сейчас 0): флип, выбор расклада, покупка, разблокировка PIN.

```kotlin
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
```

### 4.8. Иконки вместо эмодзи

Эмодзи в строках (`"COPY 📋"`, `"NEXT ➡️"`, `"VERIFY ✔️"`) выглядят по-разному на Samsung, Xiaomi и Pixel. Нужен единый набор: Material Symbols в одном весе или свои векторные глифы (лист, Меркаба, луна, ключ). Иконка идёт отдельным `Icon`, а не частью текста. Тогда ещё и перевод перестаёт тащить эмодзи за собой.

### 4.9. Тексты и локализация

- Одна система: `res/values/strings.xml` (+ `values-ru`, `values-es`, …) и `stringResource`. Язык приложения через per-app locales.
- Список кириллических литералов для миграции: команда B.3. Идти экран за экраном, начиная с дашборда и Оракула.
- Копирайт интерфейса: глагол в кнопке («Сделать расклад», а не «ОК»), одно название для одного действия по всему потоку, ошибки без извинений и с понятным «что делать». Описывай, что делает кнопка, а не «продавай» её.
- Убери из UI слова-«шум»: `//`, `(ORACLE & COSMIC MYSTIC)`, повторные «Священные Колоды / Боевая Арена / Хаб». Один экран = одно честное имя.

### 4.10. Доступность (пол качества)

- [ ] Минимальный текст 12sp; всё, что ≤10sp, поднять (91 место).
- [ ] Цель касания ≥48dp у всех кликабельных (36 элементов размером 20–35dp проверить).
- [ ] Декоративные изображения `contentDescription = null`, содержательные получают осмысленное описание (из 48 `null` разобрать вручную).
- [ ] Кастомные `clickable` — через `Role.Button` / `Role.Tab` (или Material-компоненты).
- [ ] Цвет никогда не единственный носитель смысла: редкость = цвет **+** форма/иконка рамки; выбранная вкладка = цвет **+** индикатор.
- [ ] Контраст текста ≥4,5:1, крупного ≥3:1, границ интерактивных элементов ≥3:1.
- [ ] Прогнать на `fontScale` 1,3 и 2,0 и на самых длинных русских строках.
- [ ] Уважать reduced motion (п. 4.7).

---

## 5. Архитектура под красоту

1. **Пакет `ui/design/`:** `Tokens.kt` (Space, Shapes, Motion), `CyberTheme.kt`, `Type.kt`, `components/`. Экранам запрещено хардкодить цвета и размеры шрифтов.
2. **Резать монолиты** (F12): `CyberTarotScreens.kt` → `DashboardScreen.kt`, `DivinationScreen.kt`, `SplashScreen.kt`, `Backdrop.kt`. То же для `SecurityScreen`, `NetOpsBlockchainPanels`.
3. **`@Preview`** для каждого компонента × каждый скин × fontScale 1,0/1,5 (сейчас 0 превью).
4. **Скриншот-тесты:** подключи Roborazzi (зависимости уже есть, плагин закомментирован) и снимай ключевые экраны на каждом из скинов. Регрессия красоты ловится автоматически.
5. **«Храповик» в CI**, чтобы хардкод только убывал (скрипт B.2).
6. Обнови Compose BOM и проверь поведение на API 24, 33, 36.

---

## 6. Безопасность и сборка (коротко, пока мы тут)

**Подтвердил по коду:** `allowBackup=false`; `cleartextTrafficPermitted=false` (кроме localhost); правила Firestore со строгой схемой (`hasOnly`), запретом клиентской накрутки `weed` и атомарной покупкой; R8 включён на release; подпись release через переменные окружения. Закрытие критичных пунктов C1–C6 я опираю на твой отчёт, сами тесты не гонял.

**Осталось:**
- F17: ключ Gemini в `BuildConfig` → Firebase AI Logic + App Check, enforcement включить в консоли.
- F16: заполнить `data_extraction_rules.xml`.
- `google-services.json` в репозитории — это норма для Firebase, но ключ API ограничь по пакету и SHA-1 в консоли.
- В `FirestoreRulesTest` нужен эмулятор; включи его в CI отдельной джобой.
- Проверь политику Google Play по каннабис-контенту, NFT/крипто и названию приложения (F4).

---

## 7. План по шагам

**Спринт 0 — «быстрые победы», 1–2 часа**
- [ ] Убрать глобальный `Shadow` из `LocalTextStyle`.
- [ ] Обои: `contentDescription = null`.
- [ ] `selectedTab` → `rememberSaveable`, добавить `AnimatedContent`.
- [ ] Починить соответствие вкладка ↔ фон ↔ подзаголовок.
- [ ] Исправить кэш шейдеров (4.6).
- [ ] Ворота `rememberMotionEnabled()` на бесконечных анимациях.

**Спринт 1 — шрифты и токены, 2–3 дня**
- [ ] Выбрать шрифты, прогнать B.1, положить в `res/font`.
- [ ] `CyberTheme`, `CyberTypography`, `Space`, `CyberShapes`.
- [ ] Подключить в `MainActivity`.

**Спринт 2 — компоненты и ключевые экраны, 3–5 дней**
- [ ] `CyberButton`, `CyberPanel`, `CyberChip`, `CyberTopBar`.
- [ ] Миграция: дашборд, Оракул, карточный вьюер.
- [ ] Новый `SkinBackdrop`, обои = скин.

**Спринт 3 — карты, 3–4 дня**
- [ ] «Арт без хрома» + Compose-рамки по редкости.
- [ ] Единый аспект, миниатюры, переименование ресурсов.
- [ ] Сигнатурное раскрытие + haptic.

**Спринт 4 — строки, доступность, тесты, 3–4 дня**
- [ ] Миграция строк в `strings.xml`.
- [ ] Чеклист 4.10, превью, Roborazzi, «храповик».

**Спринт 5 — полировка.** Остальные экраны, сплэш, иконки вместо эмодзи, финальный проход по контрасту.

---

## 8. Definition of Done: «Красиво» ✅

- В русском интерфейсе нет ни одного символа системного шрифта в UI-хроме.
- В экранах нет `Color(0x…)` и `fontSize = …sp` вне `ui/design/`.
- Смена скина меняет и атмосферу, и акценты.
- Текст читается на всех скинах (включая Jamaica) при `fontScale` 1,5.
- Все цели касания ≥48dp, нет «немых» кнопок для TalkBack.
- При «уменьшить анимации» приложение статично и всё ещё красиво.
- Раскрытие карты вызывает «вау» и отзывается haptic'ом.
- Один бренд-нейм на иконке, сплэше, топ-баре и в сторе.
- Скриншот-тесты зелёные на всех скинах.

---

## Приложение A. Исходные метрики (baseline)

| Метрика | Значение |
|---------|----------|
| Литералы `Color(0xAARRGGBB)` в UI | **811** (259 уникальных) |
| `MaterialTheme.colorScheme` / `.typography` в UI | **0 / 0** |
| Литералы `fontSize = N.sp` | **810** |
| `FontFamily.Monospace` | **108** |
| Текст ≤10sp | **91** |
| Разных радиусов скругления | **10** (8dp ×97, 12dp ×90, 4dp ×88, 6dp ×49, 16dp ×32, 10dp ×31, …) |
| `contentDescription = null` / с текстом | **48 / 69** |
| `semantics` / `Role` | **1** |
| `stringResource` | **0** |
| Инлайн `if (isRu…)` / кириллические литералы | **227 / 506** строк |
| `rememberSaveable` / `remember { mutableStateOf }` | **0 / 171** |
| `BackHandler` | **2** |
| `rememberInfiniteTransition` | **14** |
| Проверки reduced motion / haptic / `@Preview` | **0 / 0 / 0** |
| Покрытие кириллицы: Orbitron / Rajdhani | **0/64 / 0/64** |
| Распакованный размер лица карты (896×1200) | ≈4,3 МБ |

## Приложение B. Скрипты

**B.1. Проверка покрытия кириллицы шрифтом**

```python
# pip install fonttools
from fontTools.ttLib import TTFont
import sys
for path in sys.argv[1:]:
    cmap = TTFont(path).getBestCmap()
    up = sum(1 for c in range(0x0410, 0x0430) if c in cmap)   # А–Я
    lo = sum(1 for c in range(0x0430, 0x0450) if c in cmap)   # а–я
    yo = (0x0401 in cmap) and (0x0451 in cmap)                # Ё ё
    print(f"{path}: заглавные {up}/32, строчные {lo}/32, ёЁ {'ok' if yo else 'нет'}")
```
Запуск: `python check_fonts.py res/font/*.ttf`. Нужно 32/32, 32/32, ok.

**B.2. «Храповик» дизайн-долга для CI**

```bash
#!/usr/bin/env bash
# scripts/design-lint.sh — хардкод может только убывать
set -euo pipefail
SRC=app/src/main/java/com/example
DIRS="$SRC/ui $SRC/deckforge $SRC/duel $SRC/forge $SRC/p2p"
cnt() { grep -rE "$1" $DIRS --include=*.kt --exclude-dir=theme --exclude-dir=design | wc -l; }
colors=$(cnt 'Color\(0x[0-9A-Fa-f]{8}\)')
sizes=$(cnt 'fontSize *= *[0-9.]+\.sp')
mono=$(cnt 'FontFamily\.Monospace')
read -r b_colors b_sizes b_mono < design-baseline.txt     # храни базу в репозитории
echo "colors=$colors/$b_colors sizes=$sizes/$b_sizes mono=$mono/$b_mono"
if [ "$colors" -gt "$b_colors" ] || [ "$sizes" -gt "$b_sizes" ] || [ "$mono" -gt "$b_mono" ]; then
  echo "Design debt increased"; exit 1
fi
echo "$colors $sizes $mono" > design-baseline.txt          # фиксируем улучшение
```

**B.3. Список кириллических литералов для миграции строк**

```bash
grep -rnP '"[^"]*[А-Яа-я][^"]*"' app/src/main/java/com/example/ui \
  app/src/main/java/com/example/deckforge app/src/main/java/com/example/duel \
  app/src/main/java/com/example/forge app/src/main/java/com/example/p2p > strings-todo.txt
```

---

Это всё, друг. Сначала шрифты и тема, потом тишина вокруг арта, а потом сыграй карту так, чтобы мурашки по спине. Арт уже сделал половину работы. Теперь дело за нами.

**Делаем Красиво — и это будет весело!** 🌿✨🔮
