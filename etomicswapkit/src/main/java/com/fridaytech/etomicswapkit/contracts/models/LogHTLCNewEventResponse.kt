package com.fridaytech.etomicswapkit.contracts.models

import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

class LogHTLCNewEventResponse {
    var log: Log? = null
    var contractId: ByteArray? = null
    var sender: String? = null
    var receiver: String? = null
    var amount: BigInteger? = null
    var hashlock: ByteArray? = null
    var timelock: BigInteger? = null
}