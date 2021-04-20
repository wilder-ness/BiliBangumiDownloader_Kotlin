package com.sgpublic.bilidownload.data.Episode

import com.sgpublic.bilidownload.data.DownloadTaskData
import com.sgpublic.bilidownload.data.SeriesData
import java.util.*

class TaskData {
    var code = 0
    var message = ""
    var quality = 0
    var season_type = 0
    var season_type_name = ""
    var media_type = 0
    var seriesData: SeriesData = SeriesData()
    var episodeData: InfoData = InfoData()
    var task_info: DownloadTaskData = DownloadTaskData()

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