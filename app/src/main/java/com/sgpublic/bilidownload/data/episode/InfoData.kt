package com.sgpublic.bilidownload.data.episode

import java.util.*

data class InfoData (
    var index: String,
    var aid: Long,
    var cid: Long,
    var ep_id: Long,
    var cover: String,
    var pub_real_time: String,
    var title: String,
    var payment: Int,
    var bvid: String,
    var area_limit: Int,
    var badge: String,
    var badge_color: Int,
    var badge_color_night: Int,
){
    companion object {
        const val PAYMENT_NORMAL = 2
        const val PAYMENT_VIP = 13
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val infoData = other as InfoData
        return ep_id == infoData.ep_id
    }

    override fun hashCode(): Int {
        return Objects.hash(ep_id)
    }
}