package com.sgpublic.bilidownload.module

import android.content.Context
import android.graphics.Color
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.FollowData
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import kotlin.collections.ArrayList

class FollowsModule(private val context: Context, access_key: String) {
    private lateinit var callbackPrivate: Callback
    private val helper: BaseAPI = BaseAPI(access_key)

    fun getFollows(mid: Long, page_index: Int, callback: Callback) {
        getFollows(mid, page_index, 2, callback)
    }

    fun getFollows(mid: Long, page_index: Int, status: Int, callback: Callback) {
        callbackPrivate = callback
        val call = helper.getFollowsRequest(mid, page_index, status)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-301, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-302, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("result")
                        val hasNext = json.getInt("has_next")
                        val total = json.getInt("total")
                        val followDataArray = ArrayList<FollowData>()
                        if (total > 0) {
                            val array = json.getJSONArray("follow_list")
                            val totalPage = array.length()
                            for (followListIndex in 0 until totalPage) {
                                json = array.getJSONObject(followListIndex)
                                val followData = FollowData()
                                followData.seasonId = json.getLong("season_id")
                                followData.title = json.getString("title")
                                followData.cover = json.getString("cover")
                                followData.isFinish = json.getInt("is_finish")
                                val badge = json.getJSONObject("badge_info")
                                followData.badge = badge.getString("text")
                                followData.badgeColor = Color.parseColor(
                                    badge.getString("bg_color")
                                )
                                followData.badgeColorNight = Color.parseColor(
                                    badge.getString("bg_color_night")
                                )
                                followData.squareCover = json.getString("square_cover")
                                json = json.getJSONObject("new_ep")
                                followData.newEpId = json.getLong("id")
                                followData.newEpIsNew = json.getInt("is_new")
                                followData.newEpIndexShow = json.getString("index_show")
                                followData.newEpCover = json.getString("cover")
                                followDataArray.add(followData)
                            }
                        }
                        callbackPrivate.onResult(followDataArray, hasNext)
                    } else {
                        callbackPrivate.onFailure(-304, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-303, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(followData: ArrayList<FollowData>, hasNext: Int)
    }
}