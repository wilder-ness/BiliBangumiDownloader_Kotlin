package com.sgpublic.bilidownload.data.Episode

import java.util.*

data class InfoData(
        var index: String = "",
        var aid: Long = 0L,
        var cid: Long = 0L,
        var ep_id: Long = 0L,
        var cover: String = "",
        var pub_real_time: String = "",
        var title: String = "",
        var payment: Int = 0,
        var bvid: String = "",
        var area_limit: Int = 0,
        var badge: String = "",
        var badge_color: Int = 0,
        var badge_color_night: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val infoData = other as InfoData
        return ep_id == infoData.ep_id
    }

    override fun hashCode(): Int {
        return Objects.hash(ep_id)
    }

    companion object {
        const val PAYMENT_NORMAL = 2
        const val PAYMENT_VIP = 13
    }
}