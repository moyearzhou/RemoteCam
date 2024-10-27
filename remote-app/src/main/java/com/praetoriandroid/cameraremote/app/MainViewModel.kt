package com.praetoriandroid.cameraremote.app

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.remote_app.MyLogger
import com.example.remote_app.Tracker
import com.praetoriandroid.cameraremote.LiveViewFetcher
import com.praetoriandroid.cameraremote.rpc.ActTakePictureRequest
import com.praetoriandroid.cameraremote.rpc.ActTakePictureResponse
import com.praetoriandroid.cameraremote.rpc.basic.ApertureResponse
import com.praetoriandroid.cameraremote.rpc.basic.AvailableIsoResponse
import com.praetoriandroid.cameraremote.rpc.basic.ExposureCompensationResponse
import com.praetoriandroid.cameraremote.rpc.basic.FocusModeResponse
import com.praetoriandroid.cameraremote.rpc.basic.GetApertureRequest
import com.praetoriandroid.cameraremote.rpc.basic.GetAvailableIsoRequest
import com.praetoriandroid.cameraremote.rpc.basic.GetExposureCompensationRequest
import com.praetoriandroid.cameraremote.rpc.basic.GetFocusModeRequest
import com.praetoriandroid.cameraremote.rpc.basic.GetIsoSpeedRateRequest
import com.praetoriandroid.cameraremote.rpc.basic.GetShutterSpeedRequest
import com.praetoriandroid.cameraremote.rpc.basic.GetWhiteBalanceRequest
import com.praetoriandroid.cameraremote.rpc.basic.IsoSpeedRateResponse
import com.praetoriandroid.cameraremote.rpc.basic.SetExposureCompensationRequest
import com.praetoriandroid.cameraremote.rpc.basic.SetExposureCompensationResponse
import com.praetoriandroid.cameraremote.rpc.basic.ShutterSpeedRequester
import com.praetoriandroid.cameraremote.rpc.basic.WhiteBalance
import com.praetoriandroid.cameraremote.rpc.basic.WhiteBalanceResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    val TAG = "MainViewModel"

    val showProgress = MutableLiveData<Boolean>()
    val showErrorDialog = MutableLiveData<Boolean>()
    val shotButtonStatus = MutableLiveData<Boolean>()

    val isoValue = MutableLiveData<String>("-")
    val apertureValue = MutableLiveData<String>("-")
    val shutterValue = MutableLiveData<String>("-")

    val evValue = MutableLiveData<String>("-")
    val wbValue = MutableLiveData<WhiteBalance>(WhiteBalance("-", -1))
    val focusValue = MutableLiveData<String>("-")

    val availableIso = MutableLiveData<String>("")

    val liveWatchEnable = MutableLiveData(true)

    // 预览画面是否启用
    var cameraViewActive = false

    /**
     * 是否实时获取曝光参数
     */
    val cameraPramsInstantFetch = false

    val rpc: Rpc by lazy {
        Rpc()
    }

    fun initRPC() {
        // 在您的 Activity 或 Fragment 中
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Tracker
                    .create("执行请求-连接rpc")
                    .track()
                rpc.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onConnected() {
        showProgress.postValue(false)
        shotButtonStatus.postValue(true)

        Tracker
            .create("RPC连接事件-连接成功")
            .track()

        // 获取曝光参数
        fetchExposureParam()
        // 启用实时获取view
        cameraViewActive = true
    }

    fun onConnectionFailed(e: Throwable) {
        Tracker
            .create("RPC连接-连接失败")
            .withParma("error", "${e}")
            .track()
        showProgress.postValue(false)
        showErrorDialog.postValue(true)
    }

    fun takeShot() {
        // 在您的 Activity 或 Fragment 中
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rpc.sendRequest(
                    ActTakePictureRequest(),
                    "TakeShot",
                    object : Rpc.ResponseHandler<ActTakePictureResponse> {
                        override fun onSuccess(response: ActTakePictureResponse) {
                            shotButtonStatus.postValue(true)
                        }

                        override fun onFail(e: Throwable) {
                            Log.e(TAG, "Shot failed", e)
                            shotButtonStatus.postValue(true)
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startLiveView(liveView: LiveView) {
        Tracker
            .create("执行请求-请求实时预览画面")
            .track()
        CoroutineScope(Dispatchers.IO).launch {
            rpc.startLiveView(object : Rpc.LiveViewCallback {
                override fun onNextFrame(frame: LiveViewFetcher.Frame) {
                    val bitmap = BitmapFactory.decodeByteArray(frame.buffer, 0, frame.size)
                    liveView.putFrame(bitmap)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Live view error: $e")
                    rpc.stopLiveView()
                    showErrorDialog.postValue(true)
                }
            })
        }
    }

    fun stopPause(listener: Rpc.ConnectionListener) {
        Tracker
            .create("事件-关闭画面实时预览")
            .track()

        rpc.unregisterInitCallback(listener)

        CoroutineScope(Dispatchers.IO).launch {
            rpc.stopLiveView()
        }

    }

    fun fetchExposureParam() {
        if (!rpc.isInitialized) {
            MyLogger.d("rpc未初始化，无法获取曝光参数")
        }

        Tracker
            .create("执行连接请求-获取基本曝光参数")
            .track()

        CoroutineScope(Dispatchers.IO).launch {


            rpc.sendRequest(
                GetIsoSpeedRateRequest(),
                "IsoSpeedRate",
                object : Rpc.ResponseHandler<IsoSpeedRateResponse> {
                    override fun onSuccess(response: IsoSpeedRateResponse) {

                        isoValue.postValue(response.value)
                        MyLogger.d("获取参数成功: " + response.value)
                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("zoom失败: $e")
                        e.printStackTrace()
                    }
                })

            // 快门速度
            rpc.sendRequest(
                GetShutterSpeedRequest(),
                "ShutterSpeed",
                object : Rpc.ResponseHandler<ShutterSpeedRequester> {
                    override fun onSuccess(response: ShutterSpeedRequester) {

                        shutterValue.postValue(response.value)
                        MyLogger.d("获取参数成功: " + response.value)
                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("zoom失败: $e")
                        e.printStackTrace()
                    }
                })

            // 光圈
            rpc.sendRequest(
                GetApertureRequest(),
                "Aperture",
                object : Rpc.ResponseHandler<ApertureResponse> {
                    override fun onSuccess(response: ApertureResponse) {

                        apertureValue.postValue(response.value)
                        MyLogger.d("获取参数成功: " + response.value)
                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("失败: $e")
                        e.printStackTrace()
                    }
                })

            // 曝光补偿
            rpc.sendRequest(
                GetExposureCompensationRequest(),
                "ExposureCompensation",
                object : Rpc.ResponseHandler<ExposureCompensationResponse> {
                    override fun onSuccess(response: ExposureCompensationResponse) {
                        evValue.postValue(response.value)
                        MyLogger.d("获取参数成功: " + response.value)
                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("失败: $e")
                        e.printStackTrace()
                    }
                })

            // 白平衡
            rpc.sendRequest(
                GetWhiteBalanceRequest(),
                "WhiteBalance",
                object : Rpc.ResponseHandler<WhiteBalanceResponse> {
                    override fun onSuccess(response: WhiteBalanceResponse) {

                        val whiteBalance = response.value

                        MyLogger.d("获取参数成功: " + response.value)
                        wbValue.postValue(whiteBalance)

                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("失败: $e")
                        e.printStackTrace()
                    }
                })

            // 对焦模式
            rpc.sendRequest(
                GetFocusModeRequest(),
                "FocusMode",
                object : Rpc.ResponseHandler<FocusModeResponse> {
                    override fun onSuccess(response: FocusModeResponse) {
                        MyLogger.d("获取参数成功: " + response.value)
                        focusValue.postValue(response.value)

                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("失败: $e")
                        e.printStackTrace()
                    }
                })


        }

    }

    fun fetchAvailableIso() {
        CoroutineScope(Dispatchers.IO).launch {
            rpc.sendRequest(
                GetAvailableIsoRequest(),
                "AvailableIso",
                object : Rpc.ResponseHandler<AvailableIsoResponse> {
                    override fun onSuccess(response: AvailableIsoResponse) {

                        availableIso.postValue(response.value)
                        MyLogger.d("获取可用iso成功: " + response.value)
                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("获取可用iso失败: $e")
                        e.printStackTrace()
                    }
                })
        }
    }

    fun setEv(evValue: Float) {
        // todo 确保ev值是正常可用的

        CoroutineScope(Dispatchers.IO).launch {
            rpc.sendRequest(
                SetExposureCompensationRequest(1.0f),
                "AvailableIso",
                object : Rpc.ResponseHandler<SetExposureCompensationResponse> {
                    override fun onSuccess(response: SetExposureCompensationResponse) {

                        availableIso.postValue(response.value)
                        MyLogger.d("获取可用iso成功: " + response.value)
                    }

                    override fun onFail(e: Throwable) {
                        MyLogger.e("获取可用iso失败: $e")
                        e.printStackTrace()
                    }
                })
        }

    }


}