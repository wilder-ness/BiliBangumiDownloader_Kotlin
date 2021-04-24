package com.sgpublic.bilidownload.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kongzue.dialogx.dialogs.MessageDialog
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.activity.About
import com.sgpublic.bilidownload.activity.Login
import com.sgpublic.bilidownload.activity.MyFollows
import com.sgpublic.bilidownload.activity.Setting
import com.sgpublic.bilidownload.base.BaseFragment
import com.sgpublic.bilidownload.databinding.FragmentMineBinding
import com.sgpublic.bilidownload.manager.ConfigManager

class Mine(private val context: AppCompatActivity): BaseFragment<FragmentMineBinding>(context) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        Glide.with(context)
            .load(ConfigManager.getString("face"))
            .into(binding.mineAvatar)
        val vipType = ConfigManager.getInt("vip_type")
        if (vipType == 0){
            binding.mineVip.visibility = View.GONE
            binding.mineVipString.visibility = View.GONE
        } else {
            binding.mineVip.visibility = View.VISIBLE
            binding.mineVipString.visibility = View.VISIBLE
            binding.mineVipString.text = ConfigManager.getString("vip_label")
        }
        val imageLevels = intArrayOf(
            R.drawable.ic_level_0,
            R.drawable.ic_level_1,
            R.drawable.ic_level_2,
            R.drawable.ic_level_3,
            R.drawable.ic_level_4,
            R.drawable.ic_level_5,
            R.drawable.ic_level_6
        )
        binding.mineLevel.setImageResource(imageLevels[ConfigManager.getInt("level")])
        binding.mineName.text = ConfigManager.getString("name", "哔哩番剧用户")
        binding.mineSign.text = ConfigManager.getString("sign", "这个人很懒，什么也没有留下。")
        val genders = intArrayOf(
            R.drawable.ic_gender_unknown,
            R.drawable.ic_gender_male,
            R.drawable.ic_gender_female
        )
        binding.mineGender.setImageResource(genders[ConfigManager.getInt("sex")])
        binding.mineMore.setOnClickListener {
            onToast(R.string.text_mine_developing)
        }
        binding.mineLogout.setOnClickListener {
            MessageDialog.build()
                .setTitle(R.string.title_mine_logout)
                .setMessage(R.string.text_mine_logout_check)
                .setOkButton(R.string.text_ok) { _, _ ->
                    Login.startActivity(context)
                    false
                }
                .setCancelButton(R.string.text_cancel)
                .show()
        }
        binding.mineAbout.setOnClickListener {
            About.startActivity(context)
        }
        binding.mineSetting.setOnClickListener {
            Setting.startActivity(context)
        }
        binding.mineOtherBangumi.setOnClickListener {
            MyFollows.startActivity(context)
        }
    }
}