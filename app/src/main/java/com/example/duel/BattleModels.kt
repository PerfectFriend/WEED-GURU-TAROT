package com.example.duel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import java.util.UUID

enum class ElementAffinity(
    val displayNameEn: String,
    val displayNameRu: String,
    val symbol: String,
    val color: Color
) {
    CYBER("Cyber", "Кибер", "⚡", Color(0xFF00F5FF)),
    BIO("Bio", "Био", "🌿", Color(0xFF00FF66)),
    QUANTUM("Quantum", "Квант", "🔮", Color(0xFFBD00FF)),
    VOID("Void", "Пустота", "🌑", Color(0xFFFF5252));

    fun getMultiplierAgainst(target: ElementAffinity): Float {
        return when (this) {
            CYBER -> when (target) {
                BIO -> 1.5f
                QUANTUM -> 0.75f
                else -> 1.0f
            }
            BIO -> when (target) {
                VOID -> 1.5f
                CYBER -> 0.75f
                else -> 1.0f
            }
            QUANTUM -> when (target) {
                CYBER -> 1.5f
                VOID -> 0.75f
                else -> 1.0f
            }
            VOID -> when (target) {
                QUANTUM -> 1.5f
                BIO -> 0.75f
                else -> 1.0f
            }
        }
    }
}

enum class CardSpecialEffect(
    val titleEn: String,
    val titleRu: String,
    val descriptionEn: String,
    val descriptionRu: String
) {
    OVERCHARGE("Overcharge", "Перегрузка", "Deals +3 instant burst damage on deploy", "Наносит +3 мгновенного урона при выкладке"),
    BIO_LEECH("Bio Leech", "Био-Дренаж", "Restores +3 HP to your avatar on attack", "Восстанавливает +3 HP аватару при атаке"),
    QUANTUM_SHIELD("Quantum Shield", "Квантовый Щит", "Grants +4 shield barrier to avatar", "Дает +4 очков щита вашему аватару"),
    VOID_DRAIN("Void Drain", "Слив Данных", "Drains 1 enemy RAM node", "Сжигает 1 единицу RAM у противника"),
    COUNTER_STRIKE("Spike Protocol", "Контрудар", "Reflects 2 damage to any attacker", "Отражает 2 урона любому атакующему")
}

@Stable
data class BattleCard(
    val instanceId: String = UUID.randomUUID().toString(),
    val cardIndex: Int,
    val name: String,
    val traditionalName: String,
    val element: ElementAffinity,
    val energyCost: Int,
    val basePower: Int,
    var currentPower: Int = basePower,
    val baseDefense: Int,
    var currentDefense: Int = baseDefense,
    val specialEffect: CardSpecialEffect?,
    val isUpright: Boolean = true,
    var hasAttackedThisTurn: Boolean = false,
    val rarity: String = "Common",
    val description: String = ""
) {
    val isDead: Boolean
        get() = currentDefense <= 0
}

enum class DuelWinner {
    PLAYER, ENEMY, DRAW
}

@Immutable
data class DuelState(
    val turnNumber: Int = 1,
    val isPlayerTurn: Boolean = true,
    val isAiThinking: Boolean = false,
    val playerHp: Int = 30,
    val playerMaxHp: Int = 30,
    val playerShield: Int = 0,
    val playerEnergy: Int = 2,
    val playerMaxEnergy: Int = 2,
    val enemyHp: Int = 30,
    val enemyMaxHp: Int = 30,
    val enemyShield: Int = 0,
    val enemyEnergy: Int = 2,
    val enemyMaxEnergy: Int = 2,
    val playerHand: List<BattleCard> = emptyList(),
    val enemyHandSize: Int = 4,
    val playerBoard: List<BattleCard?> = listOf(null, null, null), // 3 tactical combat lanes
    val enemyBoard: List<BattleCard?> = listOf(null, null, null),  // 3 opposing tactical lanes
    val combatLog: List<String> = emptyList(),
    val winner: DuelWinner? = null,
    val difficulty: String = "Normal" // "Normal", "Hardcore", "Glitch"
)

object BattleCardFactory {
    fun fromTarot(card: CyberTarotCard, isUpright: Boolean = true): BattleCard {
        val element = when (card.index % 4) {
            0 -> ElementAffinity.CYBER
            1 -> ElementAffinity.BIO
            2 -> ElementAffinity.QUANTUM
            else -> ElementAffinity.VOID
        }

        val (cost, power, defense) = when (card.defaultRarity) {
            "Genesis" -> Triple(5, 9, 8)
            "Legend" -> Triple(5, 8, 7)
            "Epic" -> Triple(4, 6, 6)
            "Rare" -> Triple(3, 4, 5)
            else -> {
                // Commons: scale based on card index
                val scaledCost = (1 + (card.index % 3))
                val scaledPwr = (2 + (card.index % 4))
                val scaledDef = (2 + ((card.index + 1) % 4))
                Triple(scaledCost, scaledPwr, scaledDef)
            }
        }

        val effect = when {
            card.index % 7 == 0 -> CardSpecialEffect.OVERCHARGE
            card.index % 7 == 1 -> CardSpecialEffect.BIO_LEECH
            card.index % 7 == 2 -> CardSpecialEffect.QUANTUM_SHIELD
            card.index % 7 == 3 -> CardSpecialEffect.VOID_DRAIN
            card.index % 7 == 4 -> CardSpecialEffect.COUNTER_STRIKE
            else -> null
        }

        val finalPower = if (isUpright) power else (power + 1).coerceAtLeast(1)
        val finalDefense = if (isUpright) defense else (defense - 1).coerceAtLeast(1)

        return BattleCard(
            cardIndex = card.index,
            name = card.name,
            traditionalName = card.traditionalName,
            element = element,
            energyCost = cost,
            basePower = finalPower,
            currentPower = finalPower,
            baseDefense = finalDefense,
            currentDefense = finalDefense,
            specialEffect = effect,
            isUpright = isUpright,
            rarity = card.defaultRarity,
            description = card.cyberMeaning
        )
    }

    fun generateStarterDeck(): List<BattleCard> {
        val selectedIndices = listOf(0, 1, 2, 3, 4, 7, 10, 11, 13, 17, 18, 19, 21, 25, 30)
        return selectedIndices.mapNotNull { idx ->
            CyberTarotDeck.cards.getOrNull(idx)?.let { fromTarot(it, isUpright = true) }
        }
    }

    fun generateEnemyDeck(difficulty: String): List<BattleCard> {
        val pool = CyberTarotDeck.cards.shuffled().take(15)
        return pool.mapIndexed { i, card ->
            val upright = (i % 3 != 0)
            fromTarot(card, isUpright = upright)
        }
    }
}
