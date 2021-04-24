package com.sgpublic.bilidownload.activity

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.data.TokenData
import com.sgpublic.bilidownload.data.UserData
import com.sgpublic.bilidownload.databinding.ActivityWelcomeBinding
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.module.LoginModule
import com.sgpublic.bilidownload.module.UpdateModule
import com.sgpublic.bilidownload.module.UserInfoModule
import com.sgpublic.bilidownload.util.ActivityCollector
import java.text.SimpleDateFormat
import java.util.*

class Welcome: BaseActivity<ActivityWelcomeBinding>(), UpdateModule.Callback {
    private lateinit var activityIntent: Intent

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (ConfigManager.getInt("quality", -1) == -1) {
                    ConfigManager.putInt("quality", 80)
                }
                ConfigManager.checkClient()

                if (!ConfigManager.getBoolean("is_login")) {
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            onSetupFinished(false)
                        }
                    }, 800)
                } else if (ConfigManager.getLong("expires_in", 0L) > System.currentTimeMillis()) {
                    refreshUserInfo()
                } else {
                    val refreshKey: String = ConfigManager.getString("refresh_key")
                    val expired = object : TimerTask() {
                        override fun run() {
                            onToast(R.string.error_login_refresh)
                            onSetupFinished(false)
                        }
                    }
                    if (refreshKey == "") {
                        Timer().schedule(expired, 100)
                        return
                    }
                    val accessToken = ConfigManager.getString("access_token")
                    val helper = LoginModule(this@Welcome)
                    helper.refreshToken(accessToken, refreshKey, object : LoginModule.Callback {
                        override fun onFailure(code: Int, message: String?, e: Throwable?) {
                            Timer().schedule(expired, 200)
                        }

                        override fun onLimited() {
                            Timer().schedule(expired, 200)
                        }

                        override fun onResult(token: TokenData, mid: Long) {
                            ConfigManager.putString("access_key", token.accessToken)
                            ConfigManager.putString("refresh_key", token.refreshToken)
                            ConfigManager.putLong("mid", mid)
                            ConfigManager.putLong("expires_in", token.expiresIn)
                            refreshUserInfo()
                        }
                    })
                }
            }
        }, 300)
    }

    override fun onViewSetup() {

    }

    private fun refreshUserInfo(){
        val userInfoModule = UserInfoModule(
                this@Welcome,
                ConfigManager.getString("access_key", ""),
                ConfigManager.getLong("mid", 0L)
        )
        userInfoModule.getInfo(object : UserInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_login)
                onSetupFinished(false)
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(data: UserData) {
                ConfigManager.putString("name", data.name)
                ConfigManager.putString("sign", data.sign)
                ConfigManager.putString("face", data.face)
                ConfigManager.putInt("sex", data.sex)
                ConfigManager.putString("vip_label", data.vipLabel)
                ConfigManager.putInt("vip_type", data.vipType)
                ConfigManager.putInt("vip_state", data.vipState)
                ConfigManager.putInt("level", data.level)
                ConfigManager.putBoolean("is_login", true)
                onSetupFinished(true)
            }
        })
    }


    private fun onSetupFinished(is_login: Boolean) {
        activityIntent = if (is_login) {
            Intent(this@Welcome, Main::class.java)
        } else {
            Intent(this@Welcome, Login::class.java)
        }
        val helper = UpdateModule(this@Welcome)
        helper.getUpdate(0, this)
    }

    override fun onFailure(code: Int, message: String?, e: Throwable) {
        CrashHandler.saveExplosion(e, code)
        runOnUiThread { this@Welcome.startActivity(activityIntent) }
    }

    override fun onUpToDate() {
        runOnUiThread { this@Welcome.startActivity(activityIntent) }
    }

    override fun onUpdate(force: Int, verName: String, sizeString: String, changelog: String, dlUrl: String) {
        val updateHeader = intArrayOf(
                R.string.text_update_content,
                R.string.text_update_content_force,
                R.string.text_update_content_beta
        )
        val builder = AlertDialog.Builder(this@Welcome)
        builder.setTitle(R.string.title_update_get)
        builder.setCancelable(force == 0)
        var header = updateHeader[force]
        if (verName.contains("Build")) {
            header = updateHeader[2]
        }
        val message = String.format(this@Welcome.getString(header), sizeString) +
                this@Welcome.getString(R.string.text_update_version) +
                verName + this@Welcome.getString(R.string.text_update_changelog) +
                changelog
        builder.setMessage(message)
        builder.setPositiveButton(R.string.text_ok) { _, _ ->
            Thread {
                val url = Uri.parse(dlUrl)
                val downloadManager = applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val req = DownloadManager.Request(url)
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                val apkName = this@Welcome.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".apk"
                req.setDestinationInExternalFilesDir(applicationContext, Environment.DIRECTORY_DOWNLOADS, apkName)
                req.setNotificationVisibility(View.VISIBLE)
                req.setTitle(this@Welcome.getString(R.string.title_update_download))
                req.setMimeType("application/vnd.android.package-archive")
                val referer = downloadManager.enqueue(req)
                UpdateModule.listener(this@Welcome, referer)
            }.start()
            this@Welcome.startActivity(activityIntent)
        }
        val runnable = Runnable {
            if (force == 1) {
                ActivityCollector.finishAll()
            } else {
                runOnUiThread { this@Welcome.startActivity(activityIntent) }
            }
        }
        builder.setNegativeButton(R.string.text_cancel) { _, _ -> runnable.run() }
        builder.setOnCancelListener { runnable.run() }
        runOnUiThread { builder.show() }
    }

    override fun onDisabled(time: Long, reason: String) {
        val builder = AlertDialog.Builder(this@Welcome)
        builder.setTitle(R.string.title_update_disable)
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)
        val date = Date()
        date.time = time
        builder.setMessage(String.format(
                this@Welcome.getString(R.string.text_update_content_disable),
                reason, sdf.format(date)
        ))
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.text_ok) { _, _ -> ActivityCollector.finishAll() }
        runOnUiThread { builder.show() }
    }

    override fun isActivityAtBottom(): Boolean = true
}