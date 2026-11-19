package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TarotRepository
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel

@Composable
fun CardSynthesisView(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    val unlistedOwned = remember(mintedCards) { mintedCards.filter { !it.isListed } }

    val commonCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Common", ignoreCase = true) } }
    val rareCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Rare", ignoreCase = true) } }
    val epicCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Epic", ignoreCase = true) } }

    var isFusing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.sm, vertical = Space.md),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CyberPanel(
                borderColor = c.goldLeaf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = c.goldLeaf,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "SYNTHESIS MATRIX / СИНТЕЗ КАРТ",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.goldLeaf
                        )
                    }
                    Spacer(modifier = Modifier.height(Space.xs))
                    Text(
                        text = "Fuse lower rarity cards into higher tiers according to the Genesis Formula. 10 Epic cards fuse into 1 Gold Genesis Series card ('Начало Начал').",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            SynthesisRecipeCard(
                title = "COMMON -> RARE",
                subtitle = "4 Common Cards -> 1 Rare NFT Card",
                ownedCount = commonCards.size,
                requiredCount = 4,
                targetColor = NeonCyan,
                buttonText = "FUSE 4 COMMONS -> 1 RARE",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.COMMON_TO_RARE) {
                        isFusing = false
                    }
                }
            )
        }

        item {
            SynthesisRecipeCard(
                title = "RARE -> EPIC",
                subtitle = "4 Rare Cards -> 1 Epic NFT Card",
                ownedCount = rareCards.size,
                requiredCount = 4,
                targetColor = NeonMagenta,
                buttonText = "FUSE 4 RARES -> 1 EPIC",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.RARE_TO_EPIC) {
                        isFusing = false
                    }
                }
            )
        }

        item {
            SynthesisRecipeCard(
                title = "EPIC -> LEGEND",
                subtitle = "4 Epic Cards -> 1 Legend NFT Card",
                ownedCount = epicCards.size,
                requiredCount = 4,
                targetColor = NeonGold,
                buttonText = "FUSE 4 EPICS -> 1 LEGEND",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.EPIC_TO_LEGEND) {
                        isFusing = false
                    }
                }
            )
        }

        item {
            SynthesisRecipeCard(
                title = "EPIC -> GENESIS GOLD",
                subtitle = "10 Epic Cards -> 1 Genesis Gold Card (#22 Electron, #23 Proton, #24 Hydrogen...)",
                ownedCount = epicCards.size,
                requiredCount = 10,
                targetColor = NeonGold,
                buttonText = "FUSE 10 EPICS -> 1 GENESIS GOLD",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.EPIC_TO_GENESIS) {
                        isFusing = false
                    }
                }
            )
        }
    }
}

@Composable
fun SynthesisRecipeCard(
    title: String,
    subtitle: String,
    ownedCount: Int,
    requiredCount: Int,
    targetColor: Color,
    buttonText: String,
    isFusing: Boolean,
    onFuse: () -> Unit
) {
    val c = Cyber.colors
    val canFuse = ownedCount >= requiredCount && !isFusing

    CyberPanel(
        borderColor = if (canFuse) targetColor else c.goldAntique.copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = targetColor
                )
                Text(
                    text = "AVAILABLE: $ownedCount / $requiredCount",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                    color = if (canFuse) NeonGreen else c.textMid
                )
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid
            )

            CyberNeonButton(
                text = if (isFusing) "SYNTHESIZING..." else buttonText,
                onClick = onFuse,
                enabled = canFuse,
                color = targetColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
