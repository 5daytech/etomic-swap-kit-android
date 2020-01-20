package com.fridaytech.demo.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fridaytech.demo.App
import com.fridaytech.etomicswapkit.contracts.HtlcContract
import com.fridaytech.demo.core.EthereumAdapter
import com.fridaytech.etomicswapkit.models.Secret
import com.fridaytech.etomicswapkit.models.SwapRequest
import com.fridaytech.etomicswapkit.models.SwapResponse
import com.fridaytech.demo.utils.*
import com.fridaytech.etomicswapkit.EtomicSwapKit
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

class MainViewModel : ViewModel() {

    private val infuraCredentials = EthereumKit.InfuraCredentials(
        projectId = "0c3f9e6a005b40c58235da423f58b198",
        secretKey = "57b6615fb10b4749a54b29c2894a00df")
    private val etherscanKey = "NHURUUGCB2HDQFS26CKTRT51K4PHBY7234"
    private val networkTypeRopsten: EthereumKit.NetworkType = EthereumKit.NetworkType.Ropsten
    private val networkTypeKovan: EthereumKit.NetworkType = EthereumKit.NetworkType.Kovan

    private var secret = Secret.generate()
    private var makerRequest: SwapRequest? = null
    private var takerResponse: SwapResponse? = null
    private var makerContractId: String? = null
    private var takerContractId: String? = null

    private var swapKitRopsten: EtomicSwapKit
    private lateinit var ethereumKitRopsten: EthereumKit
    private lateinit var ethereumAdapterRopsten: EthereumAdapter

    private var swapKitKovan: EtomicSwapKit
    private lateinit var ethereumKitKovan: EthereumKit
    private lateinit var ethereumAdapterKovan: EthereumAdapter

    val rethBalance = MutableLiveData<BigDecimal>()
    val kethBalance = MutableLiveData<BigDecimal>()
    val lastBlockHeightRopsten = MutableLiveData<Long>()
    val lastBlockHeightKovan = MutableLiveData<Long>()
    val makerOrderRequestData = MutableLiveData<SwapRequest>()
    val takerOrderRequestData = MutableLiveData<SwapResponse>()
    val makerContractIdLive = MutableLiveData<String>()
    val takerContractIdLive = MutableLiveData<String>()

    val receiveAddress: String
        get() = ethereumKitRopsten.receiveAddress

    private val gasInfoProvider = object : ContractGasProvider {
        override fun getGasLimit(contractFunc: String?): BigInteger =
            200_000.toBigInteger()

        override fun getGasLimit(): BigInteger {
            return getGasLimit("")
        }

        override fun getGasPrice(contractFunc: String?): BigInteger =
            5_000_000_000.toBigInteger() // Gas price 5 GWei

        override fun getGasPrice(): BigInteger {
            return getGasLimit("")
        }
    }

    private val disposables = CompositeDisposable()

    init {
//        val words = "grocery hedgehog relief fancy pond surprise panic slight clog female deal wash".split(" ")
        val words = "surprise fancy pond panic grocery hedgehog slight relief deal wash clog female".split(" ")

        val seed = Mnemonic().toSeed(words)
        val hdWallet = HDWallet(seed, 1)
        val privateKey = hdWallet.privateKey(0, 0, true).privKey

        initEthereumKits(privateKey)

        val providerMode = EtomicSwapKit.RpcProviderMode.Infura(infuraCredentials.projectId, infuraCredentials.secretKey)
        swapKitRopsten = EtomicSwapKit.newInstance(
            privateKey,
            providerMode,
            EtomicSwapKit.NetworkType.Ropsten,
            gasInfoProvider
        )

        swapKitKovan = EtomicSwapKit.newInstance(
            privateKey,
            providerMode,
            EtomicSwapKit.NetworkType.Kovan,
            gasInfoProvider
        )
    }

    private fun getContractForCoin(symbol: String?): HtlcContract {
        return when(symbol) {
            "KETH" -> swapKitKovan.htlcContract
            else -> swapKitRopsten.htlcContract
        }
    }

    private fun getAddressForCoin(symbol: String?): String = if (symbol == "RETH") {
        ethereumKitRopsten.receiveAddress
    } else {
        ethereumKitKovan.receiveAddress
    }

    //region Maker

    fun newContract(
        makerCoin: String,
        takerCoin: String,
        makerAmount: BigDecimal,
        takerAmount: BigDecimal
    ) {
        secret = Secret.generate()
        val timeLock = (Date().time / 1000) + 2400

        val receiveAddress = getAddressForCoin(makerCoin)

        val swapRequest = SwapRequest(
            receiveAddress,
            makerCoin,
            takerCoin,
            makerAmount.fromEther(),
            takerAmount.fromEther(),
            timeLock,
            secret.hash
        )

        makerRequest = swapRequest
        makerOrderRequestData.value = swapRequest
    }

    fun onSwapResponse(swapRequest: SwapRequest) {
        val receiveAddress = getAddressForCoin(swapRequest.makerCoin)

        val swapResponse = SwapResponse(
            receiveAddress,
            swapRequest.makerCoin,
            swapRequest.takerCoin,
            swapRequest.makerAmount,
            swapRequest.takerAmount,
            swapRequest.timeLock,
            swapRequest.secretHash
        )

        makerRequest = swapRequest
        takerResponse = swapResponse
        takerOrderRequestData.value = swapResponse
    }

