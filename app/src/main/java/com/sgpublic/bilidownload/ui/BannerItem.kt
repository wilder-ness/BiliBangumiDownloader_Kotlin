package com.sgpublic.bilidownload.ui

import androidx.appcompat.app.AppCompatActivity

class BannerItem(
        val context: AppCompatActivity,
        val bannerPath: String,
        val seasonCover: String,
        val seasonId: Long,
        val title: String,
        private val indicator: String,
        val badge: String,
        val badgeColor: Int
) {
    val indicatorText: String get() = "$title：$indicator"
}