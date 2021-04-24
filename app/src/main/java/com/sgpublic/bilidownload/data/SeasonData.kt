package com.sgpublic.bilidownload.data

import com.sgpublic.bilidownload.data.episode.QualityData
import java.util.*
import kotlin.collections.ArrayList

class SeasonData(
    var area: Int = 0,
    var baseInfo: SeriesData = SeriesData(),
    var actors: String = "",
    var alias: String = "",
    var evaluate: String = "",
    var staff: String = "",
    var styles: String = "",
    var description: String = "",
    var rating: Double = 0.0,
    var actorsLines: Int = 0,
    var staffLines: Int = 0,
    var seasonType: Int = 0,
    var series: ArrayList<SeriesData> = ArrayList(),
    var qualities: ArrayList<QualityData> = ArrayList()
) {
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