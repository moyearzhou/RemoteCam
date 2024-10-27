package com.praetoriandroid.cameraremote.rpc.basic

import com.praetoriandroid.cameraremote.rpc.BaseRequest
import com.praetoriandroid.cameraremote.rpc.BaseResponse
import com.praetoriandroid.cameraremote.rpc.IllegalResultSizeException
import com.praetoriandroid.cameraremote.rpc.RpcMethod
import com.praetoriandroid.cameraremote.rpc.ValidationException

class ApertureResponse : BaseResponse<String?>() {
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

    val value: String
        get() = result[0]!!
}

class GetApertureRequest :
    BaseRequest<Void, ApertureResponse>(ApertureResponse::class.java, RpcMethod.getFNumber)