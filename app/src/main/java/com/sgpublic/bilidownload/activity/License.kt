package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityLicenseBinding

class License: BaseActivity<ActivityLicenseBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivityLicenseBinding = ActivityLicenseBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}