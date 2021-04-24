package com.sgpublic.bilidownload.module

import android.content.Context
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.TokenData
import com.sgpublic.bilidownload.util.Base64Util
import io.reactivex.annotations.Beta
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

class LoginModule(private val context: Context) {
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var cookie: String
    private lateinit var userAgent: String
    private lateinit var hash: String
    private lateinit var publicKey: String
    private lateinit var callbackPrivate: Callback
    private val helper: BaseAPI = BaseAPI()

    fun loginInAccount(username: String, password: String, callback: Callback) {
        this.username = username
        this.password = password
        this.callbackPrivate = callback
        this.getPublicKey()
    }

    private fun getPublicKey(){
        val call = helper.getKeyRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(
                            -101,
                            context.getString(R.string.error_network),
                            e
                    )
                } else {
                    callbackPrivate.onFailure(-102, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("data")
                        hash = json.getString("hash")
                        this@LoginModule.publicKey = json.getString("key")
                        postAccount()
                    } else {
                        callbackPrivate.onFailure(-104, null, null)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-103, null, e)
                }
            }
        })
    }

    private fun postAccount() {
        this.publicKey = this.publicKey.replace("\n", "").substring(26, 242)
        val keySpec = X509EncodedKeySpec(Base64Util.decode(this.publicKey))
        val keyFactory = KeyFactory.getInstance("RSA")
        val pubKey = keyFactory.generatePublic(keySpec)
        val cp = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cp.init(Cipher.ENCRYPT_MODE, pubKey)
        var passwordEncrypted = Base64Util.encode(cp.doFinal((hash + password).toByteArray()))
        passwordEncrypted = URLEncoder.encode(passwordEncrypted, "UTF-8")
        val call = helper.getLoginRequest(username, passwordEncrypted)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(
                        -121,
                        context.getString(R.string.error_network),
                        null
                    )
                } else {
                    callbackPrivate.onFailure(-122, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("data")
                        if (json.getInt("status") == 0) {
                            json = json.getJSONObject("token_info")
                            val token = TokenData()
                            token.accessToken = json.getString("access_token")
                            token.refreshToken = json.getString("refresh_token")
                            token.expiresIn = json.getLong("expires_in") * 1000L + BaseAPI.ts.toLong()
                            callbackPrivate.onResult(token, json.getLong("mid"))
                        } else if (json.getInt("status") == 3 || json.getInt("status") == 2) {
                            callbackPrivate.onLimited()
                        } else {
                            callbackPrivate.onFailure(-126, null, null)
                        }
                    } else {
                        callbackPrivate.onFailure(-124, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-123, null, e)
                }
            }
        })
    }

    fun loginInWeb(cookie: String, user_agent: String, callback: Callback) {
        this.cookie = cookie
        this.userAgent = user_agent
        this.callbackPrivate = callback
        this.getConfirmUri()
    }

    private fun getConfirmUri(){
        val call = helper.getLoginWebRequest(cookie, userAgent)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-131, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-132, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    val json = JSONObject(result)
                    val confirmUri: String =
                            json.getJSONObject("data").getString("confirm_uri")
                    getAccessKey(confirmUri)
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-133, null, e)
                }
            }
        })
    }

    private fun getAccessKey(confirm_uri: String) {
        val call = helper.getLoginConfirmRequest(confirm_uri, cookie, userAgent)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-141, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-142, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val location = response.header("Location")
                if (location != null && location != "") {
                    if (location.startsWith("http://link.acg.tv/forum.php")) {
                        val urlSplit = location.split("&").toTypedArray()
                        val accessKey = urlSplit[0].substring(40)
                        val mid = urlSplit[1].substring(4).toLong()
                        val token = TokenData()
                        token.accessToken = accessKey
                        token.refreshToken = ""
                        token.expiresIn = 2592000000L + BaseAPI.ts.toLong()
                        callbackPrivate.onResult(token, mid)
                    } else {
                        callbackPrivate.onFailure(-144, null, null)
                    }
                } else {
                    callbackPrivate.onFailure(-143, null, null)
                }
            }
        })
    }

    @Beta
    fun refreshToken(access_token: String, refresh_token: String, callback: Callback) {
        callbackPrivate = callback
        val helper = BaseAPI(access_token)
        val call = helper.getRefreshTokenRequest(refresh_token)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-151, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-152, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("data")
                        val token = TokenData()
                        token.accessToken = json.getString("access_token")
                        token.refreshToken = json.getString("refresh_token")
                        token.expiresIn =
                            json.getLong("expires_in") * 1000L + BaseAPI.ts
                                .toLong()
                        callbackPrivate.onResult(token, json.getLong("mid"))
                    } else {
                        callbackPrivate.onFailure(-154, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-153, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onLimited()
        fun onResult(token: TokenData, mid: Long)
    }
}