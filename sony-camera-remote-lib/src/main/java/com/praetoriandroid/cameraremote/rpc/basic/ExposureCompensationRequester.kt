package com.praetoriandroid.cameraremote.rpc.basic

import com.praetoriandroid.cameraremote.rpc.BaseRequest
import com.praetoriandroid.cameraremote.rpc.BaseResponse
import com.praetoriandroid.cameraremote.rpc.IllegalResultSizeException
import com.praetoriandroid.cameraremote.rpc.RpcMethod
import com.praetoriandroid.cameraremote.rpc.ValidationException

class ExposureCompensationResponse : BaseResponse<String?>() {
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

class GetExposureCompensationRequest :
    BaseRequest<Void, ExposureCompensationResponse>(ExposureCompensationResponse::class.java, RpcMethod.getExposureCompensation)


class SetExposureCompensationResponse : BaseResponse<String?>() {
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

class SetExposureCompensationRequest(evValue: Float) :
    BaseRequest<Float, SetExposureCompensationResponse>(SetExposureCompensationResponse::class.java, RpcMethod.setExposureCompensation, evValue)