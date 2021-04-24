package com.sgpublic.bilidownload.data

data class DownloadTaskData (
    var status: Int = 0,
    var progress: Int = 0,
    var downloadBytes: Long = 0L,
    var totalBytes: Long = 0L
)