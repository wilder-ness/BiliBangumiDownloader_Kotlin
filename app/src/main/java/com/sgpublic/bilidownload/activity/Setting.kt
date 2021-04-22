package com.sgpublic.bilidownload.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivitySettingBinding
import com.sgpublic.bilidownload.databinding.DialogSettingTaskCountBinding
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.util.MyLog
import java.util.*

class Setting: BaseActivity<ActivitySettingBinding>() {
    private var clientSet: ConfigManager.ClientItem? = null
    private var qualityItem: ConfigManager.QualityItem? = null
    private var taskCount = 1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val clientItems = ConfigManager.getInstalledClients()
        if (clientItems.size > 1){
            binding.settingType.alpha = 1.0F
            binding.settingType.setOnClickListener {
                typeSetting()
            }
            clientSet = ConfigManager.checkClient()
        } else {
            binding.settingType.alpha = 0.3F
            binding.settingType.isClickable = false
            if (clientItems.size == 1){
                clientSet = clientItems[0]
                ConfigManager.putString("package", clientSet!!.packageName)
            }
        }
        clientSet?.let {
            binding.settingTypeString.text = it.name
        }
        qualityItem = ConfigManager.checkQuality()
        qualityItem?.let {
            binding.settingQualityString.text = it.name
        }
        binding.settingAutoStart.isChecked = ConfigManager.getBoolean("task_auto_start", true)
        taskCountLoad()
    }

    override fun onViewSetup() {
        super.onViewSetup()
        setSupportActionBar(binding.settingToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_mine_setting)
        }

        binding.settingTaskCount.setOnClickListener {
            taskCountSetting(1, 3)
        }
        binding.settingQuality.setOnClickListener {
            qualitySetting()
        }
        binding.settingAutoStartBase.setOnClickListener {
            binding.settingAutoStart.isChecked = !binding.settingAutoStart.isChecked
        }
        binding.settingAutoStart.setOnCheckedChangeListener { _, isChecked ->
            ConfigManager.putBoolean("task_auto_start", isChecked)
        }
    }

    private fun typeSetting() {
        val clientItems: ArrayList<ConfigManager.ClientItem> = ConfigManager.getInstalledClients()
        val typesString = arrayOfNulls<String>(clientItems.size)
        var typeSet = 0
        val packageSet: String = ConfigManager.getString("package", clientItems[0].packageName)
        for (index in clientItems.indices) {
            val item: ConfigManager.ClientItem = clientItems[index]
            typesString[index] = item.name
            if (packageSet == item.packageName) {
                typeSet = index
                clientSet = clientItems[index]
            }
        }
        AlertDialog.Builder(this@Setting)
            .setTitle(R.string.title_setting_type)
            .setSingleChoiceItems(typesString, typeSet) { _: DialogInterface?, which: Int ->
                clientSet = clientItems[which]
            }
            .setPositiveButton(R.string.text_ok) { _, _ ->
                ConfigManager.putString("package", clientSet!!.packageName)
                binding.settingTypeString.text = clientSet!!.name
            }
            .setNegativeButton(R.string.text_cancel, null)
            .show()
    }

    private fun qualitySetting() {
        qualityItem = ConfigManager.checkQuality()
        val qualities: ArrayList<ConfigManager.QualityItem> = ConfigManager.getQualities()
        val qualityDescription = arrayOfNulls<String>(qualities.size)
        for (index in qualities.indices) {
            qualityDescription[index] = qualities[index].name
        }
        val builder = AlertDialog.Builder(this@Setting)
        builder.setTitle(R.string.title_setting_quality)
        builder.setSingleChoiceItems(
            qualityDescription, qualityItem!!.index
        ) { _, which -> qualityItem = qualities[which] }
        builder.setPositiveButton(R.string.text_ok) { _, _ ->
            ConfigManager.putInt("quality", qualityItem!!.quality)
            qualitySettingLoad()
        }
        builder.setNegativeButton(R.string.text_cancel, null)
        builder.show()
    }

    @Suppress("SameParameterValue")
    private fun taskCountSetting(min: Int, max: Int) {
        taskCount = ConfigManager.checkTaskCount()

        val dialogSettingTaskCount = DialogSettingTaskCountBinding.inflate(layoutInflater)
        dialogSettingTaskCount.dialogSettingTaskMin.text = min.toString()
        dialogSettingTaskCount.dialogSettingTaskMax.text = max.toString()
        dialogSettingTaskCount.dialogSettingTaskSeek.min = min
        dialogSettingTaskCount.dialogSettingTaskSeek.max = max
        dialogSettingTaskCount.dialogSettingTaskSeek.progress = taskCount
        dialogSettingTaskCount.dialogSettingTaskSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                taskCount = progress
                dialogSettingTaskCount.dialogSettingTaskShow.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        dialogSettingTaskCount.dialogSettingTaskShow.text = taskCount.toString()

        AlertDialog.Builder(this@Setting)
            .setTitle(R.string.title_setting_task)
            .setView(dialogSettingTaskCount.root)
            .setPositiveButton(R.string.text_ok) { _, _ ->
                ConfigManager.putInt("task_parallel_count", taskCount)
                taskCountLoad()
            }
            .setNegativeButton(R.string.text_cancel, null)
            .show()
    }

    private fun qualitySettingLoad() {
        qualityItem?.let {
            binding.settingQualityString.text = it.name
        }
    }

    private fun taskCountLoad() {
        binding.settingTaskCountString.text = String.format(
            getString(R.string.text_setting_task_show),
            ConfigManager.checkTaskCount()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Setting::class.java)
            context.startActivity(intent)
        }
    }

    override fun onSetSwipeBackEnable(): Boolean = true
}