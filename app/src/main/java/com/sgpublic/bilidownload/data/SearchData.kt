package com.sgpublic.bilidownload.data

import android.text.Spannable

data class SearchData (
    var season_id: Long,
    var season_title: Spannable,
    var season_cover: String,
    var media_score: Double,
    var angle_title: String,
    var selection_style: String,
    var season_content: String,
    var episode_title: Spannable,
    var episode_cover: String,
    var episode_badges: String
)