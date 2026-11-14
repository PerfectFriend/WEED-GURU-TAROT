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
}
