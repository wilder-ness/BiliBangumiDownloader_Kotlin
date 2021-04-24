package com.sgpublic.bilidownload.data

import java.util.*

data class SeriesData(
    var seasonType: Int = 0,
    var seasonTypeName: String = "",
    var badge: String = "",
    var badgeColor: Int = 0,
    var badgeColorNight: Int = 0,
    var cover: String = "",
    var title: String = "",
    var seasonId: Long = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SeriesData
        return seasonId == that.seasonId
    }

    override fun hashCode(): Int {
        return Objects.hash(seasonId)
    }
}