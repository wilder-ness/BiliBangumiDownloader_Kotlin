package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivitySettingBinding

class Setting: BaseActivity<ActivitySettingBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivitySettingBinding = ActivitySettingBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}