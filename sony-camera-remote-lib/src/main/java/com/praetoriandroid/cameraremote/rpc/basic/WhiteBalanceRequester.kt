package com.praetoriandroid.cameraremote.rpc.basic

import com.praetoriandroid.cameraremote.rpc.BaseRequest
import com.praetoriandroid.cameraremote.rpc.BaseResponse
import com.praetoriandroid.cameraremote.rpc.IllegalResultSizeException
import com.praetoriandroid.cameraremote.rpc.RpcMethod
import com.praetoriandroid.cameraremote.rpc.ValidationException

class WhiteBalanceResponse : BaseResponse<WhiteBalance>() {
    @Throws(ValidationException::class)
    override fun validate() {
        super.validate()
        if (isOk) {
            val result = result
            if (result.size != 1) {
                throw IllegalResultSizeException(1, result.size)
            }
        }
    }

    val value: WhiteBalance
        get() = result[0]!!
}

class GetWhiteBalanceRequest :
    BaseRequest<Void, WhiteBalanceResponse>(WhiteBalanceResponse::class.java, RpcMethod.getWhiteBalance)


data class WhiteBalance(val whiteBalanceMode: String, val colorTemperature: Int)