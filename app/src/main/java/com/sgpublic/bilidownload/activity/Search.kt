package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivitySearchBinding

class Search: BaseActivity<ActivitySearchBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivitySearchBinding = ActivitySearchBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}