package com.sgpublic.bilidownload.data

import android.text.Spannable
import android.text.SpannableString

class SearchData {
    var season_id = 0L
    var season_title: Spannable = SpannableString("")
    var season_cover = ""
    var media_score = 0.0
    var angle_title = ""
    var selection_style = ""
    var season_content = ""
    var episode_title: Spannable = SpannableString("")
    var episode_cover = ""
    var episode_badges = ""
}