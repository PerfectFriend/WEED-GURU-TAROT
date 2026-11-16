package com.example.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NftMintingServiceTest {

    private lateinit var service: EvmTestnetSmartContractService

    @Before
    fun setup() {
        service = EvmTestnetSmartContractService()
    }

    @Test
    fun mintTarotNFT_unreachableRpc_returnsFailureWithErrorMessage_noFakeExplorerUrl() = runTest {
        val metadata = TarotNftMetadata(
            cardIndex = 0,
            name = "The Fool",
            rarity = "Legendary",
            mintNumber = 1,
            interpretation = "Beginnings"
        )

        // An unreachable RPC endpoint
        val unreachableRpc = "http://127.0.0.1:54321/rpc"
        val result = service.mintTarotNFT(
            ownerAddress = "0x1111111111111111111111111111111111111111",
            metadata = metadata,
            rpcUrl = unreachableRpc,
            contractAddress = "0x2222222222222222222222222222222222222222"
        )

        // Must report failure honestly
        assertFalse("Must NOT return success=true when network RPC fails", result.success)
        assertNotNull("Must provide descriptive errorMessage", result.errorMessage)
        assertEquals("Must NOT generate a fake block explorer URL that 404s", "", result.explorerUrl)
        assertEquals("TxHash must be empty on failure", "", result.txHash)
    }

    @Test
    fun mintTarotNFT_localSandboxMode_identifiesAsSandbox_withoutFakeEtherscanUrl() = runTest {
        val metadata = TarotNftMetadata(
            cardIndex = 1,
            name = "The Magician",
            rarity = "Rare",
            mintNumber = 2,
            interpretation = "Power"
        )

        // Explicit offline sandbox (blank rpcUrl)
        val result = service.mintTarotNFT(
            ownerAddress = "0x3333333333333333333333333333333333333333",
            metadata = metadata,
            rpcUrl = "",
            contractAddress = "0x4444444444444444444444444444444444444444"
        )

        assertTrue(result.success)
        assertEquals("Local Sandbox (Offline)", result.network)
        assertEquals("Must NOT generate a fake Etherscan URL for offline sandbox", "", result.explorerUrl)
        assertTrue("TxHash should exist for local sandbox tracking", result.txHash.startsWith("0x"))
    }

    @Test
    fun mintTarotNFT_reachableRpcWithoutSigner_returnsHonestReadOnlyFailure() = runTest {
        val metadata = TarotNftMetadata(
            cardIndex = 3,
            name = "The Empress",
            rarity = "Epic",
            mintNumber = 7,
            interpretation = "Abundance"
        )

        val serverSocket = java.net.ServerSocket(0)
        val port = serverSocket.localPort
        val serverThread = Thread {
            try {
                serverSocket.accept().use { client ->
                    val reader = client.getInputStream().bufferedReader()
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isEmpty()) break
                    }
                    val body = """{"jsonrpc":"2.0","id":1,"result":"0x1234"}"""
                    val response = buildString {
                        append("HTTP/1.1 200 OK\r\n")
                        append("Content-Type: application/json\r\n")
                        append("Content-Length: ${body.toByteArray().size}\r\n")
                        append("Connection: close\r\n\r\n")
                        append(body)
                    }
                    client.getOutputStream().write(response.toByteArray())
                    client.getOutputStream().flush()
                }
            } catch (_: Exception) {
            }
        }
        serverThread.start()

        try {
            val result = service.mintTarotNFT(
                ownerAddress = "0x5555555555555555555555555555555555555555",
                metadata = metadata,
                rpcUrl = "http://127.0.0.1:$port/rpc",
                contractAddress = "0x6666666666666666666666666666666666666666"
            )

            assertFalse("Read-only RPC without signer must return success=false", result.success)
            assertEquals(0x1234L, result.blockNumber)
            assertEquals("", result.txHash)
            assertEquals("", result.explorerUrl)
            assertTrue(result.isSimulated)
            assertNotNull(result.errorMessage)
            assertTrue(
                "Error message must explain disabled on-chain minting / missing signer",
                result.errorMessage!!.contains("signer", ignoreCase = true)
            )
        } finally {
            runCatching { serverSocket.close() }
            serverThread.join(1000)
        }
    }
}

