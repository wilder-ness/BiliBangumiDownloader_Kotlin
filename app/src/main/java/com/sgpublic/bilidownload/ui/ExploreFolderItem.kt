package com.sgpublic.bilidownload.ui

import java.text.SimpleDateFormat
import java.util.*

class ExploreFolderItem(val foldName: String, private val fold_description: Long) {
    val foldDescription: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return formatter.format(fold_description)
        }
}