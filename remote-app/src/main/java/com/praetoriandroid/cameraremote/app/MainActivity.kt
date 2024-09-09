package com.praetoriandroid.cameraremote.app

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProvider
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.remote_app.R
import com.praetoriandroid.cameraremote.LiveViewFetcher
import com.praetoriandroid.cameraremote.app.Rpc.ConnectionListener
import com.praetoriandroid.cameraremote.app.Rpc.LiveViewCallback
import com.praetoriandroid.cameraremote.app.Rpc.ResponseHandler
import com.praetoriandroid.cameraremote.rpc.ActTakePictureRequest
import com.praetoriandroid.cameraremote.rpc.ActTakePictureResponse
import com.praetoriandroid.cameraremote.rpc.SetSelfTimerRequest
import com.praetoriandroid.cameraremote.rpc.SimpleResponse
import com.praetoriandroid.widget.RadialSelector.OnValueSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.UiThread

class MainActivity : AppCompatActivity(), ConnectionListener {


    lateinit var liveView: LiveView
    lateinit var shot: ImageButton
    lateinit var progress: View
    lateinit var connectionErrorDialog: View
    lateinit var progressLabel: TextView
    lateinit var selfTimer: SelfTimerSelector

    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 使内容扩展到状态栏和导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                // 标志位，允许内容扩展到状态栏和导航栏区域
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

                // 状态栏和导航栏透明
                statusBarColor = android.graphics.Color.TRANSPARENT
                navigationBarColor = android.graphics.Color.TRANSPARENT
            }
        }

        viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainViewModel::class.java)

        initView()
        initListener()

        initRPC()


    }

    private fun initRPC() {
        // 在您的 Activity 或 Fragment 中
        CoroutineScope(Dispatchers.IO).launch {
            try {
                viewModel.rpc.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
    }

    override fun onStart() {
        super.onStart()
        viewModel.rpc.registerInitCallback(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.rpc.unregisterInitCallback(this)
        viewModel.rpc.stopLiveView()
    }

    private fun shotClicked() {
        viewModel.shotButtonStatus.postValue(false)

        // 在您的 Activity 或 Fragment 中
        CoroutineScope(Dispatchers.IO).launch {
            try {
                viewModel.rpc.sendRequest(
                    ActTakePictureRequest(),
                    shot,
                    object : ResponseHandler<ActTakePictureResponse> {
                        override fun onSuccess(response: ActTakePictureResponse) {
                            viewModel.shotButtonStatus.postValue(true)
                        }

                        override fun onFail(e: Throwable) {
                            Log.e("@@@@@", "Shot failed", e)
                            viewModel.shotButtonStatus.postValue(true)
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    override fun onConnected() {
        viewModel.showProgress.postValue(false)
        viewModel.shotButtonStatus.postValue(true)

        showToast("连接成功")

        if (isFinishing) {
            return
        }
        viewModel.rpc.startLiveView(object : LiveViewCallback {
            override fun onNextFrame(frame: LiveViewFetcher.Frame) {
                val bitmap = BitmapFactory.decodeByteArray(frame.buffer, 0, frame.size)
                liveView.putFrame(bitmap)
            }

            override fun onError(e: Throwable) {
                Log.e("@@@@@", "Live view error: $e")
                viewModel.rpc.stopLiveView()
                viewModel.showErrorDialog.postValue(true)
            }
        })
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    override fun onConnectionFailed(e: Throwable) {
        viewModel.showProgress.postValue(false)
        viewModel.showErrorDialog.postValue(true)
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
        viewModel.rpc.connect()
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
                }
            })
    }

    private fun showToast(msg: String) {
        // 使用 Handler 将结果返回到主线程
        Handler(Looper.getMainLooper()).post { // 更新 UI
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }

    }
}