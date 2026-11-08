package com.example.model

import androidx.compose.runtime.Immutable
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck

enum class ArcanaType {
    MAJOR,
    MINOR
}

enum class CyberSuit(val title: String, val elementalPower: String) {
    CIRCUITS("Circuits", "Fire / Vegetative Vitality / Energy"),
    SYNAPSE("Synapse", "Air / Neural Intellect / Logic Gates"),
    NECTAR("Nectar", "Water / Terpene Emotion / Fluid Resin"),
    CHIPS("Chips", "Earth / Hardware Substance / Crystals & Soil")
}

enum class CardRarity(val label: String, val colorHex: String) {
    COMMON("Common", "#39FF14"),
    RARE("Rare", "#00FFFF"),
    EPIC("Epic", "#FF00FF"),
    LEGEND("Legend", "#FFA500"),
    GENESIS("Genesis", "#FFD700")
}

enum class CardOrientation {
    UPRIGHT,
    REVERSED;

    val isUpright: Boolean get() = this == UPRIGHT
}

@Immutable
data class CyberTarotCardModel(
    val id: Int,
    val name: String,
    val traditionalName: String,
    val arcana: ArcanaType,
    val suit: CyberSuit? = null,
    val number: Int = id,
    val description: String,
    val uprightKeywords: List<String>,
    val reversedKeywords: List<String>,
    val circuitryElement: String,
    val cyberMeaning: String,
    val uprightAdvice: String,
    val reversedAdvice: String,
    val rarity: CardRarity = CardRarity.COMMON,
    val series: String = "Cyber Botanica"
) {
    val rarityColorHex: String get() = rarity.colorHex
    val isMajorArcana: Boolean get() = arcana == ArcanaType.MAJOR

    fun toCyberTarotCard(): CyberTarotCard {
        return CyberTarotCard(
            index = id,
            name = name,
            traditionalName = traditionalName,
            description = description,
            uprightKeywords = uprightKeywords,
            reversedKeywords = reversedKeywords,
            circuitryElement = circuitryElement,
            cyberMeaning = cyberMeaning,
            uprightAdvice = uprightAdvice,
            reversedAdvice = reversedAdvice,
            defaultRarity = rarity.label,
            series = series
        )
    }

    companion object {
        fun fromCyberTarotCard(card: CyberTarotCard): CyberTarotCardModel {
            val rarity = when (card.defaultRarity) {
                "Genesis" -> CardRarity.GENESIS
                "Legend" -> CardRarity.LEGEND
                "Epic" -> CardRarity.EPIC
                "Rare" -> CardRarity.RARE
                else -> CardRarity.COMMON
            }
            val arcana = if (card.index in 0..21) ArcanaType.MAJOR else ArcanaType.MINOR
            val suit = when {
                card.series.contains("Amber", ignoreCase = true) -> CyberSuit.NECTAR
                card.series.contains("Ocean", ignoreCase = true) -> CyberSuit.NECTAR
                card.circuitryElement.contains("Chip", ignoreCase = true) -> CyberSuit.CHIPS
                card.circuitryElement.contains("Synapse", ignoreCase = true) -> CyberSuit.SYNAPSE
                else -> CyberSuit.CIRCUITS
            }
            return CyberTarotCardModel(
                id = card.index,
                name = card.name,
                traditionalName = card.traditionalName,
                arcana = arcana,
                suit = suit,
                number = card.index,
                description = card.description,
                uprightKeywords = card.uprightKeywords,
                reversedKeywords = card.reversedKeywords,
                circuitryElement = card.circuitryElement,
                cyberMeaning = card.cyberMeaning,
                uprightAdvice = card.uprightAdvice,
                reversedAdvice = card.reversedAdvice,
                rarity = rarity,
                series = card.series
            )
        }
    }
}

@Immutable
data class SpreadPosition(
    val index: Int,
    val titleEn: String,
    val titleRu: String,
    val significanceEn: String,
    val significanceRu: String
)

