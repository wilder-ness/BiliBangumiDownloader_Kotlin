package com.sgpublic.bilidownload.module

import android.content.Context
import com.sgpublic.bilidownload.data.Episode.DownloadData
import java.io.IOException
import java.net.UnknownHostException

class DownloadModule(private val context: Context) {
    private val helper: BaseAPI
    private val downloadData: DownloadData = DownloadData()

    //{"code":-10403,"message":"抱歉您所在地区不可观看！"}
    //{"code":-10403,"message":"大会员专享限制"}
    fun getDownloadInfo(data: TaskData): DownloadData {
        getOfficialDownloadInfo(data)
        return downloadData
    }

    private fun getOfficialDownloadInfo(data: TaskData) {
        val call = helper.getEpisodeOfficialRequest(data.episodeData.cid, data.quality)
        try {
            val response = call!!.execute()
            val `object` = JSONObject(response.body!!.string())
            if (`object`.getInt("code") != 0) {
                if (`object`.getInt("code") == -10403 and data.episodeData.payment === PAYMENT_NORMAL) {
                    getBiliplusDownloadInfo(data)
                } else {
                    downloadData.code = -501
                    downloadData.message = `object`.getString("message")
                }
            } else {
                val type: String = `object`.getString("type")
                if (type == "FLV") {
                    downloadData.code = -505
                    downloadData.message = context.getString(R.string.error_download_flv)
                } else if (type == "DASH") {
                    getDASHData(`object`, data.quality)
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
                downloadData.message = e.message
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
            val response = call!!.execute()
            if (response.code == -504) {
                getKghostDownloadInfo(data)
                return
            }
            val `object` = JSONObject(response.body!!.string())
            if (`object`.getInt("code") != 0) {
                downloadData.code = -514
                downloadData.message = `object`.getString("message")
            } else {
                if (!`object`.isNull("durl")) {
                    downloadData.code = -515
                    downloadData.message = context.getString(R.string.error_download_flv)
                } else if (!`object`.isNull("dash")) {
                    getDASHData(`object`, data.quality)
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
                downloadData.message = e.message
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
            val response = call!!.execute()
            val `object` = JSONObject(response.body!!.string())
            if (`object`.getInt("code") != 0) {
                downloadData.code = -524
                downloadData.message = `object`.getString("message")
            } else {
                if (!`object`.isNull("durl")) {
                    downloadData.code = -525
                    downloadData.message = context.getString(R.string.error_download_flv)
                } else if (!`object`.isNull("dash")) {
                    getDASHData(`object`, data.quality)
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
                downloadData.message = e.message
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
    private fun getDASHData(`object`: JSONObject, qn: Int) {
        val downloadData = DASHDownloadData()
        downloadData.time_length = `object`.getLong("timelength")
        val private_object: JSONObject = `object`.getJSONObject("dash")
        var array: JSONArray
        array = private_object.getJSONArray("video")
        var object_video: JSONObject? = null
        for (array_index in 0 until array.length()) {
            val object_video_index: JSONObject = array.getJSONObject(array_index)
            val video_qn_pre = if (object_video == null) -1 else object_video.getInt("id")
            val video_qn_this: Int = object_video_index.getInt("id")
            if (video_qn_this > video_qn_pre && video_qn_this <= qn) {
                object_video = object_video_index
            }
        }
        if (object_video != null) {
            downloadData.video_url = object_video.getString("base_url")
            downloadData.video_bandwidth = object_video.getLong("bandwidth")
            downloadData.video_codecid =
                if (object_video.isNull("codecid")) 0 else object_video.getInt("codecid")
            downloadData.video_id = object_video.getInt("id")
            downloadData.video_md5 =
                if (object_video.isNull("md5")) "" else object_video.getString("md5")
            downloadData.video_size = DownloadTaskManager.getSizeLong(downloadData.video_url)
        }
        array = private_object.getJSONArray("audio")
        var object_audio: JSONObject? = null
        for (array_index in 0 until array.length()) {
            val object_audio_index: JSONObject = array.getJSONObject(array_index)
            val audio_qn_pre = if (object_audio == null) -1 else object_audio.getInt("id") - 30200
            val audio_qn_this: Int = object_audio_index.getInt("id") - 30200
            if (audio_qn_this > audio_qn_pre && audio_qn_this <= qn) {
                object_audio = object_audio_index
            }
        }
        if (object_audio != null) {
            downloadData.audio_url = object_audio.getString("base_url")
            downloadData.audio_bandwidth = object_audio.getLong("bandwidth")
            downloadData.audio_codecid =
                if (object_audio.isNull("codecid")) 0 else object_audio.getInt("codecid")
            downloadData.audio_id = object_audio.getInt("id")
            downloadData.audio_md5 =
                if (object_audio.isNull("md5")) "" else object_audio.getString("md5")
            downloadData.audio_size = DownloadTaskManager.getSizeLong(downloadData.audio_url)
        }
        if (object_video != null && object_audio != null) {
            this.downloadData.code = 0
            downloadData.total_size = downloadData.audio_size + downloadData.video_size
            this.downloadData.data = downloadData
        } else {
            this.downloadData.code = -531
            this.downloadData.message = context.getString(R.string.error_download_url)
        }
    }

    init {
        val access_token = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            .getString("access_token", "")
        helper = BaseAPI(access_token)
    }
}