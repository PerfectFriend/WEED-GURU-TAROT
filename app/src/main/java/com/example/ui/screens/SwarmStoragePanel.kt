package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProfile
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import java.util.Locale

@Composable
fun SwarmStoragePanel(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val allocatedSize by viewModel.swarmContainerAllocatedSize.collectAsStateWithLifecycle()
    val isMounted by viewModel.swarmContainerMounted.collectAsStateWithLifecycle()
    val nodeRole by viewModel.swarmNodeRole.collectAsStateWithLifecycle()
    val nodeActive by viewModel.swarmNodeActive.collectAsStateWithLifecycle()
    val peers by viewModel.swarmDiscoveredPeers.collectAsStateWithLifecycle()
    val logs by viewModel.swarmNodeLogs.collectAsStateWithLifecycle()
    val isAllocating by viewModel.isSwarmAllocating.collectAsStateWithLifecycle()
    val allocationProgress by viewModel.swarmAllocationProgress.collectAsStateWithLifecycle()

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isSpeedTesting by viewModel.isSpeedTesting.collectAsStateWithLifecycle()
    val leaderboard by viewModel.globalNodeLeaderboard.collectAsStateWithLifecycle()

    var passphraseInput by remember { mutableStateOf("") }
    var sizeSelectionGb by remember { mutableStateOf(16f) }
    var selectedShardIndex by remember { mutableStateOf(0) }
    var shardPayloadInput by remember { mutableStateOf("Netrunner Encrypted Payload Block") }
    var decryptedShardOutput by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(bottom = Space.xxl)
    ) {
        item {
            CyberPanel(
                borderColor = currentSkin.primaryColor.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "SECURE SPACE PROVISIONING & PHYSICAL ALLOCATION",
                    subtitle = "Allocates a physical encrypted storage volume on your local device storage.",
                    color = currentSkin.primaryColor
                )
                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                        .padding(Space.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS:",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.textMid
                    )
                    Text(
                        text = if (allocatedSize > 0) {
                            "${String.format(Locale.US, "%.2f", allocatedSize / (1024 * 1024 * 1024.0))} GB RESERVED"
                        } else {
                            "NO ACTIVE ALLOCATION"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = if (allocatedSize > 0) c.emeraldGlow else c.danger
                    )
                }

                if (allocatedSize == 0L) {
                    Spacer(modifier = Modifier.height(Space.md))

                    Text(
                        text = "CONTAINER MASTER PASSPHRASE (PBKDF2-HMAC-SHA256, 210,000 iterations)",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.xs))
                    OutlinedTextField(
                        value = passphraseInput,
                        onValueChange = { passphraseInput = it },
                        placeholder = { Text("Enter strong node security key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PROVISIONED CAPACITY:",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Text(
                            text = "${sizeSelectionGb.toInt()} GB",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = currentSkin.primaryColor
                        )
                    }
                    Slider(
                        value = sizeSelectionGb,
                        onValueChange = { sizeSelectionGb = it },
                        valueRange = 1f..32f,
                        steps = 31,
                        colors = SliderDefaults.colors(
                            thumbColor = currentSkin.primaryColor,
                            activeTrackColor = currentSkin.primaryColor,
                            inactiveTrackColor = c.panelRaised
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Space.md))

                    if (isAllocating) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { allocationProgress },
                                color = currentSkin.primaryColor,
                                trackColor = c.panelRaised,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = "Reserving Disk Blocks... ${String.format(Locale.US, "%.0f", allocationProgress * 100)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        CyberActionButton(
                            text = "PRE-ALLOCATE & ENCRYPT CRYPTO-VOLUME",
                            onClick = {
                                if (passphraseInput.isNotBlank()) {
                                    val bytes = sizeSelectionGb.toLong() * 1024 * 1024 * 1024L
                                    viewModel.allocateSwarmContainer(bytes, passphraseInput)
                                } else {
                                    viewModel.postNotification("Please enter a secure passphrase before generating container.")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(Space.md))
                    CyberTextButton(
                        onClick = { viewModel.deallocateSwarmContainer() },
                        text = "PURGE & SECURE-ERASE CONTAINER FROM DISK",
                        skin = currentSkin,
                        containerColor = c.danger,
                        contentColor = c.textHigh,
                        shape = CyberShapes.control,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (allocatedSize > 0) {
            item {
                CyberPanel(
                    borderColor = if (isMounted) c.emeraldGlow.copy(alpha = 0.45f) else c.goldAntique.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CyberSectionHeader(
                        title = "SECURE VOLUME DECRYPTION & MOUNT ENGINE",
                        color = if (isMounted) c.emeraldGlow else c.textHigh
                    )
                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.ink, CyberShapes.chip)
                            .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                            .padding(Space.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isMounted) c.emeraldGlow else c.danger, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(Space.sm))
                            Text(
                                text = "MOUNT STATUS:",
                                style = MaterialTheme.typography.labelLarge,
                                color = c.textMid
                            )
                        }
                        Text(
                            text = if (isMounted) "RAM MOUNTED (UNLOCKED)" else "LOCKED (ENCRYPTED)",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = if (isMounted) c.emeraldGlow else c.danger
                        )
                    }

                    Spacer(modifier = Modifier.height(Space.md))

                    if (!isMounted) {
                        Text(
                            text = "ENTER MASTER PASSPHRASE TO MOUNT VOLUME:",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        OutlinedTextField(
                            value = passphraseInput,
                            onValueChange = { passphraseInput = it },
                            placeholder = { Text("Verify passphrase") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(Space.md))
                        CyberActionButton(
                            text = "UNLOCK & MOUNT PARTITION",
                            onClick = {
                                if (passphraseInput.isNotBlank()) {
                                    viewModel.mountSwarmContainer(passphraseInput)
                                    passphraseInput = ""
                                } else {
                                    viewModel.postNotification("Passphrase is required to unlock container.")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        CyberActionButton(
                            text = "UNMOUNT & PURGE VOLATILE KEYS FROM RAM",
                            onClick = { viewModel.unmountSwarmContainer() },
                            secondary = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (isMounted) {
            item {
                CyberPanel(
                    borderColor = currentSkin.secondaryColor.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CyberSectionHeader(
                        title = "SECURE BLOCK SHARD TESTING MATRIX",
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(Space.md))

                    Text(
                        text = "SELECT VIRTUAL SHARD INDEX:",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        (0..4).forEach { idx ->
                            CyberChip(
                                label = "#$idx",
                                selected = selectedShardIndex == idx,
                                onClick = { selectedShardIndex = idx },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.md))

                    Text(
                        text = "SHARD DATA CARGO:",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.xs))
                    OutlinedTextField(
                        value = shardPayloadInput,
                        onValueChange = { shardPayloadInput = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        CyberActionButton(
                            text = "WRITE SHARD",
                            onClick = { viewModel.writeSwarmShardBlock(selectedShardIndex, shardPayloadInput) },
                            modifier = Modifier.weight(1f)
                        )
                        CyberActionButton(
                            text = "READ SHARD",
                            onClick = {
                                viewModel.readSwarmShardBlock(selectedShardIndex) { result ->
                                    decryptedShardOutput = result ?: "EMPTY BLOCK"
                                }
                            },
                            secondary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    decryptedShardOutput?.let { output ->
                        Spacer(modifier = Modifier.height(Space.md))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                .padding(Space.md)
                        ) {
                            Column {
                                Text(
                                    text = "DECRYPTED SHARD #$selectedShardIndex:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.textMid
                                )
                                Spacer(modifier = Modifier.height(Space.xs))
                                Text(
                                    text = output,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    color = c.emeraldGlow
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            val userP = profile ?: UserProfile()
            CyberPanel(
                borderColor = currentSkin.primaryColor.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "SUB-ZERO NODE STAKING & UPTIME INSURANCE",
                    subtitle = "Lock WEED tokens in node escrow to unlock advanced server roles and priority ranking.",
                    color = currentSkin.primaryColor
                )
                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.ink),
                        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.3f)),
                        shape = CyberShapes.chip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(Space.md), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WALLET BALANCE", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text("${userP.weed} WEED", style = MaterialTheme.typography.titleSmall, fontFamily = IbmPlexMonoFontFamily, color = c.textHigh)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.ink),
                        border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                        shape = CyberShapes.chip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(Space.md), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STAKED NODE ESCROW", style = MaterialTheme.typography.labelMedium, color = currentSkin.primaryColor)
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text("${userP.nodeStake} WEED", style = MaterialTheme.typography.titleSmall, fontFamily = IbmPlexMonoFontFamily, color = c.emeraldGlow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md)
                ) {
                    Text(
                        text = "PRIVILEGE ESCROW LEVELS:",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.xs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("STORAGE: 100 WEED", style = MaterialTheme.typography.labelMedium, color = if (userP.nodeStake >= 100) c.emeraldGlow else c.textMid)
                        Text("RELAY: 250 WEED", style = MaterialTheme.typography.labelMedium, color = if (userP.nodeStake >= 250) c.emeraldGlow else c.textMid)
                    }
                    Spacer(modifier = Modifier.height(Space.xs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VALIDATOR: 500 WEED", style = MaterialTheme.typography.labelMedium, color = if (userP.nodeStake >= 500) c.emeraldGlow else c.textMid)
                        Text("ORACLE: 1000 WEED", style = MaterialTheme.typography.labelMedium, color = if (userP.nodeStake >= 1000) c.emeraldGlow else c.textMid)
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                var stakeInputText by remember { mutableStateOf("100") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    OutlinedTextField(
                        value = stakeInputText,
                        onValueChange = { stakeInputText = it },
                        label = { Text("Weed Amount") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                        modifier = Modifier.weight(1f)
                    )
                    CyberActionButton(
                        text = "STAKE",
                        onClick = {
                            val amt = stakeInputText.toIntOrNull() ?: 0
                            if (amt > 0) viewModel.stakeSwarmWeed(amt)
                        }
                    )
                    CyberActionButton(
                        text = "UNSTAKE",
                        onClick = {
                            val amt = stakeInputText.toIntOrNull() ?: 0
                            if (amt > 0) viewModel.unstakeSwarmWeed(amt)
                        },
                        secondary = true
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("NODE LIVE UPTIME", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text("${userP.nodeUptimeMinutes} mins", style = MaterialTheme.typography.titleSmall, fontFamily = IbmPlexMonoFontFamily, color = c.emeraldGlow)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("OFFLINE PENALTY ENGINE", style = MaterialTheme.typography.labelMedium, color = c.danger)
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text("50% Slashed if Offline >15m", style = MaterialTheme.typography.bodySmall, color = c.textMid)
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                CyberTextButton(
                    onClick = { viewModel.simulateFifteenMinutesOffline() },
                    text = "SIMULATE 15-MINUTE OUTAGE (PENALTY SLASH TEST)",
                    skin = currentSkin,
                    containerColor = c.danger.copy(alpha = 0.16f),
                    contentColor = c.danger,
                    shape = CyberShapes.control,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            val userP = profile ?: UserProfile()
            CyberPanel(
                borderColor = if (userP.nodeBandwidthMbps >= 3.0) NeonCyan.copy(alpha = 0.45f) else c.danger.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "DECENTRALIZED TRANSIT CHANNEL BANDWIDTH",
                    subtitle = "Minimum 3.0 Mbps required to assist global routing maps.",
                    color = if (userP.nodeBandwidthMbps >= 3.0) NeonCyan else c.danger
                )
                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MEASURED TRANSIT SPEED", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text(
                            text = "${String.format(Locale.US, "%.2f", userP.nodeBandwidthMbps)} Mbps",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = if (userP.nodeBandwidthMbps >= 3.0) c.emeraldGlow else c.danger
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ROUTING PROTOCOL STATE", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text(
                            text = if (userP.nodeBandwidthMbps >= 3.0) "ELIGIBLE" else "LOCAL ONLY",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (userP.nodeBandwidthMbps >= 3.0) c.emeraldGlow else NeonAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                if (isSpeedTesting) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(Space.sm))
                        Text("Testing network transit speed... Please stand by.", style = MaterialTheme.typography.bodySmall, color = c.textMid)
                    }
                } else {
                    CyberActionButton(
                        text = "RUN CHANNEL BANDWIDTH SPEED TEST",
                        onClick = { viewModel.runBandwidthSpeedTest() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            CyberPanel(
                borderColor = if (nodeActive) NeonCyan.copy(alpha = 0.45f) else c.goldAntique.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECENTRAL SWARM P2P NETWORK COORDINATOR",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (nodeActive) NeonCyan else c.textHigh,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (nodeActive) NeonCyan else c.danger, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = "Configures local discovery, routing, and transit protocols via UDP local socket broadcasts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "SELECT SWARM OPERATIONAL MODE / ROLE LEVEL:",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xs))

                val roles = listOf("Storage", "Relay", "Validator", "Oracle")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    roles.forEach { r ->
                        CyberChip(
                            label = r.uppercase(),
                            selected = nodeRole == r,
                            onClick = { viewModel.setSwarmNodeRole(r) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SWARM NODE PROTOCOL:",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text(
                            text = if (nodeActive) "UDP BROADCASTER ONLINE" else "OFFLINE",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (nodeActive) NeonCyan else c.danger
                        )
                    }
                    Switch(
                        checked = nodeActive,
                        onCheckedChange = { active ->
                            if (active) viewModel.startSwarmNode() else viewModel.stopSwarmNode()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = c.textMid,
                            uncheckedTrackColor = c.ink
                        )
                    )
                }

                if (nodeActive) {
                    Spacer(modifier = Modifier.height(Space.md))
                    Text(
                        text = "DISCOVERED SWARM NEIGHBORS (PEERS ON SUBNET):",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.xs))

                    if (peers.isEmpty()) {
                        Text(
                            text = "Scanning local subnet... UDP Port 9944 broadcast active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            modifier = Modifier.padding(vertical = Space.xs)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                            peers.forEach { peer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(c.panelRaised, CyberShapes.chip)
                                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                                        .padding(Space.md),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${peer.id} (${peer.ip})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontFamily = IbmPlexMonoFontFamily,
                                            color = c.textHigh
                                        )
                                        Spacer(modifier = Modifier.height(Space.xxs))
                                        Text(
                                            text = "Role: ${peer.role} | Cap: ${peer.allocatedGb} GB",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = c.textMid
                                        )
                                        Spacer(modifier = Modifier.height(Space.sm))
                                        CyberActionButton(
                                            text = "STREAM SHARD #$selectedShardIndex",
                                            onClick = {
                                                viewModel.fetchSwarmShardBlockFromPeer(peer.ip, selectedShardIndex) { result ->
                                                    decryptedShardOutput = result ?: "EMPTY CHUNK"
                                                }
                                            }
                                        )
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = c.ink),
                                        border = BorderStroke(1.dp, c.emeraldGlow.copy(alpha = 0.3f)),
                                        shape = CyberShapes.chip
                                    ) {
                                        Text(
                                            text = peer.status,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = c.emeraldGlow,
                                            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))
                Text(
                    text = "NODE REAL-TIME SIGNAL STREAM:",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (log.contains("Critical") || log.contains("Error")) c.danger else if (log.contains("SWARM")) NeonCyan else c.textMid
                            )
                        }
                    }
                }
            }
        }

        item {
            CyberPanel(
                borderColor = c.goldAntique.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "GLOBAL DECENTRALIZED CLUSTER RANKINGS",
                    subtitle = "Rank index determined by locked WEED escrow stake, uptime, and bandwidth.",
                    color = c.textHigh
                )
                Spacer(modifier = Modifier.height(Space.md))

                Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    leaderboard.forEachIndexed { idx, rankNode ->
                        val isUser = rankNode.isUser
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isUser) NeonCyan.copy(alpha = 0.08f) else c.panelRaised,
                                    CyberShapes.chip
                                )
                                .border(
                                    1.dp,
                                    if (isUser) NeonCyan else c.goldAntique.copy(alpha = 0.25f),
                                    CyberShapes.chip
                                )
                                .padding(Space.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    color = if (isUser) NeonCyan else c.textMid,
                                    modifier = Modifier.width(34.dp)
                                )
                                Column {
                                    Text(
                                        text = rankNode.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isUser) NeonCyan else c.textHigh
                                    )
                                    Spacer(modifier = Modifier.height(Space.xxs))
                                    Text(
                                        text = "Role: ${rankNode.role} | Bandwidth: ${String.format(Locale.US, "%.1f", rankNode.speedMbps)} Mbps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMid
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${rankNode.stake} STAKED",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    color = c.emeraldGlow
                                )
                                Spacer(modifier = Modifier.height(Space.xxs))
                                Text(
                                    text = rankNode.status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (rankNode.status == "ONLINE") c.emeraldGlow else if (rankNode.status == "LOW_SPEED") NeonAmber else c.danger
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
