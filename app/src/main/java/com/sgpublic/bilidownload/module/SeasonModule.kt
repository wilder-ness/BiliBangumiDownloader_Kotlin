package com.sgpublic.bilidownload.module

import android.content.Context
import android.graphics.Color
import com.sgpublic.bilidownload.data.Episode.InfoData
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class SeasonModule(private val context: Context, access_key: String?) {
    private val helper: BaseAPI
    private var sid: Long = 0
    private var episodeData: ArrayList<InfoData>? = null
    private val seasonData: SeasonData = SeasonData()
    private var callback_private: Callback? = null
    fun getInfoBySid(sid: Long, callback: Callback?) {
        callback_private = callback
        this.sid = sid
        val call = helper.getSeasonInfoAppRequest(sid)
        call!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-401, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-402, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var `object` = JSONObject(result)
                    if (`object`.getInt("code") != 0) {
                        if (`object`.getInt("code") == -404) {
                            getInfoBySidAppProxy(call)
                        } else {
                            callback_private!!.onFailure(-404, `object`.getString("message"), null)
                        }
                    } else {
                        `object` = `object`.getJSONObject("result")
                        doParseAppResult(`object`)
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-403, e.message, e)
                }
            }
        })
    }

    private val infoBySidWeb: Unit
        private get() {
            val call = helper.getSeasonInfoWebRequest(sid)
            call!!.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback_private!!.onFailure(
                            -411,
                            context.getString(R.string.error_network),
                            e
                        )
                    } else {
                        callback_private!!.onFailure(-412, e.message, e)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val result = Objects.requireNonNull(response.body)!!.string()
                    try {
                        var `object` = JSONObject(result)
                        if (`object`.getInt("code") != 0) {
                            if (`object`.getInt("code") == -404) {
                                getInfoBySidWebProxy(call)
                            } else {
                                callback_private!!.onFailure(
                                    -414,
                                    `object`.getString("message"),
                                    null
                                )
                            }
                        } else {
                            `object` = `object`.getJSONObject("result")
                            doParseWebResult(`object`)
                        }
                    } catch (e: JSONException) {
                        callback_private!!.onFailure(-413, e.message, e)
                    }
                }
            })
        }

    private fun getInfoBySidAppProxy(call: Call) {
        val helper = BaseAPI()
        val call_proxy = helper.getProxyRequest_iill(call)
        call_proxy!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-421, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-422, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var `object` = JSONObject(result)
                    if (`object`.getInt("code") != 0) {
                        callback_private!!.onFailure(-424, `object`.getString("message"), null)
                    } else {
                        `object` = `object`.getJSONObject("result")
                        doParseAppResult(`object`)
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-423, e.message, e)
                }
            }
        })
    }

    private fun getInfoBySidWebProxy(call: Call) {
        val helper = BaseAPI()
        val call_proxy = helper.getProxyRequest_iill(call)
        call_proxy!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-431, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-432, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = Objects.requireNonNull(response.body)!!.string()
                try {
                    var `object` = JSONObject(result)
                    if (`object`.getInt("code") != 0) {
                        callback_private!!.onFailure(-434, `object`.getString("message"), null)
                    } else {
                        `object` = `object`.getJSONObject("result")
                        doParseWebResult(`object`)
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-433, e.message, e)
                }
            }
        })
    }

    @Throws(JSONException::class)
    private fun doParseAppResult(`object`: JSONObject) {
        seasonData.actors = `object`.getJSONObject("actor").getString("info")
        seasonData.actors_lines = seasonData.actors.split("\n").toTypedArray().size
        seasonData.alias = `object`.getString("alias")
        seasonData.season_type = `object`.getInt("type")
        var array: JSONArray = `object`.getJSONArray("seasons")
        val list: ArrayList<SeriesData> = ArrayList<SeriesData>()
        for (array_index in 0 until array.length()) {
            val object_index: JSONObject = array.getJSONObject(array_index)
            val seriesData = SeriesData()
            seriesData.badge = object_index.getString("badge")
            val object_index_badge_info: JSONObject = object_index.getJSONObject("badge_info")
            seriesData.badge_color = Color.parseColor(
                object_index_badge_info.getString("bg_color")
            )
            seriesData.badge_color_night = Color.parseColor(
                object_index_badge_info.getString("bg_color_night")
            )
            seriesData.cover = object_index.getString("cover")
            seriesData.title = object_index.getString("title")
            seriesData.season_id = object_index.getLong("season_id")
            if (seriesData.season_id != sid) {
                list.add(seriesData)
            } else {
                seriesData.season_type_name = `object`.getString("type_name")
                seasonData.base_info = seriesData
            }
        }
        seasonData.series = list
        if (!`object`.isNull("limit") and seasonData.area == AREA_LOCAL) {
            seasonData.area = AREA_LIMITED
        }
        val description = StringBuilder()
        val array_areas: JSONArray = `object`.getJSONArray("areas")
        description.append("番剧 | ")
        for (areas_index in 0 until array_areas.length()) {
            if (areas_index != 0) {
                description.append("、")
            }
            description.append(array_areas.getJSONObject(areas_index).getString("name"))
        }
        description.append("\n")
        description.append(`object`.getJSONObject("publish").getString("release_date_show"))
        description.append("\n")
        description.append(`object`.getJSONObject("publish").getString("time_length_show"))
        seasonData.description = description.toString()
        seasonData.evaluate = `object`.getString("evaluate")
        seasonData.staff = `object`.getJSONObject("staff").getString("info")
        seasonData.staff_lines = seasonData.staff.split("\n").toTypedArray().size
        val styles = StringBuilder()
        val array_styles: JSONArray = `object`.getJSONArray("styles")
        description.append("番剧 | ")
        for (styles_index in 0 until array_styles.length()) {
            if (styles_index != 0) {
                styles.append("、")
            }
            styles.append(array_styles.getJSONObject(styles_index).getString("name"))
        }
        seasonData.styles = styles.toString()
        seasonData.rating = if (`object`.isNull("rating")) 0.0 else `object`.getJSONObject("rating")
            .getDouble("score")
        if (`object`.isNull("limit")) {
            array = `object`.getJSONArray("episodes")
            episodeData = getEpisodesInfo(array)
            availableQuality
        } else {
            infoBySidWeb
        }
    }

    private val availableQuality: Unit
        private get() {
            if (episodeData!!.size > 0) {
                val call = helper.getEpisodeOfficialRequest(episodeData!![0].cid, 112)
                call!!.enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (e is UnknownHostException) {
                            callback_private!!.onFailure(
                                -441,
                                context.getString(R.string.error_network),
                                e
                            )
                        } else {
                            callback_private!!.onFailure(-442, e.message, e)
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        val result = Objects.requireNonNull(response.body)!!
                            .string()
                        try {
                            val `object` = JSONObject(result)
                            if (`object`.getInt("code") != 0) {
                                if (`object`.getInt("code") == -10403 and episodeData!![0].payment === PAYMENT_NORMAL) {
                                    seasonData.area = AREA_LIMITED
                                    availableQualityBiliplus
                                    return
                                }
                                callback_private!!.onFailure(
                                    -444,
                                    `object`.getString("message"),
                                    null
                                )
                            } else {
                                seasonData.qualities = getEpisodeQuality(`object`)
                                callback_private!!.onResult(episodeData, seasonData)
                            }
                        } catch (e: JSONException) {
                            callback_private!!.onFailure(-443, null, e)
                        }
                    }
                })
            } else {
                callback_private!!.onResult(episodeData, seasonData)
            }
        }
    private val availableQualityBiliplus: Unit
        private get() {
            val call = helper.getEpisodeBiliplusRequest(episodeData!![0].cid, 112)
            call!!.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback_private!!.onFailure(
                            -451,
                            context.getString(R.string.error_network),
                            e
                        )
                    } else {
                        callback_private!!.onFailure(-452, null, e)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 504) {
                        availableQualityKghost
                    } else {
                        val result = Objects.requireNonNull(response.body)!!
                            .string()
                        try {
                            val `object` = JSONObject(result)
                            if (`object`.getInt("code") != 0) {
                                callback_private!!.onFailure(
                                    -454,
                                    `object`.getString("message"),
                                    null
                                )
                            } else {
                                seasonData.qualities = getEpisodeQuality(`object`)
                                callback_private!!.onResult(episodeData, seasonData)
                            }
                        } catch (e: JSONException) {
                            callback_private!!.onFailure(-453, null, e)
                        }
                    }
                }
            })
        }
    private val availableQualityKghost: Unit
        private get() {
            val call = helper.getEpisodeKghostRequest(episodeData!![0].cid, 112)
            call!!.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback_private!!.onFailure(
                            -461,
                            context.getString(R.string.error_network),
                            e
                        )
                    } else {
                        callback_private!!.onFailure(-462, null, e)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val result = Objects.requireNonNull(response.body)!!.string()
                    try {
                        var `object` = JSONObject(result)
                        if (`object`.getInt("code") != 0) {
                            callback_private!!.onFailure(-524, `object`.getString("message"), null)
                        } else {
                            `object` = `object`.getJSONObject("result")
                            seasonData.qualities = getEpisodeQuality(`object`)
                            callback_private!!.onResult(episodeData, seasonData)
                        }
                    } catch (e: JSONException) {
                        callback_private!!.onFailure(-523, null, e)
                    } catch (e: IndexOutOfBoundsException) {
                        callback_private!!.onFailure(-524, null, e)
                    }
                }
            })
        }

    @Throws(JSONException::class)
    private fun getEpisodeQuality(`object`: JSONObject): ArrayList<QualityData> {
        val arrayList: ArrayList<QualityData> = ArrayList<QualityData>()
        if (`object`.isNull("support_formats")) {
            val accept_description: JSONArray = `object`.getJSONArray("accept_description")
            val accept_format: Array<String> =
                `object`.getString("accept_format").split(",").toTypedArray()
            val accept_quality: JSONArray = `object`.getJSONArray("accept_quality")
            for (i in 0 until accept_description.length()) {
                arrayList.add(
                    QualityData(
                        accept_quality.getInt(i), accept_description.getString(i), accept_format[i]
                    )
                )
            }
        } else {
            val support_formats: JSONArray = `object`.getJSONArray("support_formats")
            for (array_index in 0 until support_formats.length()) {
                val support_format: JSONObject = support_formats.getJSONObject(array_index)
                val new_description: String = support_format.getString("new_description")
                val accept_quality: Int = support_format.getInt("quality")
                val accept_format: String = support_format.getString("format")
                arrayList.add(QualityData(accept_quality, new_description, accept_format))
            }
        }
        return arrayList
    }

    @Throws(JSONException::class)
    private fun doParseWebResult(`object`: JSONObject) {
        val array: JSONArray = `object`.getJSONArray("episodes")
        episodeData = getEpisodesInfo(array)
        availableQuality
    }

    @Deprecated("")
    @Throws(JSONException::class)
    private fun doParseOldResult(`object`: JSONObject) {
        val array: JSONArray = `object`.getJSONArray("episodes")
        episodeData = getEpisodesInfo(array)
        availableQuality
    }

    @Throws(JSONException::class)
    private fun getEpisodesInfo(array: JSONArray): ArrayList<InfoData> {
        val episodeData: ArrayList<InfoData> = ArrayList<InfoData>()
        for (episodes_index in 0 until array.length()) {
            val episodeData_index = InfoData()
            val object_episodes_index: JSONObject = array.getJSONObject(episodes_index)
            episodeData_index.aid = object_episodes_index.getLong("aid")
            episodeData_index.cid = object_episodes_index.getLong("cid")
            episodeData_index.ep_id = object_episodes_index.getLong("id")
            episodeData_index.cover = object_episodes_index.getString("cover")
            episodeData_index.payment = object_episodes_index.getInt("status")
            episodeData_index.bvid = object_episodes_index.getString("bvid")
            episodeData_index.area_limit = object_episodes_index.getInt("status")
            val object_episodes_index_badge: JSONObject =
                object_episodes_index.getJSONObject("badge_info")
            episodeData_index.badge = object_episodes_index_badge.getString("text")
            episodeData_index.badge_color = Color.parseColor(
                object_episodes_index_badge.getString("bg_color")
            )
            episodeData_index.badge_color_night = Color.parseColor(
                object_episodes_index_badge.getString("bg_color_night")
            )
            val dateFormat = SimpleDateFormat.getDateInstance()
            episodeData_index.pub_real_time = dateFormat.format(
                Date(
                    object_episodes_index
                        .getLong("pub_time") * 1000L
                )
            )
            episodeData_index.title = object_episodes_index.getString("long_title")
            episodeData_index.index = object_episodes_index.getString("title")
            episodeData.add(episodeData_index)
        }
        return episodeData
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(episodeData: ArrayList<InfoData>?, seasonData: SeasonData?)
    }

    companion object {
        private const val TAG = "SeasonHelper"
        const val AREA_LOCAL = 0
        const val AREA_LIMITED = 1
    }

    init {
        helper = BaseAPI(access_key)
    }
}