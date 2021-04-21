package com.sgpublic.bilidownload.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityLoginBinding
import com.sgpublic.bilidownload.manager.ConfigManager

class Login: BaseActivity<ActivityLoginBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        ConfigManager.putBoolean("is_login", false)

    }

    private fun onLoginAction(){

    }

    private fun setLoadState(is_loading: Boolean) {
        runOnUiThread {
            binding.loginUsername.isEnabled = !is_loading
            binding.loginPassword.isEnabled = !is_loading
            binding.loginAction.isEnabled = !is_loading
            binding.loginDoing.visibility = if (is_loading) View.VISIBLE else View.GONE
            binding.loginAction.setTextColor(if (is_loading) Color.GRAY else this@Login.getColor(R.color.colorAccent))
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()
        binding.loginPassword.addTextChangedListener {
            if (it == null){
                return@addTextChangedListener
            }
            val images = if (it.toString() == ""){
                intArrayOf(R.drawable.pic_login_banner_left_hide, R.drawable.pic_login_banner_right_hide)
            } else {
                intArrayOf(R.drawable.pic_login_banner_left_show, R.drawable.pic_login_banner_right_show)
            }
            binding.loginBannerLeft.setImageResource(images[0])
            binding.loginBannerRight.setImageResource(images[1])
        }
        binding.loginInWeb.setOnClickListener {
            ExceptionDialog.startActivity(this@Login, "logContent")
//            LoginWeb.startActivity(this@Login)
        }
    }

    override fun getContentView(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}