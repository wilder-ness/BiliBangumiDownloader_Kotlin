package com.sgpublic.bilidownload.activity

import android.os.Bundle
import android.os.Handler
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityWelcomeBinding
import com.sgpublic.bilidownload.util.ConfigManager
import java.util.*

class Welcome: BaseActivity<ActivityWelcomeBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val manager = ConfigManager(this@Welcome)
                if (manager.getInt("quality", -1) == -1){
                    manager.putInt("quality", 80)
                }
                ConfigManager.checkClient(this@Welcome)
            }
        }, 300)
    }

    override fun getContentView(): ActivityWelcomeBinding = ActivityWelcomeBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = false
}