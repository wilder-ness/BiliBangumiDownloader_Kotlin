package com.sgpublic.bilidownload.data.episode

data class DownloadData(
        var code: Int = -1,
        var message: String = "",
        var e: Throwable = Throwable(),
        var data: DASHDownloadData = DASHDownloadData()
) {
    data class DASHDownloadData (
            var timeLength: Long = 0L,
            var totalSize: Long = 0L,
            var videoUrl: String = "",
            var videoSize: Long = 0L,
            var videoBandwidth: Long = 0L,
            var videoId: Int = 0,
            var videoDownloadId: Long = 0L,
            var videoCodecid: Int = 0,
            var videoMd5: String = "",
            var audioUrl: String = "",
            var audioSize: Long = 0L,
            var audioBandwidth: Long = 0L,
            var audioId: Int = 0,
            var audioDownloadId: Long = 0L,
            var audioCodecid: Int = 0,
            var audioMd5: String = ""
    )
}