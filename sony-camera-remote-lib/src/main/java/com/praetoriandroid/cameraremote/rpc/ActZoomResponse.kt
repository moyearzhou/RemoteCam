package com.praetoriandroid.cameraremote.rpc

class ActZoomResponse : BaseResponse<Array<String?>?>() {
    @Throws(ValidationException::class)
    override fun validate() {
        super.validate()
        val result = result
        if (isOk) {
            if (result.size != 1) {
                throw IllegalResultSizeException(1, result.size)
            }
        }
    }

    fun capturingInProgress(): Boolean {
        return !isOk && errorCode == ERROR_STILL_CAPTURING_NOT_FINISHED
    }

    val urls: Array<String?>?
        get() = result[0]
}
