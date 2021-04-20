package com.sgpublic.bilidownload.data.Episode

import java.util.*

class InfoData {
    var index = ""
    var aid = 0L
    var cid = 0L
    var ep_id = 0L
    var cover = ""
    var pub_real_time = ""
    var title = ""
    var payment = 0
    var bvid = ""
    var area_limit = 0
    var badge = ""
    var badge_color = 0
    var badge_color_night = 0
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