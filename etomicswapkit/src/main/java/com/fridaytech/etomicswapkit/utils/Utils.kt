package com.fridaytech.etomicswapkit.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest

const val ETH_DECIMALS = 18

fun sha256(input: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input)
}

internal fun ByteArray?.toHexString(): String {
    return this?.joinToString(separator = "") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    } ?: ""
}

@Throws(NumberFormatException::class)
internal fun String.hexStringToByteArray(): ByteArray {
    return ByteArray(this.length / 2) {
        this.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }
}

@Throws(NumberFormatException::class)
internal fun String.hexToBytes(): ByteArray {
    return if (this.startsWith("0x")) {
        substring(2, length).hexStringToByteArray()
    } else {
        hexStringToByteArray()
    }
}

internal fun BigDecimal.fromEther(): BigInteger = this.movePointRight(ETH_DECIMALS).stripTrailingZeros().toBigInteger()
