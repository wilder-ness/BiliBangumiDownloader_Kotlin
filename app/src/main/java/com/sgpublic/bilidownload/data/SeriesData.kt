package com.sgpublic.bilidownload.data

import java.util.*

data class SeriesData(
        var season_type: Int = 0,
        var season_type_name: String = "",
        var badge: String = "",
        var badge_color: Int = 0,
        var badge_color_night: Int = 0,
        var cover: String = "",
        var title: String = "",
        var season_id: Long = 0L
) {
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