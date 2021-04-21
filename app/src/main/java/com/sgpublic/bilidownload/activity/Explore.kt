package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityExploreBinding

class Explore: BaseActivity<ActivityExploreBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onSetSwipeBackEnable(): Boolean = true
}