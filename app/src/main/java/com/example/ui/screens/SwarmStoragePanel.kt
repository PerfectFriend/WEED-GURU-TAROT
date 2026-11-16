package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProfile
import com.example.ui.theme.CyberCharcoal
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.viewmodel.TarotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwarmStoragePanel(viewModel: TarotViewModel) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. PHYSICAL STORAGE ALLOCATION CONSOLE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SECURE SPACE PROVISIONING & PHYSICAL ALLOCATION",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This allocates a physical encrypted storage volume (container file) using instant space reservation. The file resides on your local device storage, operating on a torrent-like scheme for maximum autonomy and peer resilience.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS:",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Text(
                            text = if (allocatedSize > 0) {
                                "${String.format("%.2f", allocatedSize / (1024 * 1024 * 1024.0))} GB RESERVED"
                            } else {
                                "NO ACTIVE ALLOCATION"
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allocatedSize > 0) NeonGreen else Color.Red
                        )
                    }

                    if (allocatedSize == 0L) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CONTAINER MASTER PASSPHRASE (derived via PBKDF2-HMAC-SHA256, 210,000 iterations)",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = passphraseInput,
                            onValueChange = { passphraseInput = it },
                            placeholder = { Text("Enter strong node security key", fontSize = 16.sp, color = Color.DarkGray) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PROVISIONED CAPACITY:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${sizeSelectionGb.toInt()} GB",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
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
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isAllocating) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { allocationProgress },
                                    color = currentSkin.primaryColor,
                                    trackColor = Color.DarkGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Reserving Disk Blocks... ${String.format("%.0f", allocationProgress * 100)}%",
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            CyberButton(
                                onClick = {
                                    if (passphraseInput.isNotBlank()) {
                                        val bytes = sizeSelectionGb.toLong() * 1024 * 1024 * 1024L
                                        viewModel.allocateSwarmContainer(bytes, passphraseInput)
                                    } else {
                                        viewModel.postNotification("⚠️ Please enter a secure passphrase before generating container.")
                                    }
                                },
                                skin = currentSkin,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "PRE-ALLOCATE & ENCRYPT CRYPTO-VOLUME",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        CyberButton(
                            onClick = { viewModel.deallocateSwarmContainer() },
                            skin = currentSkin,
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AutoResizingText(
                                text = "PURGE & SECURE-ERASE CONTAINER FROM DISK",
                                targetTextSize = 11.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 2. CRYPTOGRAPHIC MOUNT & DECRYPTION VAULT
        if (allocatedSize > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, if (isMounted) NeonGreen.copy(alpha = 0.4f) else Color.DarkGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECURE VOLUME DECRYPTION & MOUNT ENGINE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMounted) NeonGreen else Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isMounted) NeonGreen else Color.Red, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MOUNT STATUS:",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                            Text(
                                text = if (isMounted) "RAM MOUNTED (UNLOCKED)" else "LOCKED (ENCRYPTED)",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMounted) NeonGreen else Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isMounted) {
                            Text(
                                text = "ENTER MASTER PASSPHRASE TO MOUNT VOLUME:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = passphraseInput,
                                onValueChange = { passphraseInput = it },
                                placeholder = { Text("Verify passphrase", fontSize = 16.sp, color = Color.DarkGray) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CyberButton(
                                onClick = {
                                    if (passphraseInput.isNotBlank()) {
                                        viewModel.mountSwarmContainer(passphraseInput)
                                        passphraseInput = ""
                                    } else {
                                        viewModel.postNotification("⚠️ Passphrase is required to unlock container.")
                                    }
                                },
                                skin = currentSkin,
                                containerColor = NeonGreen,
                                contentColor = CyberObsidian,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "UNLOCK & MOUNT PARTITION",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            CyberButton(
                                onClick = { viewModel.unmountSwarmContainer() },
                                skin = currentSkin,
                                containerColor = Color.DarkGray,
                                contentColor = Color.White,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "UNMOUNT & PURGE VOLATILE KEYS FROM RAM",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. SECURE BLOCK SHARD WRITER & AUDITOR
        if (isMounted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, currentSkin.secondaryColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECURE BLOCK SHARD TESTING MATRIX",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentSkin.primaryColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECT VIRTUAL SHARD INDEX:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..4).forEach { idx ->
                                    val isSel = selectedShardIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSel) currentSkin.secondaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(1.dp, if (isSel) currentSkin.primaryColor else Color.DarkGray, RoundedCornerShape(4.dp))
                                            .clickable { selectedShardIndex = idx }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "#$idx",
                                            fontSize = 16.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = if (isSel) currentSkin.primaryColor else Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SHARD DATA CARGO:",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = shardPayloadInput,
                            onValueChange = { shardPayloadInput = it },
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberButton(
                                onClick = { viewModel.writeSwarmShardBlock(selectedShardIndex, shardPayloadInput) },
                                skin = currentSkin,
                                containerColor = currentSkin.secondaryColor,
                                contentColor = CyberObsidian,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "WRITE SHARD",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            CyberButton(
                                onClick = {
                                    viewModel.readSwarmShardBlock(selectedShardIndex) { result ->
                                        decryptedShardOutput = result ?: "EMPTY BLOCK"
                                    }
                                },
                                skin = currentSkin,
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "READ SHARD",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        decryptedShardOutput?.let { output ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "DECRYPTED SHARD #$selectedShardIndex:",
                                        fontSize = 15.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color(0xFFB0B0C5)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = output,
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3a. SUB-ZERO NODE STAKING & UPTIME MONITOR
        item {
            val userP = profile ?: UserProfile()
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUB-ZERO NODE STAKING & UPTIME INSURANCE",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Operating a node is a critical network privilege. It requires locking (freezing) WEED tokens in a dedicated node escrow. Higher stake levels unlock advanced server roles and boost your global network priority ranking.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WALLET BALANCE", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${userP.weed} WEED", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = Color.White)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STAKED NODE ESCROW", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = currentSkin.primaryColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${userP.nodeStake} WEED", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = NeonGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF07070F), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "PRIVILEGE ESCROW LEVELS:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("STORAGE: 100 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 100) NeonGreen else Color.LightGray)
                            Text("RELAY: 250 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 250) NeonGreen else Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VALIDATOR: 500 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 500) NeonGreen else Color.LightGray)
                            Text("ORACLE: 1000 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 1000) NeonGreen else Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    var stakeInputText by remember { mutableStateOf("100") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stakeInputText,
                            onValueChange = { stakeInputText = it },
                            label = { Text("Weed Amount", fontSize = 16.sp, color = Color(0xFFB0B0C5)) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        CyberButton(
                            onClick = {
                                val amt = stakeInputText.toIntOrNull() ?: 0
                                if (amt > 0) viewModel.stakeSwarmWeed(amt)
                            },
                            skin = currentSkin,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            AutoResizingText("STAKE", targetTextSize = 11.sp, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                        CyberButton(
                            onClick = {
                                val amt = stakeInputText.toIntOrNull() ?: 0
                                if (amt > 0) viewModel.unstakeSwarmWeed(amt)
                            },
                            skin = currentSkin,
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            AutoResizingText("UNSTAKE", targetTextSize = 11.sp, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NODE LIVE UPTIME", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${userP.nodeUptimeMinutes} mins", fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = NeonGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("OFFLINE PENALTY ENGINE", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color.Red)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("50% Slashed if Offline >15m", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CyberButton(
                        onClick = { viewModel.simulateFifteenMinutesOffline() },
                        skin = currentSkin,
                        containerColor = Color(0xFF3A1010),
                        contentColor = Color.Red,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        AutoResizingText("SIMULATE 15-MINUTE OUTAGE (PENALTY SLASH TEST)", targetTextSize = 10.sp, fontFamily = RajdhaniFontFamily, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // 3b. CHANNEL BANDWIDTH SPEED TESTER
        item {
            val userP = profile ?: UserProfile()
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, if (userP.nodeBandwidthMbps >= 3.0) NeonCyan.copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DECENTRALIZED TRANSIT CHANNEL BANDWIDTH",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (userP.nodeBandwidthMbps >= 3.0) NeonCyan else Color.Red
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nodes must maintain a minimum transit channel speed of 3.0 Mbps to assist global routing maps. Nodes falling below this threshold are temporarily isolated from public rating leaderboards, remaining reserved exclusively for private personal projects.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MEASURED TRANSIT SPEED", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${String.format("%.2f", userP.nodeBandwidthMbps)} Mbps", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = if (userP.nodeBandwidthMbps >= 3.0) NeonGreen else Color.Red)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ROUTING PROTOCOL STATE", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (userP.nodeBandwidthMbps >= 3.0) "🟢 ELIGIBLE" else "⚠️ LOCAL ONLY",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = if (userP.nodeBandwidthMbps >= 3.0) NeonGreen else Color.Yellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isSpeedTesting) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Testing network transit speed... Please stand by.", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = Color.LightGray)
                        }
                    } else {
                        CyberButton(
                            onClick = { viewModel.runBandwidthSpeedTest() },
                            skin = currentSkin,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            AutoResizingText("RUN CHANNEL BANDWIDTH SPEED TEST", targetTextSize = 11.sp, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // 4. ZERO-TRACKER DECENTRALIZED P2P NETWORK CORE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, if (nodeActive) NeonCyan.copy(alpha = 0.4f) else Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DECENTRAL SWARM P2P NETWORK COORDINATOR",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (nodeActive) NeonCyan else Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (nodeActive) NeonCyan else Color.Red, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Configures local discovery, routing, and transit protocols. Zero external tracking. Uses UDP local socket broadcasts to exchange network maps directly between devices.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SELECT SWARM OPERATIONAL MODE / ROLE LEVEL:",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val roles = listOf("Storage", "Relay", "Validator", "Oracle")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        roles.forEach { r ->
                            val isSel = nodeRole == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSel) NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, if (isSel) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setSwarmNodeRole(r) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = r.uppercase(),
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isSel) NeonCyan else Color(0xFFB0B0C5),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SWARM NODE PROTOCOL:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (nodeActive) "UDP BROADCASTER ONLINE" else "OFFLINE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nodeActive) NeonCyan else Color.Red
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
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Black
                            )
                        )
                    }

                    if (nodeActive) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "DISCOVERED SWARM NEIGHBORS (PEERS ON SUBNET):",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (peers.isEmpty()) {
                            Text(
                                text = "Scanning local subnet... UDP Port 9944 broadcast active. Ensure other nodes are running on the network.",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                peers.forEach { peer ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${peer.id} (${peer.ip})",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Role: ${peer.role} | Cap: ${peer.allocatedGb} GB",
                                                fontSize = 15.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color(0xFFB0B0C5)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            CyberButton(
                                                onClick = {
                                                    viewModel.fetchSwarmShardBlockFromPeer(peer.ip, selectedShardIndex) { result ->
                                                        decryptedShardOutput = result ?: "EMPTY CHUNK"
                                                    }
                                                },
                                                skin = currentSkin,
                                                containerColor = Color.Black,
                                                contentColor = currentSkin.primaryColor,
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                AutoResizingText(
                                                    text = "STREAM SHARD #$selectedShardIndex",
                                                    targetTextSize = 9.sp,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = peer.status,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = RajdhaniFontFamily,
                                                color = NeonGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "NODE REAL-TIME SIGNAL STREAM:",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 15.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (log.contains("Critical") || log.contains("Error")) Color.Red else if (log.contains("📡") || log.contains("SWARM")) NeonCyan else Color.LightGray,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4a. GLOBAL DECENTRALIZED NODE LEADERBOARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GLOBAL DECENTRALIZED CLUSTER RANKINGS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time ranking of active nodes on the Sub-Zero swarm. Rank index is determined primarily by locked WEED escrow stake, rewarding high-fidelity uptime and commitment to the collective storage grid.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        leaderboard.forEachIndexed { idx, rankNode ->
                            val isUser = rankNode.isUser
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isUser) NeonCyan.copy(alpha = 0.08f) else Color(0xFF0A0A0F),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isUser) NeonCyan else Color.DarkGray,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${idx + 1}",
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) NeonCyan else Color(0xFFB0B0C5),
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = rankNode.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RajdhaniFontFamily,
                                            color = if (isUser) NeonCyan else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Role: ${rankNode.role} | Bandwidth: ${String.format("%.1f", rankNode.speedMbps)} Mbps",
                                            fontSize = 15.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${rankNode.stake} STAKED",
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rankNode.status,
                                        fontSize = 15.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = if (rankNode.status == "ONLINE") NeonGreen else if (rankNode.status == "LOW_SPEED") Color.Yellow else Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
