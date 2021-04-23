package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.viewbinding.ViewBinding
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.base.BaseFragment
import com.sgpublic.bilidownload.databinding.ActivityMainBinding
import com.sgpublic.bilidownload.fragment.Bangumi
import com.sgpublic.bilidownload.fragment.Mine

class Main: BaseActivity<ActivityMainBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.navBangumi.callOnClick()
    }

    override fun onViewSetup() {
        super.onViewSetup()
        binding.navBangumi.setOnClickListener {
            replaceFragment(R.id.main_fragment_bangumi, Bangumi(this@Main))
            selectNavigation(0)
        }
        binding.navMine.setOnClickListener {
            replaceFragment(R.id.main_fragment_mine, Mine(this@Main))
            selectNavigation(1)
        }
        initViewAtBottom(binding.navView)
    }

    private fun selectNavigation(index: Int){
        supportFragmentManager.findFragmentById(R.id.main_fragment_bangumi)?.let {
            if (index != 0){
                it.onPause()
            } else {
                it.onResume()
            }
        }
        binding.navBangumiImage.setColorFilter(getSelectedColor(index == 0))
        binding.navBangumiTitle.setTextColor(getSelectedColor(index == 0))
        binding.navMineImage.setColorFilter(getSelectedColor(index == 1))
        binding.navMineTitle.setTextColor(getSelectedColor(index == 1))
    }

    private fun getSelectedColor(isSelected: Boolean): Int {
        return if (isSelected) {
            getColor(R.color.colorPrimary)
        } else {
            getColor(R.color.color_text_dark)
        }
    }

    private fun <T: ViewBinding> replaceFragment(@IdRes id: Int, fragment: BaseFragment<T>){
        for (i in 0 until binding.mainFragment.childCount){
            val mView = binding.mainFragment.getChildAt(i)
            if (mView.visibility == View.VISIBLE && mView.id == id){
                break
            }
            if (mView.id == id){
                mView.visibility = View.VISIBLE
                continue
            }
            if (mView.visibility == View.VISIBLE) {
                mView.visibility = View.GONE
                continue
            }
        }

        val mFragment: BaseFragment<*>? = supportFragmentManager.findFragmentById(id) as BaseFragment<*>?
        if (mFragment != null){
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(id, fragment)
        transaction.commit()
    }

    override fun onPause() {
        super.onPause()
//        binding.navView.setFPS(-1)
    }

    override fun onResume() {
        super.onResume()
//        binding.navView.setFPS(60)
    }

    override fun isActivityAtBottom(): Boolean = true

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Main::class.java)
            context.startActivity(intent)
        }
    }
}