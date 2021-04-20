package com.sgpublic.bilidownload.module

import android.content.Context
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.data.Episode.DownloadData
import com.sgpublic.bilidownload.data.Episode.InfoData.Companion.PAYMENT_NORMAL
import com.sgpublic.bilidownload.data.Episode.TaskData
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.manager.DownloadTaskManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class DownloadModule(private val context: Context) {
    private val helper: BaseAPI
    private val downloadData: DownloadData = DownloadData()

    init {
        val accessToken = ConfigManager.getString("access_token")
        helper = BaseAPI(accessToken)
    }

    //{"code":-10403,"message":"抱歉您所在地区不可观看！"}
    //{"code":-10403,"message":"大会员专享限制"}
    fun getDownloadInfo(data: TaskData): DownloadData {
        getOfficialDownloadInfo(data)
        return downloadData
    }

    private fun getOfficialDownloadInfo(data: TaskData) {
        val call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality)
        try {
            val response = call.execute()
            val json = JSONObject(response.body?.string().toString())
            if (json.getInt("code") != 0) {
                if (json.getInt("code") == -10403 && data.episodeData.payment == PAYMENT_NORMAL) {
                    getBiliplusDownloadInfo(data)
                } else {
                    downloadData.code = -501
                    downloadData.message = json.getString("message")
                }
            } else {
                val type: String = json.getString("type")
                if (type == "FLV") {
                    downloadData.code = -505
                    downloadData.message = context.getString(R.string.error_download_flv)
                } else if (type == "DASH") {
                    getDASHData(json, data.quality)
                } else {
                    downloadData.code = -506
                }
            }
        } catch (e: IOException) {
            if (e is UnknownHostException) {
                downloadData.code = -501
                downloadData.message = context.getString(R.string.error_network)
            } else {
                downloadData.code = -502
                downloadData.message = e.message.toString()
            }
            downloadData.e = e
        } catch (e: JSONException) {
            downloadData.code = -503
            downloadData.e = e
        }
    }

    private fun getBiliplusDownloadInfo(data: TaskData) {
        val call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality)
        try {
            val response = call.execute()
            if (response.code == -504) {
                getKghostDownloadInfo(data)
                return
            }
            val json = JSONObject(response.body?.string().toString())
            if (json.getInt("code") != 0) {
                downloadData.code = -514
                downloadData.message = json.getString("message")
            } else {
                if (!json.isNull("durl")) {
                    downloadData.code = -515
                    downloadData.message = context.getString(R.string.error_download_flv)
                } else if (!json.isNull("dash")) {
                    getDASHData(json, data.quality)
                } else {
                    downloadData.code = -516
                }
            }
        } catch (e: IOException) {
            if (e is UnknownHostException) {
                downloadData.code = -511
                downloadData.message = context.getString(R.string.error_network)
            } else {
                downloadData.code = -512
                downloadData.message = e.message.toString()
            }
            downloadData.e = e
        } catch (e: JSONException) {
            downloadData.code = -513
            downloadData.e = e
        }
    }

    private fun getKghostDownloadInfo(data: TaskData) {
        val call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality)
        try {
            val response = call.execute()
            val json = JSONObject(response.body?.string().toString())
            if (json.getInt("code") != 0) {
                downloadData.code = -524
                downloadData.message = json.getString("message")
            } else {
                if (!json.isNull("durl")) {
                    downloadData.code = -525
                    downloadData.message = context.getString(R.string.error_download_flv)
                } else if (!json.isNull("dash")) {
                    getDASHData(json, data.quality)
                } else {
                    downloadData.code = -526
                }
            }
        } catch (e: IOException) {
            if (e is UnknownHostException) {
                downloadData.code = -521
                downloadData.message = context.getString(R.string.error_network)
            } else {
                downloadData.code = -522
                downloadData.message = e.message.toString()
            }
            downloadData.e = e
        } catch (e: JSONException) {
            downloadData.code = -523
            downloadData.e = e
        } catch (e: IndexOutOfBoundsException) {
            downloadData.code = -523
            downloadData.e = e
        }
    }

    @Throws(JSONException::class)
    private fun getDASHData(json: JSONObject, qn: Int) {
        val downloadData = DownloadData.DASHDownloadData()
        downloadData.time_length = json.getLong("timelength")
        val privateObject: JSONObject = json.getJSONObject("dash")
        var array: JSONArray = privateObject.getJSONArray("video")
        var objectVideo: JSONObject? = null
        for (array_index in 0 until array.length()) {
            val objectVideoIndex: JSONObject = array.getJSONObject(array_index)
            val videoQnPre = objectVideo?.getInt("id") ?: -1
            val videoQnThis: Int = objectVideoIndex.getInt("id")
            if (videoQnThis in (videoQnPre + 1)..qn) {
                objectVideo = objectVideoIndex
            }
        }
        if (objectVideo != null) {
            downloadData.video_url = objectVideo.getString("base_url")
            downloadData.video_bandwidth = objectVideo.getLong("bandwidth")
            downloadData.video_codecid =
                if (objectVideo.isNull("codecid")) 0 else objectVideo.getInt("codecid")
            downloadData.video_id = objectVideo.getInt("id")
            downloadData.video_md5 =
                if (objectVideo.isNull("md5")) "" else objectVideo.getString("md5")
            downloadData.video_size = DownloadTaskManager.getSizeLong(downloadData.video_url)
        }
        array = privateObject.getJSONArray("audio")
        var objectAudio: JSONObject? = null
        for (array_index in 0 until array.length()) {
            val objectAudioIndex: JSONObject = array.getJSONObject(array_index)
            val audioQnPre = if (objectAudio == null) -1 else objectAudio.getInt("id") - 30200
            val audioQnThis: Int = objectAudioIndex.getInt("id") - 30200
            if (audioQnThis in (audioQnPre + 1)..qn) {
                objectAudio = objectAudioIndex
            }
        }
        if (objectAudio != null) {
            downloadData.audio_url = objectAudio.getString("base_url")
            downloadData.audio_bandwidth = objectAudio.getLong("bandwidth")
            downloadData.audio_codecid =
                if (objectAudio.isNull("codecid")) 0 else objectAudio.getInt("codecid")
            downloadData.audio_id = objectAudio.getInt("id")
            downloadData.audio_md5 =
                if (objectAudio.isNull("md5")) "" else objectAudio.getString("md5")
            downloadData.audio_size = DownloadTaskManager.getSizeLong(downloadData.audio_url)
        }
        if (objectVideo != null && objectAudio != null) {
            this.downloadData.code = 0
            downloadData.total_size = downloadData.audio_size + downloadData.video_size
            this.downloadData.data = downloadData
        } else {
            this.downloadData.code = -531
            this.downloadData.message = context.getString(R.string.error_download_url)
        }
    }
}