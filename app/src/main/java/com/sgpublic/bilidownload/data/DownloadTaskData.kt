package com.sgpublic.bilidownload.data

data class DownloadTaskData (
    var status: Int,
    var progress: Int,
    var download_bytes: Long,
    var total_bytes: Long,
)