enum class SpreadType(
    val titleEn: String,
    val titleRu: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val cardCount: Int
) {
    SINGLE_ORACLE(
        titleEn = "Single Oracle",
        titleRu = "Одиночный Оракул",
        descriptionEn = "Daily focal point or immediate direct insight for the question.",
        descriptionRu = "Фокус дня или мгновенный ответ на конкретный вопрос.",
        cardCount = 1
    ),
    CYBER_TRINITY(
        titleEn = "Cyber Trinity (3 Cards)",
        titleRu = "Кибер-Триада (3 Карты)",
        descriptionEn = "Timeline stream: Past causes, Current matrix state, Projected outcome.",
        descriptionRu = "Временной поток: Истоки в прошлом, Текущее состояние, Проекция будущего.",
        cardCount = 3
    ),
    FIVE_NODE_CROSS(
        titleEn = "Elemental Cross (5 Cards)",
        titleRu = "Стихийный Крест (5 Карт)",
        descriptionEn = "Comprehensive node mapping: Core situation, Obstacle, Subconscious root, Crown potential, Final resolution.",
        descriptionRu = "Глубокая развертка: Ядро ситуации, Преграда, Подсознательный корень, Высший потенциал, Итог.",
        cardCount = 5
    ),
    CELTIC_MATRIX(
        titleEn = "Celtic Matrix (10 Cards)",
        titleRu = "Кельтская Матрица (10 Карт)",
        descriptionEn = "Master quantum decryption across 10 interconnected dimensional axes.",
        descriptionRu = "Полная квантовая дешифровка по 10 взаимосвязанным узлам матрицы.",
        cardCount = 10
    );

    val positions: List<SpreadPosition>
        get() = when (this) {
            SINGLE_ORACLE -> listOf(
                SpreadPosition(0, "Oracle Synthesis", "Оракул Синтез", "Primary transmission and core guidance", "Основное послание и руководство")
            )
            CYBER_TRINITY -> listOf(
                SpreadPosition(0, "Subroutine Origin", "Исток Подпрограммы", "Underlying past influences & root code", "Влияния прошлого и исходный код"),
                SpreadPosition(1, "Current Core State", "Текущее Состояние", "Active node dynamics in this moment", "Активная динамика текущего момента"),
                SpreadPosition(2, "Quantum Horizon", "Квантовый Горизонт", "Projected outcome if the current cycle holds", "Прогнозируемый результат при сохранении тренда")
            )
            FIVE_NODE_CROSS -> listOf(
                SpreadPosition(0, "Core Node", "Центральный Узел", "The heart of your question or situation", "Суть вопроса или текущей ситуации"),
                SpreadPosition(1, "Cross Interference", "Поперечная Помеха", "Challenge, resistance, or hidden friction", "Вызов, сопротивление или помеха"),
                SpreadPosition(2, "Sub-layer Root", "Глубинный Корень", "Subconscious seed or foundation", "Подсознательный исток или фундамент"),
                SpreadPosition(3, "Crown Voltage", "Коронный Потенциал", "Highest aspiration or ideal possibility", "Высший потенциал или лучшая возможность"),
                SpreadPosition(4, "Synthesis Output", "Выходной Синтез", "Decisive evolutionary step forward", "Ключевой шаг развития и исход")
            )
            CELTIC_MATRIX -> listOf(
                SpreadPosition(0, "Present Matrix", "Текущая Матрица", "The immediate situation", "Текущее положение дел"),
                SpreadPosition(1, "Interference Vector", "Вектор Помех", "Immediate obstacle or test", "Непосредственная преграда или вызов"),
                SpreadPosition(2, "Crown Consciousness", "Сознание Венца", "Best conscious goal or possibility", "Осознанная цель или идеальный вариант"),
                SpreadPosition(3, "Subconscious Bedrock", "Подсознательный Базис", "Hidden foundation & instincts", "Скрытый фундамент и инстинкты"),
                SpreadPosition(4, "Recent Subroutine", "Недавняя Подпрограмма", "Influences just passing away", "Недавно завершившиеся влияния"),
                SpreadPosition(5, "Approaching Signal", "Приближающийся Сигнал", "Forces entering your perimeter", "Силы, входящие в ваше поле"),
                SpreadPosition(6, "Self Projection", "Проекция Себя", "Your inner attitude and power", "Ваше внутреннее состояние и сила"),
                SpreadPosition(7, "Surrounding Network", "Окружающая Сеть", "External environment and allies", "Внешняя среда и окружение"),
                SpreadPosition(8, "Hopes & Paradoxes", "Надежды и Опасения", "Inner tensions, desires, and anxieties", "Внутренние стремления и тревоги"),
                SpreadPosition(9, "Final Convergence", "Финальная Конвергенция", "Long-range synthesis and ultimate advice", "Долгосрочный исход и финальный совет")
            )
        }
}

@Immutable
data class TarotSpread(
    val type: SpreadType,
    val title: String = type.titleEn,
    val description: String = type.descriptionEn,
    val cardCount: Int = type.cardCount,
    val positions: List<SpreadPosition> = type.positions
)

@Immutable
data class DrawnCard(
    val card: CyberTarotCardModel,
    val orientation: CardOrientation = CardOrientation.UPRIGHT,
    val position: SpreadPosition,
    val isRevealed: Boolean = false,
    val selectionOrder: Int = 0
) {
    val isUpright: Boolean get() = orientation.isUpright
    val effectiveAdvice: String get() = if (isUpright) card.uprightAdvice else card.reversedAdvice
    val effectiveKeywords: List<String> get() = if (isUpright) card.uprightKeywords else card.reversedKeywords
}

enum class ShuffleMethod(val titleEn: String, val titleRu: String, val description: String) {
    QUANTUM_ENTROPY(
        "Quantum Entropy",
        "Квантовая Энтропия",
        "Cryptographically secure pseudo-random seed shuffle."
    ),
    RIFFLE(
        "Cyber Riffle",
        "Кибер-Риффл",
        "Splits the deck into two interleaving data streams."
    ),
    OVERHAND(
        "Matrix Overhand",
        "Матричный Оверхенд",
        "Progressively reorders blocks of cards in cascading packets."
    ),
    CORONA_WASH(
        "Corona Wash",
        "Вихревой Поток",
        "Chaotic circular mixing with bidirectional orientation shifts."
    )
}

enum class ReadingPhase {
    SETUP,
    SHUFFLING,
    CUTTING,
    CARD_SELECTION,
    REVEALING,
    SYNTHESIS,
    COMPLETED
}

@Immutable
data class ReadingSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val spread: TarotSpread,
    val drawnCards: List<DrawnCard>,
    val interpretation: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userNotes: String = "",
    val isSaved: Boolean = false
)

object CyberTarotDeckFactory {
    fun createDefaultDeck(): List<CyberTarotCardModel> {
        return CyberTarotDeck.cards.map { CyberTarotCardModel.fromCyberTarotCard(it) }
    }
}
