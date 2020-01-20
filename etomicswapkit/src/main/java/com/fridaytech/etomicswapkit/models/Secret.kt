package com.fridaytech.etomicswapkit.models

import com.fridaytech.etomicswapkit.utils.sha256
import com.fridaytech.etomicswapkit.utils.toHexString
import java.util.*

class Secret private constructor(val value: ByteArray) {
    val hashBytes = sha256(value)
    val hash = "0x${hashBytes.toHexString()}"

    companion object {
        fun generate(): Secret {
            val bytes = ByteArray(32)
            UUID.randomUUID()
            Random().nextBytes(bytes)
            return Secret(bytes)
        }
    }

    override fun toString(): String = "0x${value.toHexString()}"
}