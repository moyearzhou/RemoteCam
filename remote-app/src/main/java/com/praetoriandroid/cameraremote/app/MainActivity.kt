package com.praetoriandroid.cameraremote.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.remote_app.MyLogger
import com.example.remote_app.R
import com.example.remote_app.Tracker
import com.example.remote_app.databinding.ActivityMainBinding
import com.example.remote_app.databinding.LayoutParamAdjustBinding
import com.praetoriandroid.cameraremote.app.Rpc.ConnectionListener
import com.praetoriandroid.cameraremote.app.Rpc.ResponseHandler
import com.praetoriandroid.cameraremote.app.preferences.SettingsActivity
import com.praetoriandroid.cameraremote.rpc.ActZoomResponse
import com.praetoriandroid.cameraremote.rpc.SetSelfTimerRequest
import com.praetoriandroid.cameraremote.rpc.SimpleResponse
import com.praetoriandroid.cameraremote.rpc.ZoomRequest
import com.praetoriandroid.widget.RadialSelector.OnValueSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.UiThread

class MainActivity : BaseActivity(), ConnectionListener {

    private val TAG = "Rpc"

    private val UPDATE_INTERVAL: Long = 1000 // 1秒钟

    lateinit var liveView: LiveView
    lateinit var shot: ImageButton
    lateinit var progress: View
    lateinit var connectionErrorDialog: View
    lateinit var progressLabel: TextView
    lateinit var selfTimer: SelfTimerSelector

