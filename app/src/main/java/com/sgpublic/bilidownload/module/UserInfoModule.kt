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

class UserInfoModule(private val context: Context, accessKey: String, mid: Long) {
    private val mid: String = mid.toString()
    private lateinit var callbackPrivate: Callback
    private val helper: BaseAPI = BaseAPI(accessKey)

    fun getInfo(callback: Callback) {
        callbackPrivate = callback
        val call = helper.getUserInfoRequest(mid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-201, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-202, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("data")
                        val data = UserData()
                        data.face = json.getString("face")
                        data.level = json.getInt("level")
                        data.name = json.getString("name")

                        val sex: String = json.getString("sex")
                        if (sex == "男") {
                            data.sex = 1
                        } else if (sex == "女") {
                            data.sex = 2
                        } else {
                            data.sex = 0
                        }

                        data.sign = json.getString("sign")

                        json = json.getJSONObject("vip")
                        data.vip_label = json.getJSONObject("label").getString("text")
                        data.vip_type = json.getInt("type")
                        data.vip_state = json.getInt("status")

                        callbackPrivate.onResult(data)
                    } else {
                        callbackPrivate.onFailure(-204, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callbackPrivate.onFailure(-203, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(data: UserData?)
    }
}