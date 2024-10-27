package com.praetoriandroid.cameraremote.app.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.remote_app.databinding.ActivityTestBinding

class TestActivity : ComponentActivity() {

    private val mBinding by lazy {
        ActivityTestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

    }
}