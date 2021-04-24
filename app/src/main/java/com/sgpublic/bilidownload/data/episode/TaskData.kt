package com.sgpublic.bilidownload.data.episode

import com.sgpublic.bilidownload.data.DownloadTaskData
import com.sgpublic.bilidownload.data.SeriesData
import java.util.*

class TaskData(
    var code: Int = 0,
    var message: String = "",
    var quality: Int = 0,
    var seasonType: Int = 0,
    var seasonTypeName: String = "",
    var mediaType: Int = 0,
    var seriesData: SeriesData = SeriesData(),
    var episodeData: InfoData = InfoData(),
    var taskInfo: DownloadTaskData = DownloadTaskData()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val taskData = other as TaskData
        return seriesData == taskData.seriesData && episodeData == taskData.episodeData
    }

    override fun hashCode(): Int {
        return Objects.hash(seriesData, episodeData)
    }
}