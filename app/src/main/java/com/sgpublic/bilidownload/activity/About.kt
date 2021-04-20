package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityAboutBinding

class About: BaseActivity<ActivityAboutBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivityAboutBinding = ActivityAboutBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}