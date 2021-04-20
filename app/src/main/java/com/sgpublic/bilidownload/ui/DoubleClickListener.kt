package com.sgpublic.bilidownload.ui

import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
    override fun onClick(v: View) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime < DOUBLE_Effective_TIME) {
            onDoubleClick(v)
        }
        lastClickTime = currentTimeMillis
    }

    abstract fun onDoubleClick(v: View)

    companion object {
        private const val DOUBLE_Effective_TIME: Long = 200
        private const val DOUBLE_Invalid_TIME: Long = 200
        private var lastClickTime: Long = 0
    }
}