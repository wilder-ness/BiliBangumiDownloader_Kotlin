package com.sgpublic.bilidownload.data

import android.text.Spannable
import android.text.SpannableString

data class SearchData (
        var season_id: Long = 0L,
        var season_title: Spannable = SpannableString(""),
        var season_cover: String = "",
        var media_score: Double = 0.0,
        var angle_title: String = "",
        var selection_style: String = "",
        var season_content: String = "",
        var episode_title: Spannable = SpannableString(""),
        var episode_cover: String = "",
        var episode_badges: String = ""
)