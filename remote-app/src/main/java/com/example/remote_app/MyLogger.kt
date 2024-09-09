package com.example.remote_app

import android.util.Log

class MyLogger {

    companion object {

        const val TAG = "SonyCam"

        fun e(msg: String) {
            e(TAG, msg)
        }

        fun w(msg: String) {
            w(TAG, msg)
        }

        fun i(msg: String) {
            i(TAG, msg)
        }

        fun d(msg: String) {
            d(TAG, msg)
        }

        fun e(tag: String, msg: String) {
            Log.e(tag, msg)
        }

        fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        fun w(tag: String, msg: String) {
            Log.w(tag, msg)
        }

        fun i(tag: String, msg: String) {
            Log.i(tag, msg)
        }
    }
}