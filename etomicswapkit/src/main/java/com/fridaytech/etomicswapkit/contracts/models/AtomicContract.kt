package com.fridaytech.etomicswapkit.contracts.models

import com.fridaytech.etomicswapkit.utils.toHexString
import java.math.BigInteger

data class AtomicContract(
    val sender: String,
    val receiver: String,
    val amount: BigInteger,
    val hashLock: ByteArray,
    val timeLock: Long,
    val withdrawn: Boolean,
    val refunded: Boolean,
    val preimage: ByteArray
) {
    override fun toString(): String {
        return "sender: $sender,\n" +
                "receiver: $receiver,\n" +
                "amount: $amount,\n" +
                "hashLock: 0x${hashLock.toHexString()},\n" +
                "timeLock: $timeLock,\n" +
                "withdrawn: $withdrawn,\n" +
                "refunded: $refunded,\n" +
                "preimage: 0x${preimage.toHexString()}"
    }
}