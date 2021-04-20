package com.sgpublic.bilidownload.ui

import android.content.Context

class BannerItem(
        val context: Context,
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