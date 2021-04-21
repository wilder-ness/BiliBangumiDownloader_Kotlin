package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivitySeasonBinding

class Season: BaseActivity<ActivitySeasonBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onSetSwipeBackEnable(): Boolean = true
}