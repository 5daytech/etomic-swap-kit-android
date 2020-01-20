package com.fridaytech.etomicswapkit.models

import java.math.BigInteger

data class SwapRequest(
    val makerAddress: String,
    val makerCoin: String,
    val takerCoin: String,
    val makerAmount: BigInteger,
    val takerAmount: BigInteger,
    val timeLock: Long,
    val secretHash: String
)

data class SwapResponse(
    val takerAddress: String,
    val makerCoin: String,
    val takerCoin: String,
    val makerAmount: BigInteger,
    val takerAmount: BigInteger,
    val timeLock: Long,
    val secretHash: String
)