package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.base.CrashHandler
import com.sgpublic.bilidownload.data.TokenData
import com.sgpublic.bilidownload.data.UserData
import com.sgpublic.bilidownload.databinding.ActivityLoginBinding
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.module.LoginModule
import com.sgpublic.bilidownload.module.UserInfoModule

class Login: BaseActivity<ActivityLoginBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        ConfigManager.putBoolean("is_login", false)
    }

    private fun onLoginAction(){
        setLoadState(true)
        val username: String = binding.loginUsername.text.toString()
        val password: String = binding.loginPassword.text.toString()
        if (username == "" || password == "") {
            onToast(R.string.text_login_error_empty)
            setLoadState(false)
            return
        }

        val helper = LoginModule(this@Login)
        helper.loginInAccount(username, password, object : LoginModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_login, message, code)
                setLoadState(false)
                CrashHandler.saveExplosion(e, code)
            }

            override fun onLimited() {
                onToast(R.string.error_login_verify)
                val intent = Intent(this@Login, LoginWeb::class.java)
                startActivity(intent)
            }

            override fun onResult(token: TokenData, mid: Long) {
                ConfigManager.putString("access_key", token.accessToken)
                ConfigManager.putString("refresh_key", token.refreshToken)
                ConfigManager.putLong("mid", mid)
                ConfigManager.putLong("expires_in", token.expiresIn)
                val manager = UserInfoModule(this@Login, token.accessToken, mid)
                manager.getInfo(object : UserInfoModule.Callback {
                    override fun onFailure(code: Int, message: String?, e: Throwable?) {
                        onToast(R.string.error_login, message, code)
                        setLoadState(false)
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
                        setLoadState(false)
                        runOnUiThread {
                            onToast(R.string.text_login_success)
                            Main.startActivity(this@Login)
                        }
                    }
                })
            }
        })
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
        binding.loginPassword.addTextChangedListener {
            if (it == null){
                return@addTextChangedListener
            }
            val images = if (it.toString() == ""){
                intArrayOf(
                    R.drawable.pic_login_banner_left_hide,
                    R.drawable.pic_login_banner_right_hide
                )
            } else {
                intArrayOf(
                    R.drawable.pic_login_banner_left_show,
                    R.drawable.pic_login_banner_right_show
                )
            }
            binding.loginBannerLeft.setImageResource(images[0])
            binding.loginBannerRight.setImageResource(images[1])
        }
        binding.loginInWeb.setOnClickListener {
            LoginWeb.startActivity(this@Login)
        }
        binding.loginAction.setOnClickListener {
            onLoginAction()
        }
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Login::class.java)
            context.startActivity(intent)
        }
    }

    override fun isActivityAtBottom(): Boolean = true

}