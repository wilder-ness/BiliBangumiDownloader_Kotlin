package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityAboutBinding

class About: BaseActivity<ActivityAboutBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, About::class.java)
            context.startActivity(intent)
        }
    }

    override fun onSetSwipeBackEnable(): Boolean = true
}