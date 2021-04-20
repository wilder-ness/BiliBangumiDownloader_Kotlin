package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityDownloadBinding

class Download: BaseActivity<ActivityDownloadBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivityDownloadBinding = ActivityDownloadBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}