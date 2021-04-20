package com.sgpublic.bilidownload.module

import android.content.Context
import android.util.Log
import com.sgpublic.bilidownload.util.Base64Util
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

class LoginModule(private val context: Context) {
    private var username: String? = null
    private var password: String? = null
    private var cookie: String? = null
    private var user_agent: String? = null
    private var hash: String? = null
    private var public_key = ""
    private var callback_private: Callback? = null
    private val helper: BaseAPI
    fun loginInAccount(username: String?, password: String?, callback: Callback?) {
        this.username = username
        this.password = password
        callback_private = callback
        publicKey
    }

    private val publicKey: Unit
        private get() {
            val call = helper.keyRequest
            call!!.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback_private!!.onFailure(
                            -101,
                            context.getString(R.string.error_network),
                            e
                        )
                    } else {
                        callback_private!!.onFailure(-102, e.message, e)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val result = Objects.requireNonNull(response.body)!!.string()
                    try {
                        var `object` = JSONObject(result)
                        if (`object`.getInt("code") == 0) {
                            `object` = `object`.getJSONObject("data")
                            hash = `object`.getString("hash")
                            public_key = `object`.getString("key")
                            postAccount()
                        } else {
                            callback_private!!.onFailure(-104, null, null)
                        }
                    } catch (e: JSONException) {
                        callback_private!!.onFailure(-103, null, e)
                    }
                }
            })
        }

    private fun postAccount() {
        var password_encrypted: String? = ""
        public_key = public_key.replace("\n", "").substring(26, 242)
        try {
            val keySpec = X509EncodedKeySpec(Base64Util.Decode(public_key))
            val keyFactory = KeyFactory.getInstance("RSA")
            val pubKey = keyFactory.generatePublic(keySpec)
            val cp = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cp.init(Cipher.ENCRYPT_MODE, pubKey)
            password_encrypted = Base64Util.Encode(cp.doFinal(hash + password!!.toByteArray()))
            password_encrypted = URLEncoder.encode(password_encrypted, "UTF-8")
        } catch (e: Exception) {
            callback_private!!.onFailure(-125, e.message, e)
            e.printStackTrace()
        }
        val call = helper.getLoginRequest(username, password_encrypted)
        call!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(
                        -121,
                        context.getString(R.string.error_network),
                        null
                    )
                } else {
                    callback_private!!.onFailure(-122, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var `object` = JSONObject(result)
                    if (`object`.getInt("code") == 0) {
                        `object` = `object`.getJSONObject("data")
                        if (`object`.getInt("status") == 0) {
                            `object` = `object`.getJSONObject("token_info")
                            val token = TokenData()
                            token.access_token = `object`.getString("access_token")
                            token.refresh_token = `object`.getString("refresh_token")
                            token.expires_in =
                                `object`.getLong("expires_in") * 1000L + BaseAPI.Companion.getTS()
                                    .toLong()
                            callback_private!!.onResult(token, `object`.getLong("mid"))
                        } else if (`object`.getInt("status") == 3 || `object`.getInt("status") == 2) {
                            callback_private!!.onLimited()
                        } else {
                            callback_private!!.onFailure(-126, null, null)
                        }
                    } else {
                        callback_private!!.onFailure(-124, `object`.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-123, null, e)
                }
            }
        })
    }

    fun loginInWeb(cookie: String?, user_agent: String?, callback: Callback?) {
        this.cookie = cookie
        this.user_agent = user_agent
        callback_private = callback
        confirmUri
    }

    private val confirmUri: Unit
        private get() {
            val call = helper.getLoginWebRequest(cookie, user_agent)
            call!!.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback_private!!.onFailure(
                            -131,
                            context.getString(R.string.error_network),
                            e
                        )
                    } else {
                        callback_private!!.onFailure(-132, e.message, e)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val result = Objects.requireNonNull(response.body)!!.string()
                    try {
                        val `object` = JSONObject(result)
                        val confirm_uri: String =
                            `object`.getJSONObject("data").getString("confirm_uri")
                        getAccessKey(confirm_uri)
                    } catch (e: JSONException) {
                        callback_private!!.onFailure(-133, null, e)
                    }
                }
            })
        }

    private fun getAccessKey(confirm_uri: String) {
        val call = helper.getLoginConfirmRequest(confirm_uri, cookie, user_agent)
        call!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-141, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-142, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val location = response.header("Location")
                if (location != null && location != "") {
                    if (location.startsWith("http://link.acg.tv/forum.php")) {
                        val url_split = location.split("&").toTypedArray()
                        val access_key = url_split[0].substring(40)
                        val mid = url_split[1].substring(4).toLong()
                        val token = TokenData()
                        token.access_token = access_key
                        token.refresh_token = ""
                        token.expires_in = 2592000000L + BaseAPI.Companion.getTS().toLong()
                        callback_private!!.onResult(token, mid)
                    } else {
                        callback_private!!.onFailure(-144, null, null)
                    }
                } else {
                    callback_private!!.onFailure(-143, null, null)
                }
            }
        })
    }

    fun refreshToken(access_token: String?, refresh_token: String?, callback: Callback?) {
        callback_private = callback
        val helper = BaseAPI(access_token)
        val call = helper.getRefreshTokenRequest(refresh_token)
        call!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-151, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-152, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                Log.d(TAG, result)
                try {
                    var `object` = JSONObject(result)
                    if (`object`.getInt("code") == 0) {
                        `object` = `object`.getJSONObject("data")
                        val token = TokenData()
                        token.access_token = `object`.getString("access_token")
                        token.refresh_token = `object`.getString("refresh_token")
                        token.expires_in =
                            `object`.getLong("expires_in") * 1000L + BaseAPI.Companion.getTS()
                                .toLong()
                        callback_private!!.onResult(token, `object`.getLong("mid"))
                    } else {
                        callback_private!!.onFailure(-154, `object`.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-153, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onLimited()
        fun onResult(token: TokenData?, mid: Long)
    }

    companion object {
        private const val TAG = "LoginHelper"
    }

    init {
        helper = BaseAPI()
    }
}