    private lateinit var viewModel: MainViewModel
    private val mBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private lateinit var updateTask: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainViewModel::class.java)

        initView()
        initListener()

        updateTask = object : Runnable {
            override fun run() {
                if (viewModel.cameraViewActive && viewModel.cameraPramsInstantFetch) {
                    viewModel.fetchExposureParam()
                }

                handler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
        // 开始循环任务
        handler.post(updateTask)

        viewModel.initRPC()
    }

    private fun initListener() {
        viewModel.showProgress.observe(this) { isShow ->
            if (isShow!!) {
                showProgress()
            } else {
                dismissProgress()
            }
        }

        viewModel.showErrorDialog.observe(this) { isShow ->
            if (isShow!!) {
                showConnectionErrorDialog()
            } else {
                dismissConnectionErrorDialog()
            }
        }

        viewModel.shotButtonStatus.observe(this) { isShow ->
            shot.isEnabled = isShow!!
        }

        viewModel.isoValue.observe(this) {
            // todo 保证iso显示正确
//            if (it < 0) {
//                mBinding.layoutExposures.findViewById<TextView>(R.id.txt_iso).text = "-"
//                return@observe
//            }

            // todo 确保iso是可用的
            mBinding.layoutExposures.findViewById<TextView>(R.id.txt_iso).text = it.toString()
        }

        viewModel.shutterValue.observe(this) {

            // todo 确保是可用的
            mBinding.layoutExposures.findViewById<TextView>(R.id.txt_shutter).text = it.toString()
        }

        viewModel.apertureValue.observe(this) {
            // todo 确保是可用的
            mBinding.layoutExposures.findViewById<TextView>(R.id.txt_aperture).text = "f $it"
        }

        viewModel.evValue.observe(this) {
            // todo 确保是可用的
            mBinding.layoutExposures.findViewById<TextView>(R.id.txt_ev).text = it.toString()
        }

        viewModel.wbValue.observe(this) {
            // todo 确保是可用的
            // todo 考虑其他色温模式
            if (it.colorTemperature > 0) {
                mBinding.layoutExposures.findViewById<TextView>(R.id.txt_white_balance).text = it.colorTemperature.toString()
            } else {
                mBinding.layoutExposures.findViewById<TextView>(R.id.txt_white_balance).text = it.whiteBalanceMode
            }

        }

        viewModel.focusValue.observe(this) {
            // todo 确保是可用的
            mBinding.layoutExposures.findViewById<TextView>(R.id.txt_focus).text = it.toString()
        }

    }

    private fun initView() {
        liveView = findViewById(R.id.liveView)
        shot = findViewById(R.id.shot)
        progress = findViewById(R.id.progress)
        connectionErrorDialog = findViewById(R.id.connection_error_dialog)
        progressLabel = findViewById(R.id.progress_label)
        selfTimer = findViewById(R.id.self_timer)

        shot.setOnClickListener { view: View? -> shotClicked() }
        progressLabel.setText(R.string.connection_label)
        selfTimer.setOnValueSelectedListener(OnValueSelectedListener { timer: Int ->
            onSelfTimerSelected(
                timer
            )
        })

        findViewById<ImageButton>(R.id.btn_more).setOnClickListener {
//            showCameraInfoDialog()
            launchToSettings()
        }


        findViewById<LinearLayout>(R.id.lv_iso_setter).setOnClickListener {
            showIsoParamAdjuster()
        }


        findViewById<LinearLayout>(R.id.lv_ev_setter).setOnClickListener {
            showEvAdjuster()
        }

        findViewById<Button>(R.id.btn_reconnect).setOnClickListener {
            reconnectClicked()
        }

        findViewById<Button>(R.id.btn_wifi_settings).setOnClickListener {
            wiFiSettingsClicked()
        }

    }

    private fun showEvAdjuster() {
        val binding = LayoutParamAdjustBinding.inflate(layoutInflater)

        binding.txtValue.text = viewModel.evValue.value

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("曝光补偿")
            .setView(binding.root)
            .setPositiveButton("确定"
            ) { dialog, which ->

                viewModel.setEv(evValue = 0.3f)
            }
            .show()
    }

    private fun showIsoParamAdjuster() {
        viewModel.fetchAvailableIso()

        val msg = "当前ISO：${viewModel.isoValue.value} \n" +
                "可用ISO：${viewModel.availableIso.value}\n"
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg)
            .setPositiveButton("确定", null)
            .show()

    }

    private fun showCameraInfoDialog() {
        val msg = "设备ip：\n" +
                "光圈：\n"
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg)
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        MyLogger.d("==========onStart==========")
        viewModel.rpc.registerInitCallback(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPause(this)

        viewModel.cameraViewActive = false
    }

    private fun shotClicked() {
        // 关闭拍摄按钮
        viewModel.shotButtonStatus.postValue(false)
        // 开始拍摄
        viewModel.takeShot()
    }

    override fun onConnected() {
        showToast("连接成功")

        viewModel.onConnected()

        // 开始实时画面传输预览
        viewModel.startLiveView(liveView)

    }

    override fun onConnectionFailed(e: Throwable) {
        viewModel.onConnectionFailed(e)
    }

    @Click
    fun wiFiSettingsClicked() {
        viewModel.showErrorDialog.postValue(false)
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            viewModel.showErrorDialog.postValue(false)
            showToast(this.getString(R.string.error_no_wi_fi_settings_activity))
        }
    }

    @Click
    fun reconnectClicked() {
        dismissConnectionErrorDialog()
        showProgress()

        viewModel.initRPC()
    }

    fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    fun dismissProgress() {
        progress.visibility = View.GONE
    }

    fun showConnectionErrorDialog() {
        connectionErrorDialog.visibility = View.VISIBLE
    }

    fun dismissConnectionErrorDialog() {
        connectionErrorDialog.visibility = View.INVISIBLE
    }

    @Click
    fun zoomClicked() {
        Toast.makeText(this, "zoom", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.rpc.sendRequest(
                ZoomRequest(),
                "zoom",
                object : ResponseHandler<ActZoomResponse> {
                    override fun onSuccess(response: ActZoomResponse) {
                        showToast("zoom成功")
                    }

                    override fun onFail(e: Throwable) {
                        showToast("zoom失败: $e")
                        e.printStackTrace()
                    }
                })
        }

    }

    private fun onSelfTimerSelected(timerValue: Int) {
        viewModel.rpc.sendRequest(
            SetSelfTimerRequest(timerValue),
            selfTimer,
            object : ResponseHandler<SimpleResponse> {
                override fun onSuccess(response: SimpleResponse) {
                    showToast("Self timer was set to $timerValue")
                }

                override fun onFail(e: Throwable) {
                    showToast("Failed to set self timer")
                    e.printStackTrace()
                }
            })
    }

    private fun showToast(msg: String) {
        // 使用 Handler 将结果返回到主线程
        Handler(Looper.getMainLooper()).post { // 更新 UI
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MyLogger.e("=================应用被杀死")
    }

    private fun launchToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}