    fun onMakerDeposit(takerResponse: SwapResponse) {
        val contract = when(takerResponse.makerCoin) {
            "KETH" -> swapKitKovan.htlcContract
            else -> swapKitRopsten.htlcContract
        }

        contract.newContract(
            takerResponse.takerAddress,
            secret.hashBytes,
            takerResponse.timeLock.toBigInteger(),
            takerResponse.makerAmount
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                contract.getLogHTLCNewEvents(it)?.forEach {
                    makerContractId = it.contractId.toHexString()
                    makerContractIdLive.value = makerContractId
                    Log.d("ololo", "New Contract ${it.contractId.toHexString()}")
                }
                Log.d("ololo", "Maker Contract transaction $it")
            }, {
                Log.d("ololo", "New Contract error $it")
            }).let { disposables.add(it) }
    }

    fun onTakerDeposit() {
        takerResponse?.let {
            val contract = getContractForCoin(takerResponse?.takerCoin)

            contract.newContract(
                makerRequest?.makerAddress,
                makerRequest?.secretHash?.hexToBytes(),
                ((Date().time / 1000) + 1200).toBigInteger(),
                makerRequest?.takerAmount
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    contract.getLogHTLCNewEvents(it)?.forEach {
                        takerContractId = it.contractId.toHexString()
                        takerContractIdLive.value = takerContractId
                        Log.d("ololo", "New Contract ${it.contractId.toHexString()}")
                    }
                    Log.d("ololo", "Taker Contract transaction $it")
                }, {
                    Log.d("ololo", "New Contract error $it")
                }).let { disposables.add(it) }
        }
    }

    fun makerWithdraw(contractId: String) {
        val contract = getContractForCoin(makerRequest?.takerCoin)

        contract.withdraw(contractId.hexToBytes(), secret.value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("ololo", "Maker withdraw success $it")
            }, {
                Log.d("ololo", "Maker withdraw error $it")
            }).let { disposables.add(it) }
    }

    fun takerWithdraw(contractId: String) {
        val makerContract = getContractForCoin(makerRequest?.makerCoin)
        val takerContract = getContractForCoin(makerRequest?.takerCoin)

        takerContract.getContract(takerContractId?.hexToBytes()!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                makerContract.withdraw(contractId.hexToBytes(), it.preimage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("ololo", "Taker withdraw success $it")
                    }, {
                        Log.d("ololo", "Taker withdraw error $it")
                    }).let { disposables.add(it) }
                Log.d("ololo", "Taker withdraw success $it")
            }, {
                Log.d("ololo", "Taker withdraw error $it")
            }).let { disposables.add(it) }
    }

    fun refund(isMaker: Boolean) {
        if (isMaker) {
            makerContractId
        } else {
            takerContractId
        }?.hexToBytes()?.let {
            when(makerRequest?.makerCoin) {
                "KETH" -> swapKitKovan.htlcContract
                else -> swapKitRopsten.htlcContract
            }.refund(it).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("ololo", "Maker refund $it")
                }, {
                    Log.d("ololo", "New Contract error $it")
                }).let { disposables.add(it) }
        }
    }

    //endregion

    //region Base

    private fun initEthereumKits(privateKey: BigInteger) {
        ethereumKitRopsten = EthereumKit.getInstance(
            App.instance,
            privateKey,
            EthereumKit.SyncMode.ApiSyncMode(),
            networkTypeRopsten,
            infuraCredentials,
            etherscanKey,
            "eth-wallet-ropsten"
        )

        ethereumAdapterRopsten = EthereumAdapter(ethereumKitRopsten)

        ethereumAdapterRopsten.lastBlockHeightFlowable.subscribe {
            updateRopstenLastBlock()
        }.let { disposables.add(it) }

        ethereumAdapterRopsten.balanceFlowable.subscribe {
            updateRopstenBalance()
        }.let { disposables.add(it) }

        ethereumKitKovan = EthereumKit.getInstance(
            App.instance,
            privateKey,
            EthereumKit.SyncMode.ApiSyncMode(),
            networkTypeKovan,
            infuraCredentials,
            etherscanKey,
            "eth-wallet-kovan"
        )

        ethereumAdapterKovan = EthereumAdapter(ethereumKitKovan)

        ethereumKitRopsten.start()
        ethereumKitKovan.start()

        ethereumAdapterKovan.lastBlockHeightFlowable.subscribe {
            updateKovanLastBlock()
        }.let { disposables.add(it) }

        ethereumAdapterKovan.balanceFlowable.subscribe {
            updateKovanBalance()
        }.let { disposables.add(it) }

        updateRopstenLastBlock()
        updateRopstenBalance()
        updateKovanLastBlock()
        updateKovanBalance()
    }

    private fun updateKovanLastBlock() {
        lastBlockHeightKovan.postValue(ethereumKitKovan.lastBlockHeight)
    }

    private fun updateRopstenLastBlock() {
        lastBlockHeightRopsten.postValue(ethereumKitRopsten.lastBlockHeight)
    }

    private fun updateKovanBalance() {
        kethBalance.postValue(ethereumAdapterKovan.balance)
    }

    private fun updateRopstenBalance() {
        rethBalance.postValue(ethereumAdapterRopsten.balance)
    }

    fun refresh() {
        ethereumKitRopsten.refresh()
        ethereumKitKovan.refresh()
    }

    //endregion

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}