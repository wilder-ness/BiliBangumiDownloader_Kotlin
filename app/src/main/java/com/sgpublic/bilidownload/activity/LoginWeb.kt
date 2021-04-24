package com.sgpublic.bilidownload.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.data.TokenData
import com.sgpublic.bilidownload.data.UserData
import com.sgpublic.bilidownload.databinding.ActivityLoginWebBinding
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.module.LoginModule
import com.sgpublic.bilidownload.module.UserInfoModule
import java.util.*

class LoginWeb: BaseActivity<ActivityLoginWebBinding>(), LoginModule.Callback {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val manager = CookieManager.getInstance()
        if ("" != manager.getCookie(login_url)){
            manager.removeAllCookies(null)
        }
        val settings = binding.loginWebView.settings
        settings.javaScriptEnabled = true
        binding.loginWebView.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                startOnLoadingState()
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (url == null){
                    super.onPageFinished(view, url)
                    return
                }
                if (url != bilibili_host && url != bilibili_passport){
                    stopOnLoadingState()
                    super.onPageFinished(view, url)
                    return
                }
                binding.loginWebView.stopLoading()
                val webCookie = CookieManager.getInstance().getCookie(url)
                val webUserAgent = settings.userAgentString
                LoginModule(this@LoginWeb).loginInWeb(webCookie, webUserAgent, this@LoginWeb)
            }
        }
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        onToast(R.string.error_login, code)
        stopOnLoadingState()
        finish()
    }

    override fun onLimited() { }

    override fun onResult(token: TokenData, mid: Long) {
        ConfigManager.putString("access_key", token.accessToken)
        ConfigManager.putLong("mid", mid)
        ConfigManager.putLong("expires_in", token.expiresIn)
        val module = UserInfoModule(this@LoginWeb, token.accessToken, mid)
        module.getInfo(object : UserInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_login, message, code)
                stopOnLoadingState()
                CrashHandler.saveExplosion(e, code)
                finish()
            }

            override fun onResult(data: UserData) {
                ConfigManager.putString("name", data.name)
                ConfigManager.putString("sign", data.sign)
                ConfigManager.putString("face", data.face)
                ConfigManager.putInt("sex", data.sex)
                ConfigManager.putInt("vip_type", data.vipType)
                ConfigManager.putInt("vip_state", data.vipState)
                ConfigManager.putInt("level", data.level)
                ConfigManager.putBoolean("is_login", true)
                stopOnLoadingState()
                onToast(R.string.text_login_success)
                runOnUiThread {
                    Main.startActivity(this@LoginWeb)
                }
            }
        })
    }

    override fun onViewSetup() {
        setSupportActionBar(binding.loginWebToolbar)
        supportActionBar?.run {
            setTitle(R.string.title_login_web)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private var timer: Timer? = null
    private var imageIndex = 0
    private fun startOnLoadingState() {
        binding.loginWebView.visibility = View.INVISIBLE
        binding.loginLoadState.visibility = View.VISIBLE
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                imageIndex =
                    if (imageIndex == R.drawable.pic_search_doing_1) R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                runOnUiThread { binding.loginLoadState.setImageResource(imageIndex) }
            }
        }, 0, 500)
    }

    private fun stopOnLoadingState() {
        binding.loginWebView.visibility = View.VISIBLE
        binding.loginLoadState.visibility = View.GONE
        timer?.run {
            cancel()
        }
        timer = null
    }

    companion object {
        private const val login_url = "https://passport.bilibili.com/login"
        private const val bilibili_host = "https://m.bilibili.com/index.html"
        private const val bilibili_passport = "https://passport.bilibili.com/account/security#/home"

        fun startActivity(packageContext: Context){
            val intent = Intent(packageContext, LoginWeb::class.java)
            packageContext.startActivity(intent)
        }
    }

}