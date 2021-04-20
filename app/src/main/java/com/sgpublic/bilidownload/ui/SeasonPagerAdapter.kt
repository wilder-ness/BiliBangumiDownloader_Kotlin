package com.sgpublic.bilidownload.ui

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import java.util.*

class SeasonPagerAdapter(private val view_list: ArrayList<View>, private val tab_titles: ArrayList<String>) : PagerAdapter() {
    override fun getCount(): Int {
        return view_list.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(view_list[position])
        return view_list[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(view_list[position])
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tab_titles[position]
    }
}