package com.sgpublic.bilidownload.data

data class FollowData (
    var badge: String,
    var badge_color: Int,
    var badge_color_night: Int,
    var cover: String,
    var square_cover: String,
    var is_finish: Int,
    var title: String,
    var season_id: Long,
    var new_ep_cover: String,
    var new_ep_id: Long,
    var new_ep_index_show: String,
    var new_ep_is_new: Int,
)