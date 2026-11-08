# ТЕХНИЧЕСКОЕ ЗАДАНИЕ (ТЗ): УНИВЕРСАЛЬНАЯ МУЛЬТИПЛАТФОРМЕННАЯ ЭКОСИСТЕМА CYBER TAROT TCG & ORACLE (WEED GURU)

**Версия спецификации:** 2.0.0-UNIFIED  
**Статус документа:** Окончательный (Production Specification)  
**Целевые системы генерации:** Grok (xAI), Claude (Anthropic), Kimi (Moonshot), DeepSeek, OpenAI, Human Developers  
**Кроссплатформенная совместимость:** 100% бинарная, протокольная и логическая синхронизация между Android (Jetpack Compose / Kotlin), Web (React / TypeScript), Desktop/Cross-platform (Flutter / Dart, Tauri / Rust, Godot / C#), и Backend (Python / FastAPI / Node.js).

---

## 1. ВВЕДЕНИЕ И ЦЕЛЬ ПРОЕКТА

### 1.1. Назначение
Настоящее Техническое Задание (ТЗ) исчерпывающе и однозначно описывает архитектуру, протоколы передачи данных, математические формулы, структуры баз данных, правила карточной боевки (TCG), экономику и интерфейсные контракты проекта **Cyber Tarot TCG & Neural Oracle** (известного как "Weed Guru").

### 1.2. Принцип кроссплатформенной эквивалентности
Любая реализация проекта на базе моделей Grok, Claude, Kimi, DeepSeek обязана строго соблюдать:
1. **Детерминированность боевого движка:** одинаковый порядок действий и одинаковые входные данные должны приводить к идентичному состоянию боевой арены.
2. **Идентичность матриц стихий и формул урона:** 4 стихии (Cyber, Bio, Quantum, Void) с множителями 1.5x, 0.75x, 1.0x.
3. **Единый Wire-протокол P2P и сетевого взаимодействия:** бинарная/JSON совместимость пакетов WebSocket, WebRTC и P2P-сокетов.
4. **Совместимость структур данных:** единый реестр карт (78 Арканов), формат профиля игрока, колод и апгрейдов.

---

## 2. КАТАЛОГ СТИХИЙ И СПЕЦЭФФЕКТОВ (ELEMENTAL MATRIX & EFFECTS)

### 2.1. Перечисление стихий (ElementAffinity)
Все карты принадлежат строго одной из 4 стихий:

```json
[
  { "id": "CYBER", "nameRu": "Кибер", "nameEn": "Cyber", "symbol": "⚡", "colorHex": "#00F5FF" },
  { "id": "BIO", "nameRu": "Био", "nameEn": "Bio", "symbol": "🌿", "colorHex": "#00FF66" },
  { "id": "QUANTUM", "nameRu": "Квант", "nameEn": "Quantum", "symbol": "🔮", "colorHex": "#BD00FF" },
  { "id": "VOID", "nameRu": "Пустота", "nameEn": "Void", "symbol": "🌑", "colorHex": "#FF5252" }
]
```

#### Матрица стихийного урона (getMultiplierAgainst):
- **CYBER** бьет **BIO** с множителем **1.5x**; по **QUANTUM** наносит **0.75x**; по остальным — **1.0x**.
- **BIO** бьет **VOID** с множителем **1.5x**; по **CYBER** наносит **0.75x**; по остальным — **1.0x**.
- **QUANTUM** бьет **CYBER** с множителем **1.5x**; по **VOID** наносит **0.75x**; по остальным — **1.0x**.
- **VOID** бьет **QUANTUM** с множителем **1.5x**; по **BIO** наносит **0.75x**; по остальным — **1.0x**.

Формула расчета урона между картами:
$$\text{damage} = \max\left(1, \lfloor \text{attackerPower} \times \text{elementMultiplier} \rfloor \right)$$

### 2.2. Спецэффекты карт (CardSpecialEffect)

| Код эффекта | Название (RU / EN) | Триггер срабатывания | Описание механики |
| :--- | :--- | :--- | :--- |
| `OVERCHARGE` | Перегрузка / Overcharge | **ON_DEPLOY** (выкладка на стол) | Мгновенно наносит 3 единицы урона напрямую вражескому ядру (Core HP). |
| `BIO_LEECH` | Био-Дренаж / Bio Leech | **ON_ATTACK** (совершение атаки) | Восстанавливает +3 HP аватару игрока (до `maxHp`). |
| `QUANTUM_SHIELD` | Квантовый Щит / Quantum Shield | **ON_DEPLOY** (выкладка на стол) | Добавляет +4 очков щита (`shield`) аватару игрока. |
| `VOID_DRAIN` | Слив Данных / Void Drain | **ON_DEPLOY** (выкладка на стол) | Сжигает 1 единицу RAM у соперника (минимум 0). |
| `COUNTER_STRIKE` | Шипы / Spike Protocol | **ON_DEFEND** (при получении удара) | Если защищающийся аркан выжил, он отражает 2 дополнительного урона атакующему. |

---

## 3. СТРУКТУРЫ ДАННЫХ И СХЕМЫ (DATA SCHEMAS)

### 3.1. Карта Таро / Базовая сущность (CyberTarotCard)
```typescript
interface CyberTarotCard {
  index: number;               // 0..77
  name: string;                // Cyberpunk/Grower название (e.g. "The Sprout")
  traditionalName: string;     // Классическое Таро (e.g. "The Fool")
  description: string;         // Художественное описание
  uprightKeywords: string[];   // Ключевые слова прямого положения
  reversedKeywords: string[];  // Ключевые слова перевернутого положения
  circuitryElement: string;    // Аппаратный/ботанический компонент
  cyberMeaning: string;        // Кибернетическое толкование
  uprightAdvice: string;       // Совет при прямом выпадении
  reversedAdvice: string;      // Совет при перевернутом выпадении
  defaultRarity: "Common" | "Rare" | "Epic" | "Legend" | "Genesis";
  series: string;              // "Major Arcana", "Cyber Botanica", "Amber Series", "Seas & Oceans"
}
```

### 3.2. Боевая карта (BattleCard)
Используется в дуэльном движке:
```typescript
interface BattleCard {
  instanceId: string;          // UUID v4 уникального экземпляра в матче
  cardIndex: number;           // Ссылка на CyberTarotCard.index (0..77)
  name: string;
  traditionalName: string;
  element: "CYBER" | "BIO" | "QUANTUM" | "VOID";
  energyCost: number;          // Стоимость выкладки в RAM (1..10)
  basePower: number;           // Базовая атака
  currentPower: number;        // Текущая атака с учетом апгрейдов и баффов
  baseDefense: number;         // Базовое здоровье/защита
  currentDefense: number;      // Текущее здоровье (при <= 0 карта погибает)
  specialEffect: "OVERCHARGE" | "BIO_LEECH" | "QUANTUM_SHIELD" | "VOID_DRAIN" | "COUNTER_STRIKE" | null;
  isUpright: boolean;          // true = прямое, false = перевернутое (+1 атака, -1 защита)
  hasAttackedThisTurn: boolean;// флаг активности в текущем раунде
  rarity: "Common" | "Rare" | "Epic" | "Legend" | "Genesis";
  description: string;
}
```

### 3.3. Состояние матча (DuelState)
Полное синхронизируемое состояние дуэли:
```typescript
interface DuelState {
  turnNumber: number;              // Номер хода (начиная с 1)
  isPlayerTurn: boolean;           // true, если ход локального игрока
  isAiThinking: boolean;           // флаг ожидания ответа ИИ / удаленного пира
  playerHp: number;                // Здоровье игрока (базово 30)
  playerMaxHp: number;             // Максимальное здоровье игрока (30)
  playerShield: number;            // Очки щита игрока (0+)
  playerEnergy: number;            // Текущий доступный RAM (начинается с 2)
  playerMaxEnergy: number;         // Максимальный RAM текущего хода (2..10)
  enemyHp: number;                 // Здоровье соперника
  enemyMaxHp: number;              // Максимум здоровья соперника
  enemyShield: number;             // Очки щита соперника
  enemyEnergy: number;             // Текущий RAM соперника
  enemyMaxEnergy: number;          // Максимальный RAM соперника
  playerHand: BattleCard[];        // Карты в руке игрока (максимум 6)
  enemyHandSize: number;           // Количество карт в руке соперника
  playerBoard: (BattleCard | null)[]; // 3 тактических слота (индексы 0, 1, 2)
  enemyBoard: (BattleCard | null)[];  // 3 вражеских слота (индексы 0, 1, 2)
  combatLog: string[];             // Последние 20 записей боевого журнала
  winner: "PLAYER" | "ENEMY" | "DRAW" | null;
  difficulty: "Normal" | "Hardcore" | "Glitch";
}
```

### 3.4. Сохраненная колода (SavedDeck)
```typescript
interface SavedDeck {
  id: number;                      // Auto-increment ID (1..5)
  name: string;                    // Название (макс 24 символа)
  icon: string;                    // Эмодзи иконка (⚡, 🌿, 🔮, 🌑, ⚔️)
  cardIndices: number[];           // Ровно 15 индексов карт (от 0 до 77)
  isActive: boolean;               // Активна ли для входа в бой
  description: string;             // Тактическое описание
  createdAt: number;               // Unix timestamp (мс)
}
```

### 3.5. Апгрейд карты (CardUpgrade)
```typescript
interface CardUpgrade {
  cardIndex: number;               // Первичный ключ
  level: number;                   // 0..5
  powerBonus: number;              // level * 1
  defenseBonus: number;            // level * 1
  ramReduction: number;            // 1 если level >= 3, иначе 0
  unlockedSkin: string;            // "default" | "neon_noir" | "golden_genesis" | "void_glitch"
  lastUpgradedAt: number;          // Unix timestamp (мс)
}
```

### 3.6. Профиль пользователя (UserProfile)
```typescript
interface UserProfile {
  id: number;                      // ID (1)
  alias: string;                   // Имя / Никнейм
  weed: number;                    // Баланс игровой валюты WEED (кредиты)
  systemLanguage: string;          // "ru" | "en" | "de" | "es" | "fr" | "zh"
  currentSkin: string;             // ID выбранной темы оформления
  totalDuelsWon: number;
  totalDuelsLost: number;
  unlockedCardsCount: number;
}
```

---

## 4. ДЕТЕРМИНИРОВАННЫЙ БОЕВОЙ ДВИЖОК (COMBAT ENGINE PROTOCOL)

### 4.1. Инициализация матча (`startMatch`)
1. **Колода игрока:** Загружается активная колода (15 карт). Если колоды нет, формируется стандартная стартовая колода: `[0, 1, 2, 3, 4, 7, 10, 11, 13, 17, 18, 19, 21, 25, 30]`.
2. **Колода соперника:** 15 случайных карт из пула с учетом сложности:
   - `Normal`: 30 HP, стандартные параметры.
   - `Hardcore`: 35 HP.
   - `Glitch`: 40 HP.
3. Обе колоды перемешиваются детерминированным генератором случайных чисел (PRNG с общим `seed`).
4. В руку игрока сдаются 4 начальные карты.
5. Параметры: `turnNumber = 1`, `playerEnergy = 2`, `playerMaxEnergy = 2`, `enemyEnergy = 2`, `enemyMaxEnergy = 2`.
6. Поле: 3 пустых слота у игрока, 3 пустых слота у врага.

### 4.2. Розыгрыш карты (`playCard`)
**Предусловия валидации:**
- Текущий ход принадлежит игроку (`isPlayerTurn == true`, `isAiThinking == false`, `winner == null`).
- Слот `targetSlot` находится в диапазоне `[0..2]`.
- Слот `playerBoard[targetSlot]` пуст (`null`).
- Достаточно маны: `playerEnergy >= card.energyCost`.

**Шаги выполнения:**
1. Списание энергии: `playerEnergy = playerEnergy - card.energyCost`.
2. Удаление карты из `playerHand`.
3. Помещение копии карты в `playerBoard[targetSlot]` со сброшенным флагом `hasAttackedThisTurn = false`.
4. Срабатывание триггера `ON_DEPLOY`:
   - `QUANTUM_SHIELD`: `playerShield += 4`.
   - `OVERCHARGE`: `enemyHp = max(0, enemyHp - 3)`.
   - `VOID_DRAIN`: `enemyEnergy = max(0, enemyEnergy - 1)`.
5. Проверка победы: если `enemyHp <= 0`, установить `winner = "PLAYER"` и начислить награду.

### 4.3. Атака слотом (`attackWithSlot`)
**Предусловия:**
- Слот `slotIndex` занят картой `attacker`.
- `attacker.hasAttackedThisTurn == false`.
- `isPlayerTurn == true`, `winner == null`.

**Сценарий А: Слот соперника занят картой `defender`**
1. Вычисление множителя: `mult = attacker.element.getMultiplierAgainst(defender.element)`.
2. Нанесение урона защитнику: `dmg = max(1, floor(attacker.currentPower * mult))`.
   `defender.currentDefense -= dmg`.
3. Контратака защитника (если `defender.currentDefense > 0`):
   - `counterMult = defender.element.getMultiplierAgainst(attacker.element)`.
   - `counterDmg = max(1, floor(defender.currentPower * counterMult))`.
   - `attacker.currentDefense -= counterDmg`.
   - Если `defender.specialEffect == "COUNTER_STRIKE"`: `attacker.currentDefense -= 2`.
4. Удаление погибших карт (`currentDefense <= 0`):
   - Если `defender.isDead`: `enemyBoard[slotIndex] = null`.
   - Если `attacker.isDead`: `playerBoard[slotIndex] = null`.
   - Если выжил: `attacker.hasAttackedThisTurn = true`.

**Сценарий Б: Слот соперника пуст (`enemyBoard[slotIndex] == null`)**
1. Прямой удар в ядро соперника (Direct Core Hit):
   - `rawDmg = attacker.currentPower`.
   - Если `enemyShield > 0`:
     - `absorbed = min(enemyShield, rawDmg)`.
     - `enemyShield -= absorbed`.
     - `leftover = rawDmg - absorbed`.
     - `enemyHp = max(0, enemyHp - leftover)`.
   - Иначе: `enemyHp = max(0, enemyHp - rawDmg)`.
2. Установка `attacker.hasAttackedThisTurn = true`.

**Общие эффекты атаки:**
- Если `attacker.specialEffect == "BIO_LEECH"`: `playerHp = min(playerMaxHp, playerHp + 3)`.
- Проверка завершения матча:
  - `enemyHp <= 0` $\rightarrow$ `winner = "PLAYER"`.
  - `playerHp <= 0` $\rightarrow$ `winner = "ENEMY"`.

### 4.4. Завершение хода и алгоритм ИИ (`executeAiTurn`)
1. Сброс флагов атаки всех карт игрока на столе (`hasAttackedThisTurn = false`).
2. Прирост RAM ИИ: `enemyMaxEnergy = min(10, enemyMaxEnergy + 1)`, `enemyEnergy = enemyMaxEnergy`.
3. Дроп карты в руку ИИ (до максимума 6).
4. **Фаза розыгрыша ИИ:**
   - Поиск свободных слотов `emptySlots` на поле врага.
   - Если есть слот, есть карты в колоде ИИ и `enemyEnergy >= card.energyCost`:
     - Выкладка карты, списание энергии, активация `ON_DEPLOY`.
5. **Фаза атаки ИИ (по слотам 0, 1, 2):**
   - Для каждого слота `i`, где есть карта ИИ:
     - Если в слоте игрока `playerBoard[i]` есть карта $\rightarrow$ столкновение по правилам 4.3 (Сценарий А).
     - Если слот игрока пуст $\rightarrow$ прямой урон в ядро игрока с поглощением щитом (Сценарий Б).
6. **Возврат хода игроку:**
   - Прирост RAM игрока: `playerMaxEnergy = min(10, playerMaxEnergy + 1)`, `playerEnergy = playerMaxEnergy`.
   - Дроп 1 карты из колоды игрока в руку (если размер руки < 6).
   - Сброс флагов атак карт ИИ.
   - `turnNumber += 1`, `isPlayerTurn = true`.

### 4.5. Награда за победу
При победе (`winner == "PLAYER"`) единоразово за матч начисляется:
- **+150 WEED Credits** на баланс пользователя.
- **+50 XP** опыта дуэлиста.

---

## 5. СЕТЕВОЙ WIRE-ПРОТОКОЛ P2P (JSON SPECIFICATION)

Для синхронизации двух игроков по WebRTC DataChannel, WebSocket или локальному сокету используется легковесный JSON-протокол. Все сообщения содержат заголовок `type`, `seq` (монотонный счетчик) и полезную нагрузку `payload`.

### 5.1. Рукопожатие и согласование версии
```json
{
  "type": "P2P_HELLO",
  "seq": 1,
  "payload": {
    "protocolVersion": "2.0.0",
    "nodeId": "k2f3p9zk6h4pxz8m",
    "alias": "Cipher_Alpha",
    "activeDeckName": "Cyber Aggro ⚡",
    "timestamp": 1729177200000
  }
}
```

### 5.2. Приглашение и ответ
```json
{
  "type": "MATCH_INVITE",
  "seq": 2,
  "payload": {
    "matchId": "duel-uuid-8899",
    "stakeWeed": 100,
    "randomSeed": 4294967295
  }
}
```

```json
{
  "type": "MATCH_ACCEPT",
  "seq": 3,
  "payload": {
    "matchId": "duel-uuid-8899",
    "accepted": true
  }
}
```

### 5.3. Действия игрока (Action Packets)

#### Розыгрыш карты:
```json
{
  "type": "ACTION_PLAY_CARD",
  "seq": 10,
  "payload": {
    "instanceId": "card-instance-uuid-1",
    "cardIndex": 13,
    "targetSlot": 1,
    "isUpright": true
  }
}
```

#### Атака слотом:
```json
{
  "type": "ACTION_ATTACK_SLOT",
  "seq": 11,
  "payload": {
    "slotIndex": 1
  }
}
```

#### Передача хода:
```json
{
  "type": "ACTION_END_TURN",
  "seq": 12,
  "payload": {
    "turnNumber": 3
  }
}
```

### 5.4. Синхронизация контрольной суммы (State Hash Verification)
В конце каждого раунда клиенты обмениваются SHA-256 хешем текущего `DuelState` для предотвращения десинхронизации (Desync):
```json
{
  "type": "STATE_VERIFY",
  "seq": 13,
  "payload": {
    "turnNumber": 3,
    "stateHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

---

## 6. ЭКОНОМИКА, КРАФТ И АПГРЕЙДЫ (ECONOMY & SYNTHESIS)

### 6.1. Валюта WEED (Кредиты)
- Начальный баланс нового профиля: **500 WEED**.
- Награды:
  - Победа в дуэли: **+150 WEED**.
  - Ежедневный оракул (Daily Reading): **+50 WEED**.
  - Продажа карты на вторичном рынке: сумма покупателя за вычетом 2% комиссии сети.

### 6.2. Модель улучшения карт (Card Forge)
Карта может быть прокачана от Уровня 0 до Уровня 5.

#### Формула стоимости улучшения:
$$\text{Cost}(\text{targetLevel}) = \text{targetLevel} \times 75 \text{ WEED}$$

- Level 1: $1 \times 75 = 75$ WEED $\rightarrow$ (+1 к атаке, +1 к защите)
- Level 2: $2 \times 75 = 150$ WEED $\rightarrow$ (+2 к атаке, +2 к защите)
- Level 3: $3 \times 75 = 225$ WEED $\rightarrow$ (+3 к атаке, +3 к защите, -1 к стоимости RAM)
- Level 4: $4 \times 75 = 300$ WEED $\rightarrow$ (+4 к атаке, +4 к защите, -1 к стоимости RAM)
- Level 5 (Mastered): $5 \times 75 = 375$ WEED $\rightarrow$ (+5 к атаке, +5 к защите, -1 к стоимости RAM, голографическая рамка)

Суммарная стоимость прокачки карты с 0 до 5 уровня: **1125 WEED**.

---

## 7. НЕЙРОННЫЙ ОРАКУЛ И ИНТЕРПРЕТАТОР (DIVINATION ENGINE)

### 7.1. Расклады (Spreads)
1. **Daily Arcana (1 карта):** карта дня, лунный контекст, руководство гровера.
2. **Cyber-Triptych (3 карты):**
   - Позиция 1: *Root Cause / Семена прошлого*
   - Позиция 2: *Current State / Вегетация настоящего*
   - Позиция 3: *Optimal Harvest / Урожай будущего*
3. **Celtic Matrix (5 карт):** Базовый крест энергий и синтез стихий.

### 7.2. Промпт для нейросетевой интерпретации (LLM Specification)
Для вызова моделей Gemini, Grok, Claude, DeepSeek используется стандартизированный системный промпт:

```
[SYSTEM PROMPT]
You are the Neuro-Tarot Demiurge of Weed Guru. You blend cyberpunk cybernetics, occult tarot symbology, and organic craft botany/gardening wisdom into profound, atmospheric, and highly actionable readings.
Output must be strictly valid JSON conforming to the following schema:
{
  "readingTitle": "string",
  "elementalSynthesis": "string",
  "cardsAnalysis": [
    {
      "cardName": "string",
      "position": "string",
      "orientation": "UPRIGHT" | "REVERSED",
      "interpretation": "string"
    }
  ],
  "tacticalAdvice": "string",
  "botanicalRecommendation": "string",
  "luckyFrequencyHz": number
}
Tone: Cyber-Noir, Philosophical, Atmospheric, Inspiring. Do NOT mention being an AI model.
```

---

## 8. АСТРОНОМИЧЕСКИЙ ЛУННЫЙ КАЛЕНДАРЬ (LUNAR GROWER ENGINE)

### 8.1. Формула фазы Луны
Расчет фазы Луны ведется от известного новолуния эпохи J2000 (6 января 2000 года 18:14 UTC, Julian Day 2451550.26):

$$\text{SynodicMonth} = 29.53058867 \text{ дней}$$
$$\text{DaysSinceEpoch} = \text{CurrentJulianDate} - 2451550.26$$
$$\text{PhaseRatio} = \left(\frac{\text{DaysSinceEpoch}}{\text{SynodicMonth}}\right) \pmod 1.0$$
$$\text{MoonAgeDays} = \text{PhaseRatio} \times 29.53058867$$

### 8.2. Таблица фаз и рекомендаций

| Фаза Луны | Диапазон дней | Символ | Сельскохозяйственное / Гроверское правило |
| :--- | :--- | :---: | :--- |
| **New Moon** (Новолуние) | 0.0 .. 1.84 | 🌑 | Покой растений. Не проводить обрезку. Время планирования и медитации. |
| **Waxing Crescent** (Молодая Луна) | 1.85 .. 5.53 | 🌒 | Идеальное время для замачивания семян и укоренения черенков. |
| **First Quarter** (Первая четверть) | 5.54 .. 9.22 | 🌓 | Активный рост вегетативной массы. Внесение азотных удобрений. |
| **Waxing Gibbous** (Прибывающая Луна) | 9.23 .. 12.91 | 🌔 | Максимальное сокодвижение вверх. Подготовка к переводу на цветение. |
| **Full Moon** (Полнолуние) | 12.92 .. 16.61 | 🌕 | Пиковая концентрация терпенов и смолы. Идеально для харвеста. |
| **Waning Gibbous** (Убывающая Луна) | 16.62 .. 20.29 | 🌖 | Энергия уходит в корневую систему. Подкормка фосфором и калием. |
| **Last Quarter** (Третья четверть) | 20.30 .. 23.98 | 🌗 | Санитарная дефолиация, промывка субстрата чистой водой (Flushing). |
| **Waning Crescent** (Старая Луна) | 23.99 .. 29.53 | 🌘 | Подготовка сушильных шкафов, дезинфекция гроубокса и оборудования. |

---

## 9. АУДИОСИНТЕЗ И ЗВУКОВОЙ ДИЗАЙН (LO-FI SYNTHESIZER SPEC)

Для сохранения максимальной производительности без внешних тяжелых MP3/OGG файлов звук генерируется процедурно в реальном времени.

### 9.1. Параметры аудиопотока
- **Формат:** 16-bit Signed PCM, Little-Endian, Mono.
- **Частота дискретизации (Sample Rate):** 22 050 Гц.
- **Фоновый дрон (Ambient):** Базовый тон 72 Гц (Photosynthesis hum) + субгармоника 36 Гц с медленной синусоидальной LFO модуляцией (0.15 Гц).

### 9.2. Формулы процедурных звуковых эффектов (FX Presets)

| FX ID | Длительность | Формула частоты $f(t)$ | Формула огибающей амплитуды $A(t)$ |
| :--- | :--- | :--- | :--- |
| `CLICK` | 80 мс | $f(t) = 1200 - 800 \cdot (t / T)$ | Экспоненциальный спад: $e^{-40 t}$ |
| `CARD_DRAW` | 450 мс | Свип вверх: $f(t) = 220 + 660 \cdot (t / T)^2$ | Hanning Window: $\sin^2(\pi t / T)$ |
| `SCAN_BEEP` | 350 мс | Модуляция FM: $880 + 120 \sin(30 \pi t)$ | Линейный спад: $1 - (t / T)$ |
| `SUCCESS_CHIME` | 800 мс | Аккорд: 523 Гц ($C_5$) + 659 Гц ($E_5$) + 783 Гц ($G_5$) | Плавное затухание: $(1 - t/T)^2$ |
| `LASER_CLASH` | 300 мс | Резкий спад: $f(t) = 2400 \cdot (1 - t/T)^3$ | Ударный спад: $e^{-15 t}$ |
| `SHIELD_BLOCK` | 250 мс | Металлический тон: 180 Гц + шум (White Noise) | Трапеция с резким спадом |
| `COMBAT_VICTORY`| 1200 мс | Арпеджио: пентатоника 440 $\rightarrow$ 554 $\rightarrow$ 659 $\rightarrow$ 880 Гц | Арпеджио с хвостом реверберации |

---

## 10. ДИЗАЙН-СИСТЕМА И ВИЗУАЛЬНЫЕ ТОКЕНЫ (DESIGN TOKENS)

### 10.1. Цветовая палитра
```css
:root {
  --bg-deep-space: #04060C;
  --bg-surface-dark: #0A0F1D;
  --bg-surface-elevated: #111827;
  
  --accent-cyber-cyan: #00F5FF;
  --accent-bio-green: #00FF66;
  --accent-quantum-purple: #BD00FF;
  --accent-void-red: #FF5252;
  --accent-warning-gold: #FFB800;
  
  --border-subtle: rgba(255, 255, 255, 0.12);
  --border-neon-glow: rgba(0, 245, 255, 0.4);
}
```

### 10.2. Типографика
- **Моноширинный шрифт (HUD, статы, логи, кривая RAM):** `JetBrains Mono`, `Fira Code`, или системный `monospace`.
- **Заголовки и логотипы:** Без засечек, жирное начертание (`FontWeight.Black`, 800-900), межбуквенный интервал `+0.5sp`.

---

## 11. ИНСТРУКЦИЯ ДЛЯ AI-ГЕНЕРАТОРОВ (GROK, CLAUDE, KIMI, DEEPSEEK)

При получении задачи реализовать данный проект на любой платформе (React, Flutter, Swift, Godot, Python):
1. **Строго сохраняйте типы и ID перечислений:** `"CYBER"`, `"BIO"`, `"QUANTUM"`, `"VOID"`.
2. **Не изменяйте формулу урона:** множитель 1.5x при преимуществе, 0.75x при слабости, прямой урон по Core HP при пустом слоте с поглощением щитом.
3. **Используйте одинаковую структуру JSON:** при передаче пакетов по сети не переименовывайте ключи (`instanceId`, `cardIndex`, `slotIndex`, `turnNumber`).
4. **Валидируйте колоды:** минимальный размер колоды для боя — ровно 15 карт.
5. **Всегда проверяйте компиляцию и отсутствие циклических ссылок.**

Данная спецификация является полным и исчерпывающим стандартом проекта.
