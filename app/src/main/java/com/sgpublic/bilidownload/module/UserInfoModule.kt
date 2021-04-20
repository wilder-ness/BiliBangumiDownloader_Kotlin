package com.sgpublic.bilidownload.module

import android.content.Context
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.UserData
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

class UserInfoModule(context: Context, access_key: String?, mid: Long) {
    private val mid: String
    private var callbackPrivate: Callback? = null
    private val helper: BaseAPI
    private val context: Context
    fun getInfo(callback: Callback?) {
        callbackPrivate = callback
        val call = helper.getUserInfoRequest(mid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate!!.onFailure(-201, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate!!.onFailure(-202, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("data")
                        val data = UserData(
                            json.getString("name"),
                            0,
                            json.getString("face"),
                            json.getString("sign"),
                            json.getInt("level"),
                            json.getJSONObject("label").getString("text"),
                            json.getInt("type"),
                            json.getInt("status")
                        )
                        val sex = json.getString("sex")
                        if (sex == "男") {
                            data.sex = 1
                        } else if (sex == "女") {
                            data.sex = 2
                        } else {
                            data.sex = 0
                        }
                        callbackPrivate!!.onResult(data)
                    } else {
                        callbackPrivate!!.onFailure(-204, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callbackPrivate!!.onFailure(-203, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(data: UserData?)
    }

    companion object {
        private const val TAG = "UserManager"
    }

    init {
        this.mid = mid.toString()
        helper = BaseAPI(access_key)
        this.context = context
    }
}