package com.example.duel

import com.example.audio.CyberNoirSynthPlayer
import com.example.data.CyberTarotDeck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class DuelCombatEngine {
    private val _state = MutableStateFlow(DuelState())
    val state: StateFlow<DuelState> = _state.asStateFlow()

    private var playerDrawPile: MutableList<BattleCard> = mutableListOf()
    private var enemyDrawPile: MutableList<BattleCard> = mutableListOf()

    fun startMatch(
        difficulty: String = "Normal",
        customPlayerDeck: List<BattleCard>? = null,
        deckName: String = "Genesis Cyber Deck"
    ) {
        val baseDeck = if (!customPlayerDeck.isNullOrEmpty()) {
            customPlayerDeck
        } else {
            BattleCardFactory.generateStarterDeck()
        }
        playerDrawPile = baseDeck.shuffled().toMutableList()
        enemyDrawPile = BattleCardFactory.generateEnemyDeck(difficulty).shuffled().toMutableList()

        val initialHand = mutableListOf<BattleCard>()
        repeat(4) {
            if (playerDrawPile.isNotEmpty()) {
                initialHand.add(playerDrawPile.removeAt(0))
            }
        }

        _state.value = DuelState(
            turnNumber = 1,
            isPlayerTurn = true,
            isAiThinking = false,
            playerHp = 30,
            playerMaxHp = 30,
            playerShield = 0,
            playerEnergy = 2,
            playerMaxEnergy = 2,
            enemyHp = when (difficulty) {
                "Hardcore" -> 35
                "Glitch" -> 40
                else -> 30
            },
            enemyMaxHp = when (difficulty) {
                "Hardcore" -> 35
                "Glitch" -> 40
                else -> 30
            },
            enemyShield = 0,
            enemyEnergy = 2,
            enemyMaxEnergy = 2,
            playerHand = initialHand,
            enemyHandSize = 4,
            playerBoard = listOf(null, null, null),
            enemyBoard = listOf(null, null, null),
            combatLog = listOf(
                "⚔️ [DUEL CORE] Протокол инициализирован.",
                "🗂️ Активная колода: $deckName (${baseDeck.size} карт)",
                "🔋 Входная мощность: 2 RAM. Ваш ход."
            ),
            winner = null,
            difficulty = difficulty
        )

        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SCAN_BEEP)
    }

    fun playCard(card: BattleCard, targetSlot: Int): Boolean {
        val curr = _state.value
        if (!curr.isPlayerTurn || curr.isAiThinking || curr.winner != null) return false
        if (targetSlot !in 0..2) return false
        if (curr.playerBoard[targetSlot] != null) return false
        if (curr.playerEnergy < card.energyCost) return false

        val newHand = curr.playerHand.toMutableList()
        newHand.removeAll { it.instanceId == card.instanceId }

        val newBoard = curr.playerBoard.toMutableList()
        newBoard[targetSlot] = card.copy(hasAttackedThisTurn = false)

        var newShield = curr.playerShield
        var newEnemyEnergy = curr.enemyEnergy
        var newEnemyHp = curr.enemyHp
        val logs = curr.combatLog.toMutableList()

        logs.add(0, "▶ Вы разыграли [${card.name}] в Слот ${targetSlot + 1} (${card.element.displayNameRu}, ${card.currentPower}/${card.currentDefense})")

        // Deploy effects
        when (card.specialEffect) {
            CardSpecialEffect.QUANTUM_SHIELD -> {
                newShield += 4
                logs.add(0, "🛡️ Квантовый щит активирован (+4 барьера)")
                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SHIELD_BLOCK)
            }
            CardSpecialEffect.OVERCHARGE -> {
                newEnemyHp = (newEnemyHp - 3).coerceAtLeast(0)
                logs.add(0, "⚡ Перегрузка! Нанесено 3 урона ядру Демиурга")
                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
            }
            CardSpecialEffect.VOID_DRAIN -> {
                newEnemyEnergy = (newEnemyEnergy - 1).coerceAtLeast(0)
                logs.add(0, "🌑 Слив Данных: списано 1 RAM у противника")
            }
            else -> {
                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CARD_DRAW)
            }
        }

        var winner: DuelWinner? = null
        if (newEnemyHp <= 0) {
            winner = DuelWinner.PLAYER
            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.COMBAT_VICTORY)
            logs.add(0, "🏆 ПОБЕДА! Ядро ИИ разрушено!")
        }

        _state.value = curr.copy(
            playerEnergy = curr.playerEnergy - card.energyCost,
            playerHand = newHand,
            playerBoard = newBoard,
            playerShield = newShield,
            enemyHp = newEnemyHp,
            enemyEnergy = newEnemyEnergy,
            combatLog = logs.take(20),
            winner = winner
        )
        return true
    }

    fun attackWithSlot(slotIndex: Int) {
        val curr = _state.value
        if (!curr.isPlayerTurn || curr.isAiThinking || curr.winner != null) return
        val attacker = curr.playerBoard.getOrNull(slotIndex) ?: return
        if (attacker.hasAttackedThisTurn) return

        val newPlayerBoard = curr.playerBoard.toMutableList()
        val newEnemyBoard = curr.enemyBoard.toMutableList()
        var newEnemyHp = curr.enemyHp
        var newEnemyShield = curr.enemyShield
        var newPlayerHp = curr.playerHp
        val logs = curr.combatLog.toMutableList()

        val defender = newEnemyBoard[slotIndex]

        if (defender != null) {
            // Clash between cards
            val mult = attacker.element.getMultiplierAgainst(defender.element)
            val dmg = (attacker.currentPower * mult).toInt().coerceAtLeast(1)
            defender.currentDefense -= dmg

            val multText = if (mult > 1.0f) " (Крит x$mult!)" else if (mult < 1.0f) " (Слабо x$mult)" else ""
            logs.add(0, "💥 [${attacker.name}] атакует [${defender.name}] на $dmg урона$multText")

            // Counter damage if defender survived
            if (!defender.isDead) {
                val counterMult = defender.element.getMultiplierAgainst(attacker.element)
                val counterDmg = (defender.currentPower * counterMult).toInt().coerceAtLeast(1)
                attacker.currentDefense -= counterDmg
                logs.add(0, "↩ [${defender.name}] контратакует на $counterDmg урона")

                // Defender spike reflection
                if (defender.specialEffect == CardSpecialEffect.COUNTER_STRIKE) {
                    attacker.currentDefense -= 2
                    logs.add(0, "⚡ Шипы отразили 2 доп. урона!")
                }
                newEnemyBoard[slotIndex] = defender.copy(currentDefense = defender.currentDefense)
            } else {
                logs.add(0, "☠️ Вражеский аркан [${defender.name}] уничтожен!")
                newEnemyBoard[slotIndex] = null
            }

            if (attacker.isDead) {
                logs.add(0, "☠️ Ваш аркан [${attacker.name}] пал в бою!")
                newPlayerBoard[slotIndex] = null
            } else {
                newPlayerBoard[slotIndex] = attacker.copy(
                    hasAttackedThisTurn = true,
                    currentDefense = attacker.currentDefense
                )
            }

            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
        } else {
            // Direct hit to enemy core!
            val rawDmg = attacker.currentPower
            if (newEnemyShield > 0) {
                val absorbed = minOf(newEnemyShield, rawDmg)
                newEnemyShield -= absorbed
                val leftover = rawDmg - absorbed
                newEnemyHp = (newEnemyHp - leftover).coerceAtLeast(0)
                logs.add(0, "🎯 Прямой удар на $rawDmg! (Щит поглотил $absorbed)")
                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SHIELD_BLOCK)
            } else {
                newEnemyHp = (newEnemyHp - rawDmg).coerceAtLeast(0)
                logs.add(0, "🎯 Прямой удар в ядро Демиурга: -$rawDmg HP!")
                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
            }

            newPlayerBoard[slotIndex] = attacker.copy(hasAttackedThisTurn = true)
        }

        // Leech effect
        if (attacker.specialEffect == CardSpecialEffect.BIO_LEECH) {
            newPlayerHp = (newPlayerHp + 3).coerceAtMost(curr.playerMaxHp)
            logs.add(0, "🌿 Био-Дренаж восстановил +3 HP аватару")
        }

        var winner: DuelWinner? = null
        if (newEnemyHp <= 0) {
            winner = DuelWinner.PLAYER
            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.COMBAT_VICTORY)
            logs.add(0, "🏆 ПОБЕДА! Ядро Демиурга отключено!")
        } else if (newPlayerHp <= 0) {
            winner = DuelWinner.ENEMY
            logs.add(0, "💀 ПОРАЖЕНИЕ! Ваш аватар разряжен...")
        }

        _state.value = curr.copy(
            playerBoard = newPlayerBoard,
            enemyBoard = newEnemyBoard,
            enemyHp = newEnemyHp,
            enemyShield = newEnemyShield,
            playerHp = newPlayerHp,
            combatLog = logs.take(20),
            winner = winner
        )
    }

    fun endPlayerTurn(scope: CoroutineScope, onReward: (credits: Int, xp: Int) -> Unit = { _, _ -> }) {
        val curr = _state.value
        if (!curr.isPlayerTurn || curr.isAiThinking || curr.winner != null) return

        // Refresh player board attack flags
        curr.playerBoard.forEach { it?.hasAttackedThisTurn = false }

        val logs = curr.combatLog.toMutableList()
        logs.add(0, "⏳ Ход передан [Нейро-Демиургу]...")

        _state.value = curr.copy(
            isPlayerTurn = false,
            isAiThinking = true,
            combatLog = logs.take(20)
        )

        scope.launch {
            delay(700)
            executeAiTurn(onReward)
        }
    }

    private suspend fun executeAiTurn(onReward: (Int, Int) -> Unit) {
        val curr = _state.value
        if (curr.winner != null) return

        // 1. AI Energy Increment
        val nextMaxEnergy = (curr.enemyMaxEnergy + 1).coerceAtMost(10)
        var currentAiEnergy = nextMaxEnergy
        var aiHandCount = (curr.enemyHandSize + 1).coerceAtMost(6)
        val logs = curr.combatLog.toMutableList()
        val newEnemyBoard = curr.enemyBoard.map { it?.copy(hasAttackedThisTurn = false) }.toMutableList()
        val newPlayerBoard = curr.playerBoard.toMutableList()
        var newPlayerHp = curr.playerHp
        var newPlayerShield = curr.playerShield
        var newEnemyHp = curr.enemyHp
        var newEnemyShield = curr.enemyShield

        logs.add(0, "🤖 Демиург накопил $nextMaxEnergy RAM...")

        // 2. AI plays card if it has empty slot and enough RAM
        val emptySlots = newEnemyBoard.indices.filter { newEnemyBoard[it] == null }
        if (emptySlots.isNotEmpty() && enemyDrawPile.isNotEmpty() && currentAiEnergy >= 2) {
            val candidateCard = enemyDrawPile.removeAt(0)
            if (candidateCard.energyCost <= currentAiEnergy) {
                val slot = emptySlots.random()
                currentAiEnergy -= candidateCard.energyCost
                aiHandCount = (aiHandCount - 1).coerceAtLeast(0)
                newEnemyBoard[slot] = candidateCard.copy(hasAttackedThisTurn = false)
                logs.add(0, "🤖 Демиург развернул [${candidateCard.name}] в Слот ${slot + 1} (${candidateCard.element.displayNameRu})")

                if (candidateCard.specialEffect == CardSpecialEffect.QUANTUM_SHIELD) {
                    newEnemyShield += 4
                    logs.add(0, "🛡️ Демиург активировал квантовый барьер (+4)")
                }
                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CARD_DRAW)
                delay(600)
            }
        }

        // 3. AI Attacks with existing cards
        for (i in 0..2) {
            val aiCard = newEnemyBoard[i]
            if (aiCard != null && !aiCard.hasAttackedThisTurn) {
                delay(600)
                val targetPlayerCard = newPlayerBoard[i]
                if (targetPlayerCard != null) {
                    val mult = aiCard.element.getMultiplierAgainst(targetPlayerCard.element)
                    val dmg = (aiCard.currentPower * mult).toInt().coerceAtLeast(1)
                    targetPlayerCard.currentDefense -= dmg
                    logs.add(0, "💥 [${aiCard.name}] атакует ваш [${targetPlayerCard.name}] на $dmg урона")

                    if (!targetPlayerCard.isDead) {
                        val counterMult = targetPlayerCard.element.getMultiplierAgainst(aiCard.element)
                        val counterDmg = (targetPlayerCard.currentPower * counterMult).toInt().coerceAtLeast(1)
                        aiCard.currentDefense -= counterDmg
                        logs.add(0, "↩ Ваш [${targetPlayerCard.name}] контратаковал на $counterDmg урона")
                        newPlayerBoard[i] = targetPlayerCard.copy(currentDefense = targetPlayerCard.currentDefense)
                    } else {
                        logs.add(0, "☠️ Ваш аркан [${targetPlayerCard.name}] уничтожен!")
                        newPlayerBoard[i] = null
                    }

                    if (aiCard.isDead) {
                        logs.add(0, "☠️ Вражеский аркан [${aiCard.name}] уничтожен!")
                        newEnemyBoard[i] = null
                    } else {
                        newEnemyBoard[i] = aiCard.copy(hasAttackedThisTurn = true, currentDefense = aiCard.currentDefense)
                    }
                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
                } else {
                    // Direct attack on player avatar!
                    val dmg = aiCard.currentPower
                    if (newPlayerShield > 0) {
                        val absorbed = minOf(newPlayerShield, dmg)
                        newPlayerShield -= absorbed
                        val leftover = dmg - absorbed
                        newPlayerHp = (newPlayerHp - leftover).coerceAtLeast(0)
                        logs.add(0, "🚨 Демиург ударил по вам на $dmg! (Щит поглотил $absorbed)")
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SHIELD_BLOCK)
                    } else {
                        newPlayerHp = (newPlayerHp - dmg).coerceAtLeast(0)
                        logs.add(0, "🚨 Демиург нанес прямой урон: -$dmg HP!")
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
                    }
                    newEnemyBoard[i] = aiCard.copy(hasAttackedThisTurn = true)
                }
            }
        }

        // Check if player died
        var winner: DuelWinner? = null
        if (newPlayerHp <= 0) {
            winner = DuelWinner.ENEMY
            logs.add(0, "💀 Аватар отключен. Поражение в дуэли.")
        } else if (newEnemyHp <= 0) {
            winner = DuelWinner.PLAYER
            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.COMBAT_VICTORY)
            onReward(150, 50)
        }

        // 4. Return turn to player
        delay(500)
        val playerNextMaxEnergy = (curr.playerMaxEnergy + 1).coerceAtMost(10)
        val nextHand = curr.playerHand.toMutableList()
        if (playerDrawPile.isNotEmpty() && nextHand.size < 6) {
            nextHand.add(playerDrawPile.removeAt(0))
            logs.add(0, "📥 Вы получили карту из боевой колоды")
        }

        // Reset player cards
        newPlayerBoard.forEach { it?.hasAttackedThisTurn = false }

        val nextTurn = curr.turnNumber + 1
        if (winner == null) {
            logs.add(0, "▶ Ход $nextTurn. Ваша очередь действовать!")
        }

        _state.value = curr.copy(
            turnNumber = nextTurn,
            isPlayerTurn = true,
            isAiThinking = false,
            playerHp = newPlayerHp,
            playerShield = newPlayerShield,
            playerEnergy = playerNextMaxEnergy,
            playerMaxEnergy = playerNextMaxEnergy,
            playerHand = nextHand,
            playerBoard = newPlayerBoard,
            enemyHp = newEnemyHp,
            enemyShield = newEnemyShield,
            enemyEnergy = currentAiEnergy,
            enemyMaxEnergy = nextMaxEnergy,
            enemyHandSize = aiHandCount,
            enemyBoard = newEnemyBoard,
            combatLog = logs.take(20),
            winner = winner
        )
    }
}
