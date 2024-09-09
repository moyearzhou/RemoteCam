package com.praetoriandroid.cameraremote.app

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    val showProgress = MutableLiveData<Boolean>()
    val showErrorDialog = MutableLiveData<Boolean>()
    val shotButtonStatus = MutableLiveData<Boolean>()

    val rpc: Rpc by lazy {
        Rpc()
    }

}