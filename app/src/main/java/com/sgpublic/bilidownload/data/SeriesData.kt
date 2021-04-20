package com.sgpublic.bilidownload.data

import java.util.*

class SeriesData {
    var season_type = 0
    var season_type_name = ""
    var badge = ""
    var badge_color = 0
    var badge_color_night = 0
    var cover = ""
    var title = ""
    var season_id = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SeriesData
        return season_id == that.season_id
    }

    override fun hashCode(): Int {
        return Objects.hash(season_id)
    }
}