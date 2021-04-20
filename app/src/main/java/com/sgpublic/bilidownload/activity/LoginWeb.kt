package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityLoginWebBinding

class LoginWeb: BaseActivity<ActivityLoginWebBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivityLoginWebBinding = ActivityLoginWebBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}