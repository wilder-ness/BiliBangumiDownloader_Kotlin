package com.sgpublic.bilidownload.data.Episode

data class DownloadData(
        var code: Int = -1,
        var message: String = "",
        var e: Throwable = Throwable(),
        var data: DASHDownloadData = DASHDownloadData()
) {
    data class DASHDownloadData (
            var time_length: Long = 0L,
            var total_size: Long = 0L,
            var video_url: String = "",
            var video_size: Long = 0L,
            var video_bandwidth: Long = 0L,
            var video_id: Int = 0,
            var video_download_id: Long = 0L,
            var video_codecid: Int = 0,
            var video_md5: String = "",
            var audio_url: String = "",
            var audio_size: Long = 0L,
            var audio_bandwidth: Long = 0L,
            var audio_id: Int = 0,
            var audio_download_id: Long = 0L,
            var audio_codecid: Int = 0,
            var audio_md5: String = ""
    )
}