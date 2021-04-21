package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityLoginWebBinding

class LoginWeb: BaseActivity<ActivityLoginWebBinding>() {
    companion object {
        fun startActivity(packageContext: Context){
            val intent = Intent(packageContext, LoginWeb::class.java)
            packageContext.startActivity(intent)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onSetSwipeBackEnable(): Boolean = true
}