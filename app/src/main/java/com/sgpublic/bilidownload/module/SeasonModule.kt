package com.sgpublic.bilidownload.module

import android.content.Context
import android.graphics.Color
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.Episode.InfoData
import com.sgpublic.bilidownload.data.Episode.InfoData.Companion.PAYMENT_NORMAL
import com.sgpublic.bilidownload.data.Episode.QualityData
import com.sgpublic.bilidownload.data.SeasonData
import com.sgpublic.bilidownload.data.SeriesData
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class SeasonModule(private val context: Context, access_key: String) {
    private val helper: BaseAPI = BaseAPI(access_key)
    private var sid: Long = 0
    private lateinit var episodeData: ArrayList<InfoData>
    private val seasonData: SeasonData = SeasonData()
    private lateinit var callbackPrivate: Callback
    
    fun getInfoBySid(sid: Long, callback: Callback) {
        this.callbackPrivate = callback
        this.sid = sid
        val call = helper.getSeasonInfoAppRequest(sid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-401, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-402, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        if (json.getInt("code") == -404) {
                            getInfoBySidAppProxy(call)
                        } else {
                            callbackPrivate.onFailure(-404, json.getString("message"), null)
                        }
                    } else {
                        json = json.getJSONObject("result")
                        doParseAppResult(json)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-403, e.message, e)
                }
            }
        })
    }

    fun getInfoBySidWeb(){
        val call = helper.getSeasonInfoWebRequest(sid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(
                            -411,
                            context.getString(R.string.error_network),
                            e
                    )
                } else {
                    callbackPrivate.onFailure(-412, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        if (json.getInt("code") == -404) {
                            getInfoBySidWebProxy(call)
                        } else {
                            callbackPrivate.onFailure(
                                    -414,
                                    json.getString("message"),
                                    null
                            )
                        }
                    } else {
                        json = json.getJSONObject("result")
                        doParseWebResult(json)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-413, e.message, e)
                }
            }
        })
    }

    private fun getInfoBySidAppProxy(call: Call) {
        val helper = BaseAPI()
        val callProxy = helper.getProxyRequest_iill(call)
        callProxy.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-421, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-422, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        callbackPrivate.onFailure(-424, json.getString("message"), null)
                    } else {
                        json = json.getJSONObject("result")
                        doParseAppResult(json)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-423, e.message, e)
                }
            }
        })
    }

    private fun getInfoBySidWebProxy(call: Call) {
        val helper = BaseAPI()
        val callProxy = helper.getProxyRequest_iill(call)
        callProxy.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-431, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-432, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        callbackPrivate.onFailure(-434, json.getString("message"), null)
                    } else {
                        json = json.getJSONObject("result")
                        doParseWebResult(json)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-433, e.message, e)
                }
            }
        })
    }

    @Throws(JSONException::class)
    private fun doParseAppResult(json: JSONObject) {
        seasonData.actors = json.getJSONObject("actor").getString("info")
        seasonData.actors_lines = seasonData.actors.split("\n").toTypedArray().size
        seasonData.alias = json.getString("alias")
        seasonData.season_type = json.getInt("type")
        var array: JSONArray = json.getJSONArray("seasons")
        val list: ArrayList<SeriesData> = ArrayList<SeriesData>()
        for (array_index in 0 until array.length()) {
            val objectIndex: JSONObject = array.getJSONObject(array_index)
            val seriesData = SeriesData()
            seriesData.badge = objectIndex.getString("badge")
            val objectIndexBadgeInfo: JSONObject = objectIndex.getJSONObject("badge_info")
            seriesData.badge_color = Color.parseColor(
                objectIndexBadgeInfo.getString("bg_color")
            )
            seriesData.badge_color_night = Color.parseColor(
                objectIndexBadgeInfo.getString("bg_color_night")
            )
            seriesData.cover = objectIndex.getString("cover")
            seriesData.title = objectIndex.getString("title")
            seriesData.season_id = objectIndex.getLong("season_id")
            if (seriesData.season_id != sid) {
                list.add(seriesData)
            } else {
                seriesData.season_type_name = json.getString("type_name")
                seasonData.base_info = seriesData
            }
        }
        seasonData.series = list
        if (!json.isNull("limit") && seasonData.area == AREA_LOCAL) {
            seasonData.area = AREA_LIMITED
        }
        val description = StringBuilder()
        val arrayAreas: JSONArray = json.getJSONArray("areas")
        description.append("番剧 | ")
        for (areas_index in 0 until arrayAreas.length()) {
            if (areas_index != 0) {
                description.append("、")
            }
            description.append(arrayAreas.getJSONObject(areas_index).getString("name"))
        }
        description.append("\n")
        description.append(json.getJSONObject("publish").getString("release_date_show"))
        description.append("\n")
        description.append(json.getJSONObject("publish").getString("time_length_show"))
        seasonData.description = description.toString()
        seasonData.evaluate = json.getString("evaluate")
        seasonData.staff = json.getJSONObject("staff").getString("info")
        seasonData.staff_lines = seasonData.staff.split("\n").toTypedArray().size
        val styles = StringBuilder()
        val arrayStyles: JSONArray = json.getJSONArray("styles")
        description.append("番剧 | ")
        for (styles_index in 0 until arrayStyles.length()) {
            if (styles_index != 0) {
                styles.append("、")
            }
            styles.append(arrayStyles.getJSONObject(styles_index).getString("name"))
        }
        seasonData.styles = styles.toString()
        seasonData.rating = if (json.isNull("rating")) 0.0 else json.getJSONObject("rating")
            .getDouble("score")
        if (json.isNull("limit")) {
            array = json.getJSONArray("episodes")
            episodeData = getEpisodesInfo(array)
            this.getAvailableQuality()
        } else {
            this.getInfoBySidWeb()
        }
    }

    fun getAvailableQuality(){
        if (episodeData.size > 0) {
            val call = helper.getEpisodeOfficialRequest(episodeData[0].cid, 112)
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callbackPrivate.onFailure(
                                -441,
                                context.getString(R.string.error_network),
                                e
                        )
                    } else {
                        callbackPrivate.onFailure(-442, e.message, e)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val result = response.body?.string().toString()
                    try {
                        val json = JSONObject(result)
                        if (json.getInt("code") != 0) {
                            if (json.getInt("code") == -10403 && episodeData[0].payment == PAYMENT_NORMAL) {
                                seasonData.area = AREA_LIMITED
                                this@SeasonModule.getAvailableQualityBiliplus()
                                return
                            }
                            callbackPrivate.onFailure(
                                    -444,
                                    json.getString("message"),
                                    null
                            )
                        } else {
                            seasonData.qualities = getEpisodeQuality(json)
                            callbackPrivate.onResult(episodeData, seasonData)
                        }
                    } catch (e: JSONException) {
                        callbackPrivate.onFailure(-443, null, e)
                    }
                }
            })
        } else {
            callbackPrivate.onResult(episodeData, seasonData)
        }
    }

    fun getAvailableQualityBiliplus(){
        val call = helper.getEpisodeBiliplusRequest(episodeData[0].cid, 112)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-451, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-452, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 504) {
                    this@SeasonModule.getAvailableQualityKghost()
                } else {
                    val result = response.body?.string().toString()
                    try {
                        val json = JSONObject(result)
                        if (json.getInt("code") != 0) {
                            callbackPrivate.onFailure(
                                    -454,
                                    json.getString("message"),
                                    null
                            )
                        } else {
                            seasonData.qualities = getEpisodeQuality(json)
                            callbackPrivate.onResult(episodeData, seasonData)
                        }
                    } catch (e: JSONException) {
                        callbackPrivate.onFailure(-453, null, e)
                    }
                }
            }
        })
    }

    fun getAvailableQualityKghost(){
        val call = helper.getEpisodeKghostRequest(episodeData[0].cid, 112)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(
                            -461,
                            context.getString(R.string.error_network),
                            e
                    )
                } else {
                    callbackPrivate.onFailure(-462, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        callbackPrivate.onFailure(-524, json.getString("message"), null)
                    } else {
                        json = json.getJSONObject("result")
                        seasonData.qualities = getEpisodeQuality(json)
                        callbackPrivate.onResult(episodeData, seasonData)
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-523, null, e)
                } catch (e: IndexOutOfBoundsException) {
                    callbackPrivate.onFailure(-524, null, e)
                }
            }
        })
    }

    @Throws(JSONException::class)
    private fun getEpisodeQuality(json: JSONObject): ArrayList<QualityData> {
        val arrayList: ArrayList<QualityData> = ArrayList<QualityData>()
        if (json.isNull("support_formats")) {
            val acceptDescription: JSONArray = json.getJSONArray("accept_description")
            val acceptFormat: Array<String> =
                json.getString("accept_format").split(",").toTypedArray()
            val acceptQuality: JSONArray = json.getJSONArray("accept_quality")
            for (i in 0 until acceptDescription.length()) {
                arrayList.add(
                    QualityData(acceptQuality.getInt(i), acceptDescription.getString(i), acceptFormat[i])
                )
            }
        } else {
            val supportFormats: JSONArray = json.getJSONArray("support_formats")
            for (array_index in 0 until supportFormats.length()) {
                val supportFormat: JSONObject = supportFormats.getJSONObject(array_index)
                val newDescription: String = supportFormat.getString("new_description")
                val acceptQuality: Int = supportFormat.getInt("quality")
                val acceptFormat: String = supportFormat.getString("format")
                arrayList.add(QualityData(acceptQuality, newDescription, acceptFormat))
            }
        }
        return arrayList
    }

    @Throws(JSONException::class)
    private fun doParseWebResult(json: JSONObject) {
        val array: JSONArray = json.getJSONArray("episodes")
        episodeData = getEpisodesInfo(array)
        this.getAvailableQuality()
    }

    @Deprecated("")
    @Throws(JSONException::class)
    private fun doParseOldResult(json: JSONObject) {
        val array: JSONArray = json.getJSONArray("episodes")
        episodeData = getEpisodesInfo(array)
        this.getAvailableQuality()
    }

    @Throws(JSONException::class)
    private fun getEpisodesInfo(array: JSONArray): ArrayList<InfoData> {
        val episodeData: ArrayList<InfoData> = ArrayList<InfoData>()
        for (episodes_index in 0 until array.length()) {
            val episodeDataIndex = InfoData()
            val objectEpisodesIndex: JSONObject = array.getJSONObject(episodes_index)
            episodeDataIndex.aid = objectEpisodesIndex.getLong("aid")
            episodeDataIndex.cid = objectEpisodesIndex.getLong("cid")
            episodeDataIndex.ep_id = objectEpisodesIndex.getLong("id")
            episodeDataIndex.cover = objectEpisodesIndex.getString("cover")
            episodeDataIndex.payment = objectEpisodesIndex.getInt("status")
            episodeDataIndex.bvid = objectEpisodesIndex.getString("bvid")
            episodeDataIndex.area_limit = objectEpisodesIndex.getInt("status")
            val objectEpisodesIndexBadge: JSONObject =
                objectEpisodesIndex.getJSONObject("badge_info")
            episodeDataIndex.badge = objectEpisodesIndexBadge.getString("text")
            episodeDataIndex.badge_color = Color.parseColor(
                objectEpisodesIndexBadge.getString("bg_color")
            )
            episodeDataIndex.badge_color_night = Color.parseColor(
                objectEpisodesIndexBadge.getString("bg_color_night")
            )
            val dateFormat = SimpleDateFormat.getDateInstance()
            episodeDataIndex.pub_real_time = dateFormat.format(Date(
                    objectEpisodesIndex.getLong("pub_time") * 1000L
            ))
            episodeDataIndex.title = objectEpisodesIndex.getString("long_title")
            episodeDataIndex.index = objectEpisodesIndex.getString("title")
            episodeData.add(episodeDataIndex)
        }
        return episodeData
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(episodeData: ArrayList<InfoData>, seasonData: SeasonData)
    }

    companion object {
        const val AREA_LOCAL = 0
        const val AREA_LIMITED = 1
    }
}