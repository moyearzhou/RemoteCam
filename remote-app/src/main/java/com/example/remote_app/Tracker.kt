package com.example.remote_app

import java.lang.StringBuilder

class Tracker {

    companion object {
        @JvmStatic
        fun create(title: String): TrackerObj {
            return TrackerObj(title)
        }



    }


    class TrackerObj(val title: String) {
        val paramMap = HashMap<String, String>()

        fun withParma(key: String, value: String): TrackerObj {
            paramMap[key] = value
            return this
        }

        fun track() {
            val paramStr = StringBuilder()
            paramMap.forEach {
                paramStr.append("${it.key}: ${it.value}\n")
            }

            MyLogger.d("Tracker", "==============================\n" +
                    "[$title]\n" +
                    paramStr.toString() +
                    "==============================\n")
        }
    }





}