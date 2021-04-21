package com.sgpublic.bilidownload

import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.util.MyLog

@Suppress("unused")
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.v("APP启动")
        CrashHandler.init(this)
        ConfigManager.init(this)
        DialogX.init(this)
        DialogX.globalStyle = MIUIStyle.style()
    }
}