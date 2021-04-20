package com.sgpublic.bilidownload.data

import com.sgpublic.bilidownload.data.episode.QualityData
import java.util.*

data class SeasonData (
    var area: Int,
    var base_info: SeriesData,
    var actors: String,
    var alias: String,
    var evaluate: String,
    var staff: String,
    var styles: String,
    var description: String,
    var rating: Double,
    var actors_lines: Int,
    var staff_lines: Int,
    var season_type: Int,
    var series: ArrayList<SeriesData>,
    var qualities: ArrayList<QualityData>
){
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