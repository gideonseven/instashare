package com.don.myapplication

import android.os.Environment


open class CheckForSDCard {

    fun isCardPresent(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}