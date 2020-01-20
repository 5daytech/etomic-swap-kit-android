package com.fridaytech.etomicswapkit.contracts

import com.fridaytech.etomicswapkit.contracts.models.AtomicContract
import com.fridaytech.etomicswapkit.contracts.models.LogHTLCNewEventResponse
import io.reactivex.Flowable
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tuples.generated.Tuple8
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Callable

class HtlcContract(
    contractAddress: String,
    credentials: Credentials,
    contractGasProvider: ContractGasProvider,
    providerUrl: String
) : Contract(BINARY, contractAddress, Web3j.build(HttpService(providerUrl)), credentials, contractGasProvider) {

    fun newContract(
        receiver: String?,
        hashLock: ByteArray?,
        timeLock: BigInteger?,
        weiValue: BigInteger?
    ): Flowable<TransactionReceipt> {
        val function = Function(
            FUNC_NEW_CONTRACT,
            listOf<Type<*>>(
                Address(receiver),
                Bytes32(hashLock),
                Uint256(timeLock)
            ), emptyList()
        )
        return executeRemoteCallTransaction(function, weiValue).flowable()
    }

    fun getLogHTLCNewEvents(transactionReceipt: TransactionReceipt?): List<LogHTLCNewEventResponse>? {
        val valueList = extractEventParametersWithLog(
            LOGHTLCNEW_EVENT,
            transactionReceipt
        )
        val responses = ArrayList<LogHTLCNewEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = LogHTLCNewEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.contractId = eventValues.indexedValues[0].value as ByteArray
            typedResponse.sender = eventValues.indexedValues[1].value as String
            typedResponse.receiver = eventValues.indexedValues[2].value as String
            typedResponse.amount = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse.hashlock = eventValues.nonIndexedValues[1].value as ByteArray
            typedResponse.timelock = eventValues.nonIndexedValues[2].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun refund(contractId: ByteArray): Flowable<TransactionReceipt> {
        val function = Function(
            FUNC_REFUND,
            listOf<Type<*>>(Bytes32(contractId)),
            listOf<TypeReference<*>>()
        )

        return executeRemoteCallTransaction(function).flowable()
    }

    fun getContract(contractId: ByteArray): Flowable<AtomicContract> {
        val function = Function(
            FUNC_GET_CONTRACT,
            listOf<Type<*>>(Bytes32(contractId)),
            listOf<TypeReference<*>>(
                object : TypeReference<Address>() {},
                object : TypeReference<Address>() {},
                object : TypeReference<Uint>() {},
                object : TypeReference<Bytes32>() {},
                object : TypeReference<Uint>() {},
                object : TypeReference<Bool>() {},
                object : TypeReference<Bool>() {},
                object : TypeReference<Bytes32>() {}
            )
        )

        return RemoteCall(
            Callable {
                val results = executeCallMultipleValueReturn(function)

                Tuple8(
                    results[0].value as String,
                    results[1].value as String,
                    results[2].value as BigInteger,
                    results[3].value as ByteArray,
                    results[4].value.toString().toLong(),
                    results[5].value as Boolean,
                    results[6].value as Boolean,
                    results[7].value as ByteArray
                )
            }
        ).flowable().map {
            AtomicContract(
                it.value1.toString(),
                it.value2.toString(),
                it.value3,
                it.value4,
                it.value5,
                it.value6,
                it.value7,
                it.value8
            )
        }
    }

    fun withdraw(contractId: ByteArray, secret: ByteArray): Flowable<TransactionReceipt> {
        val function = Function(
            FUNC_WITHDRAW,
            listOf<Type<*>>(Bytes32(contractId), Bytes32(secret)),
            listOf<TypeReference<*>>()
        )
        return executeRemoteCallTransaction(function).flowable()
    }

    companion object {
        internal const val FUNC_NEW_CONTRACT = "newContract"
        internal const val FUNC_REFUND = "refund"
        internal const val FUNC_GET_CONTRACT = "getContract"
        internal const val FUNC_WITHDRAW = "withdraw"

        private const val BINARY = ""

        val LOGHTLCNEW_EVENT = Event(
            "LogHTLCNew",
            listOf<TypeReference<*>>(
                object : TypeReference<Bytes32?>(true) {},
                object : TypeReference<Address?>(true) {},
                object : TypeReference<Address?>(true) {},
                object : TypeReference<Uint256?>() {},
                object : TypeReference<Bytes32?>() {},
                object : TypeReference<Uint256?>() {})
        )
    }
}