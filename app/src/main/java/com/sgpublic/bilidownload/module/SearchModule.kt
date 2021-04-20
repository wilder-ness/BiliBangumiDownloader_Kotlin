package com.sgpublic.bilidownload.module

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.SearchData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class SearchModule(private val context: Context) {
    private val helper: BaseAPI = BaseAPI()
    fun getHotWord(callback: HotWordCallback) {
        val call = helper.getHotWordRequest()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-801, null, e)
                } else {
                    callback.onFailure(-802, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val hotWords = ArrayList<String>()
                        val array = json.getJSONArray("list")
                        for (array_index in 0 until array.length()) {
                            json = array.getJSONObject(array_index)
                            hotWords.add(json.getString("keyword"))
                        }
                        callback.onResult(hotWords)
                    } else {
                        callback.onFailure(-814, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-813, null, e)
                }
            }
        })
    }

    fun suggest(keyword: String, callback: SuggestCallback) {
        val call = helper.getSearchSuggestRequest(keyword)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-811, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-812, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val suggestions = ArrayList<Spannable>()
                        try {
                            val array = json.getJSONObject("result").getJSONArray("tag")
                            var array_index = 0
                            while (array_index < 7 && array_index < array.length()) {
                                json = array.getJSONObject(array_index)
                                val value_string = json.getString("value")
                                val value_spannable: Spannable = SpannableString(value_string)
                                for (value_index in 0 until keyword.length) {
                                    val keyword_index =
                                        keyword.substring(value_index).substring(0, 1)
                                    val value_string_sub = value_string.indexOf(keyword_index)
                                    if (value_string_sub >= 0) {
                                        value_spannable.setSpan(
                                            ForegroundColorSpan(context.resources.getColor(R.color.colorPrimary)),
                                            value_string_sub, value_string_sub + 1,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                    }
                                }
                                suggestions.add(value_spannable)
                                array_index++
                            }
                            callback.onResult(suggestions)
                        } catch (e: JSONException) {
                            callback.onFailure(-815, null, null)
                        }
                    } else {
                        callback.onFailure(-814, null, null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-813, null, e)
                }
            }
        })
    }

    fun search(keyword: String, callback: SearchCallback) {
        val call = helper.getSearchResultRequest(keyword)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-821, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-822, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val searchDataList = ArrayList<SearchData>()
                        json = json.getJSONObject("data")
                        if (!json.isNull("result")) {
                            val array = json.getJSONArray("result")
                            for (array_index in 0 until array.length()) {
                                json = array.getJSONObject(array_index)
                                val searchData = SearchData()
                                searchData.angle_title = json.getString("angle_title")
                                searchData.season_cover = "http:" + json.getString("cover")
                                if (json.isNull("media_score")) {
                                    searchData.media_score = 0.0
                                } else {
                                    searchData.media_score = json.getJSONObject("media_score")
                                        .getDouble("score")
                                }
                                searchData.season_id = json.getLong("season_id")
                                //searchData.season_title = object.getString("season_title");
                                val season_title_string = json.getString("title")
                                val season_title_spannable: Spannable = SpannableString(
                                    season_title_string
                                        .replace("<em class=\"keyword\">", "")
                                        .replace("</em>", "")
                                )
                                val season_title_sub_start =
                                    season_title_string.indexOf("<em class=\"keyword\">")
                                val season_title_sub_end = season_title_string
                                    .replace("<em class=\"keyword\">", "")
                                    .indexOf("</em>")
                                if (season_title_sub_start >= 0 && season_title_sub_end >= 0) {
                                    season_title_spannable.setSpan(
                                        ForegroundColorSpan(context.resources.getColor(R.color.colorPrimary)),
                                        season_title_sub_start, season_title_sub_end,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                searchData.season_title = season_title_spannable
                                if (json.getLong("pubtime") * 1000 > System.currentTimeMillis()) {
                                    searchData.selection_style = "grid"
                                } else {
                                    searchData.selection_style =
                                        json.getString("selection_style")
                                }
                                val date = Date(json.getLong("pubtime") * 1000)
                                val format = SimpleDateFormat("yyyy", Locale.CHINA)
                                searchData.season_content = """
                                    ${format.format(date)}｜${json.getString("season_type_name")}｜${
                                    json.getString(
                                        "areas"
                                    )
                                }
                                    ${json.getString("styles")}
                                    """.trimIndent()
                                var eps_array = json.getJSONArray("eps")
                                if (eps_array.length() > 0) {
                                    json = eps_array.getJSONObject(0)
                                    searchData.episode_cover = json.getString("cover")
                                    val episode_title_string = json.getString("long_title")
                                    val episode_title_spannable: Spannable = SpannableString(
                                        episode_title_string
                                            .replace("<em class=\"keyword\">", "")
                                            .replace("</em>", "")
                                    )
                                    val episode_title_sub_start =
                                        episode_title_string.indexOf("<em class=\"keyword\">")
                                    val episode_title_sub_end = episode_title_string
                                        .replace("<em class=\"keyword\">", "")
                                        .indexOf("</em>")
                                    if (episode_title_sub_start >= 0 && episode_title_sub_end >= 0) {
                                        episode_title_spannable.setSpan(
                                            ForegroundColorSpan(context.resources.getColor(R.color.colorPrimary)),
                                            episode_title_sub_start, episode_title_sub_end,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                    }
                                    searchData.episode_title = episode_title_spannable
                                    eps_array = json.getJSONArray("badges")
                                    if (eps_array.length() > 0) {
                                        json = eps_array.getJSONObject(0)
                                        searchData.episode_badges = json.getString("text")
                                    } else {
                                        searchData.episode_badges = ""
                                    }
                                }
                                searchDataList.add(searchData)
                            }
                        }
                        callback.onResult(searchDataList)
                    } else {
                        callback.onFailure(-824, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-823, null, e)
                }
            }
        })
    }

    interface SearchCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(searchData: ArrayList<SearchData>?)
    }

    interface SuggestCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(suggestions: ArrayList<Spannable>?)
    }

    interface HotWordCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(hotWords: ArrayList<String>?)
    }
}