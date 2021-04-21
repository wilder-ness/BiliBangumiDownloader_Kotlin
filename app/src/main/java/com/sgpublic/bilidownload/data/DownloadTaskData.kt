package com.sgpublic.bilidownload.data

data class DownloadTaskData (
    var status: Int = 0,
    var progress: Int = 0,
    var download_bytes: Long = 0L,
    var total_bytes: Long = 0L
)