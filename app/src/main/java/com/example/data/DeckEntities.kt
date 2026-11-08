package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "saved_decks")
data class SavedDeck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String = "⚔️",
    val cardIndicesString: String,
    val isActive: Boolean = false,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val cardIndices: List<Int>
        get() = if (cardIndicesString.isBlank()) emptyList()
        else cardIndicesString.split(",").mapNotNull { it.trim().toIntOrNull() }
}

@Immutable
@Entity(tableName = "card_upgrades")
data class CardUpgrade(
    @PrimaryKey val cardIndex: Int,
    val upgradeLevel: Int = 1,
    val bonusPower: Int = 0,
    val bonusDefense: Int = 0,
    val energyDiscount: Int = 0,
    val customSkin: String = "DEFAULT"
)
