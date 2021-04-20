package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityLoginBinding

class Login: BaseActivity<ActivityLoginBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}