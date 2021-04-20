package com.sgpublic.bilidownload.data.episode

import com.sgpublic.bilidownload.data.DownloadTaskData
import com.sgpublic.bilidownload.data.SeriesData
import java.util.*

data class TaskData (
    var code: Int,
    var message: String,
    var quality: Int,
    var season_type: Int,
    var season_type_name: String,
    var media_type: Int,
    var seriesData: SeriesData,
    var episodeData: InfoData,
    var task_info: DownloadTaskData
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val taskData = other as TaskData
        return seriesData == taskData.seriesData &&
                episodeData == taskData.episodeData
    }

    override fun hashCode(): Int {
        return Objects.hash(seriesData, episodeData)
    }
}