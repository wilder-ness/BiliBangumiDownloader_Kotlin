package com.sgpublic.bilidownload.data.episode

data class DownloadData (
    var code: Int,
    var message: String,
    var e: Throwable?,
    var data: DASHDownloadData
){
    data class DASHDownloadData (
        var time_length: Long,
        var total_size: Long,
        var video_url: String,
        var video_size: Long,
        var video_bandwidth: Long,
        var video_id: Int,
        var video_download_id: Long,
        var video_codecid: Int,
        var video_md5: String?,
        var audio_url: String?,
        var audio_size: Long,
        var audio_bandwidth: Long,
        var audio_id: Int,
        var audio_download_id: Long,
        var audio_codecid: Int,
        var audio_md5: String?
    )
}