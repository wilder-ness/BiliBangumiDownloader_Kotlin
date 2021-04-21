package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivitySeasonBinding

class Season: BaseActivity<ActivitySeasonBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    companion object {
        fun startActivity(context: Context, title: String?, sid: Long, cover_url: String?) {
            val intent = Intent(context, Season::class.java)
            intent.putExtra("season_id", sid)
            intent.putExtra("cover_url", cover_url)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }
    }

    override fun onSetSwipeBackEnable(): Boolean = true
}