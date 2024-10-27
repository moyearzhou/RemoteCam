package com.praetoriandroid.cameraremote.rpc.basic

import com.praetoriandroid.cameraremote.rpc.BaseRequest
import com.praetoriandroid.cameraremote.rpc.BaseResponse
import com.praetoriandroid.cameraremote.rpc.IllegalResultSizeException
import com.praetoriandroid.cameraremote.rpc.RpcMethod
import com.praetoriandroid.cameraremote.rpc.ValidationException

class IsoSpeedRateResponse : BaseResponse<String?>() {
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

class GetIsoSpeedRateRequest :
    BaseRequest<Void, IsoSpeedRateResponse>(IsoSpeedRateResponse::class.java, RpcMethod.getIsoSpeedRate)


class GetAvailableIsoRequest :
    BaseRequest<Void, AvailableIsoResponse>(AvailableIsoResponse::class.java, RpcMethod.getAvailableIsoSpeedRate)
class AvailableIsoResponse : BaseResponse<String>() {
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

    // todo 解决可用iso的数据转换问题
    // 返回值格式：["AUTO",["AUTO","50","64","80","100","125","160","200","250","320","400","500","640","800","1000","1250","1600","2000","2500","3200","4000","5000","6400","8000","10000","12800","16000","20000","25600","32000","40000","51200","64000","80000","102400"]]
    val value: String
        get() = parseData(result)


    private fun parseData(res: Array<String>): String {
        if (res.size > 1) {
            return res[1]
        }

        return res[0]
    }
}
