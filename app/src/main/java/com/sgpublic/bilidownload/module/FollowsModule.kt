package com.sgpublic.bilidownload.module

import android.content.Context
import android.graphics.Color
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.FollowData
import com.sgpublic.bilidownload.util.MyLog
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

class FollowsModule(context: Context, access_key: String?) {
    private var callback_private: Callback? = null
    private val helper: BaseAPI
    private val context: Context
    fun getFollows(mid: Long, page_index: Int, callback: Callback?) {
        getFollows(mid, page_index, 2, callback)
    }

    fun getFollows(mid: Long, page_index: Int, status: Int, callback: Callback?) {
        callback_private = callback
        val call = helper.getFollowsRequest(mid, page_index, status)
        call!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-301, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-302, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var `object` = JSONObject(result)
                    if (`object`.getInt("code") == 0) {
                        `object` = `object`.getJSONObject("result")
                        val has_next = `object`.getInt("has_next")
                        val total = `object`.getInt("total")
                        val followDataArray: Array<FollowData?>
                        if (total == 0) {
                            followDataArray = arrayOfNulls(0)
                        } else {
                            val array = `object`.getJSONArray("follow_list")
                            val total_page = array.length()
                            followDataArray = arrayOfNulls(total_page)
                            for (follow_list_index in 0 until total_page) {
                                `object` = array.getJSONObject(follow_list_index)
                                MyLog.v(FollowsModule::class.java, `object`)
                                val followData = FollowData()
                                followData.season_id = `object`.getLong("season_id")
                                followData.title = `object`.getString("title")
                                followData.cover = `object`.getString("cover")
                                followData.is_finish = `object`.getInt("is_finish")
                                val badge = `object`.getJSONObject("badge_info")
                                followData.badge = badge.getString("text")
                                followData.badge_color = Color.parseColor(
                                    badge.getString("bg_color")
                                )
                                followData.badge_color_night = Color.parseColor(
                                    badge.getString("bg_color_night")
                                )
                                followData.square_cover = `object`.getString("square_cover")
                                `object` = `object`.getJSONObject("new_ep")
                                followData.new_ep_id = `object`.getLong("id")
                                followData.new_ep_is_new = `object`.getInt("is_new")
                                followData.new_ep_index_show = `object`.getString("index_show")
                                followData.new_ep_cover = `object`.getString("cover")
                                followDataArray[follow_list_index] = followData
                            }
                        }
                        callback_private!!.onResult(followDataArray, has_next)
                    } else {
                        callback_private!!.onFailure(-304, `object`.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-303, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(followData: Array<FollowData?>?, has_next: Int)
    }

    companion object {
        private const val TAG = "FollowsHelper"
    }

    init {
        helper = BaseAPI(access_key)
        this.context = context
    }
}