package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityMyFollowsBinding
import com.sgpublic.bilidownload.fragment.Follows
import com.sgpublic.bilidownload.ui.FollowsPagerAdapter
import java.util.*

class MyFollows: BaseActivity<ActivityMyFollowsBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        setSupportActionBar(binding.myFollowsToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_mine_my_follows)
        }

        val list = ArrayList<Follows>()
        list.add(Follows(this@MyFollows, R.string.title_follows_want, 1))
        list.add(Follows(this@MyFollows, R.string.title_follows_watching, 2))
        list.add(Follows(this@MyFollows, R.string.title_follows_watched, 3))
        binding.myFollowsPager.adapter = FollowsPagerAdapter(supportFragmentManager, list)
        binding.myFollowsTab.setViewPager(binding.myFollowsPager)
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, MyFollows::class.java)
            context.startActivity(intent)
        }
    }
}