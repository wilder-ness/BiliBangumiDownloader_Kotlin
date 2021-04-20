package com.sgpublic.bilidownload.data.Episode

class DownloadData {
    var code = -1
    var message = ""
    var e: Throwable = Throwable()
    var data = DASHDownloadData()

    class DASHDownloadData {
        var time_length: Long = 0
        var total_size: Long = 0
        var video_url: String = ""
        var video_size: Long = 0
        var video_bandwidth: Long = 0
        var video_id = 0
        var video_download_id: Long = 0
        var video_codecid = 0
        var video_md5: String = ""
        var audio_url: String = ""
        var audio_size: Long = 0
        var audio_bandwidth: Long = 0
        var audio_id = 0
        var audio_download_id: Long = 0
        var audio_codecid = 0
        var audio_md5: String = ""
    }
}