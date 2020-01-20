package com.fridaytech.etomicswapkit

import com.fridaytech.etomicswapkit.contracts.HtlcContract
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

class EtomicSwapKit private constructor(
    val htlcContract: HtlcContract
) {

    companion object {
        fun newInstance(
            privateKey: BigInteger,
            rpcProviderMode: RpcProviderMode,
            network: NetworkType,
            gasPriceProvider: ContractGasProvider
        ) : EtomicSwapKit {
            val credentials = Credentials.create(ECKeyPair.create(privateKey))

            val providerUrl = when(rpcProviderMode) {
                is RpcProviderMode.Infura -> network.getInfuraUrl(rpcProviderMode.projectId)
                is RpcProviderMode.Node -> rpcProviderMode.nodeUrl
            }

            val htlcContract = HtlcContract(network.htlcAddress, credentials, gasPriceProvider, providerUrl)

            return EtomicSwapKit(htlcContract)
        }
    }

    sealed class RpcProviderMode {
        class Infura(
            val projectId: String,
            val projectSecret: String
        ) : RpcProviderMode()
        class Node(val nodeUrl: String) : RpcProviderMode()
    }

    enum class NetworkType(
        val id: Int,
        val htlcAddress: String,
        val htlcErc20Address: String,
        private val subdomain: String
    ) {
        MainNet(
            1,
            "",
            "",
            "mainnet"
        ),
        Ropsten(
            3,
            "0x6e4dD9137f8473bAD0F2a09eF44F3b31d3C86294",
            "0x1aa2062b9Eb3E30c82aB699FdF881ee33A2b50b0",
            "ropsten"
        ),
        Kovan(
            42,
            "0x04CDeE2139355Af2DfE6786978624b87eACa85F9",
            "0xC7B0f672698eeF6Bc5DCC4C97DF09f4069eed955",
            "kovan"
        );

        fun getInfuraUrl(projectId: String): String {
            return "https://$subdomain.infura.io/v3/$projectId"
        }

        fun getInfuraSocketUrl(projectId: String): String {
            return "wss://$subdomain.infura.io/ws/v3/$projectId"
        }
    }

}