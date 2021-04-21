package com.sgpublic.bilidownload.data

data class FollowData (
        var badge: String = "",
        var badge_color: Int = 0,
        var badge_color_night: Int = 0,
        var cover: String = "",
        var square_cover: String = "",
        var is_finish: Int = 0,
        var title: String = "",
        var season_id: Long = 0L,
        var new_ep_cover: String = "",
        var new_ep_id: Long = 0L,
        var new_ep_index_show: String = "",
        var new_ep_is_new: Int = 0
)