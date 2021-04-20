package com.sgpublic.bilidownload.data

import com.sgpublic.bilidownload.data.Episode.QualityData
import java.util.*
import kotlin.collections.ArrayList

class SeasonData {
    var area = 0
    var base_info: SeriesData = SeriesData()
    var actors = ""
    var alias = ""
    var evaluate = ""
    var staff = ""
    var styles = ""
    var description = ""
    var rating = 0.0
    var actors_lines = 0
    var staff_lines = 0
    var season_type = 0
    var series: ArrayList<SeriesData> = ArrayList()
    var qualities: ArrayList<QualityData> = ArrayList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SeasonData
        return series == that.series
    }

    override fun hashCode(): Int {
        return Objects.hash(series)
    }
}