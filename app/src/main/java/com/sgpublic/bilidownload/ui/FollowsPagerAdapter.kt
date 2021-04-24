package com.sgpublic.bilidownload.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.sgpublic.bilidownload.fragment.Follows

class FollowsPagerAdapter(fragmentManager: FragmentManager, private val list: ArrayList<Follows>):
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Fragment = list[position]

    override fun getPageTitle(position: Int): CharSequence = list[position].getTitle()
}