package com.sgpublic.bilidownload.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.kongzue.dialogx.impl.ActivityLifecycleImpl
import java.lang.ref.WeakReference
import java.util.*

class ConfigManager {
    companion object {
        private val TYPES = mapOf(
            "tv.danmaku.bili" to "正式版",
            "com.bilibili.app.blue" to "概念版",
            "com.bilibili.app.in" to "国际版"
        )

        private val QUALITIES = mapOf(
            120 to "4K 超清",
            112 to "1080P 高码率",
            80 to "1080P 高清",
            64 to "720P 高清",
            32 to "480P 清晰",
            16 to "360P 流畅"
        )

        private val instance = ConfigManager()
        private lateinit var contextWeakReference: WeakReference<Activity>

        fun init(context: Application){
            ActivityLifecycleImpl.init(context) {
                contextWeakReference = WeakReference(it)
            }
        }

        fun getString(key: String, defValue: String = "") = instance.sharedPreferences.getString(
            key,
            defValue
        ).toString()
        fun getInt(key: String, defValue: Int = 0) = instance.sharedPreferences.getInt(
            key,
            defValue
        )
        fun getLong(key: String, defValue: Long = 0L) = instance.sharedPreferences.getLong(
            key,
            defValue
        )
        fun getBoolean(key: String, defValue: Boolean = false) = instance.sharedPreferences.getBoolean(
            key,
            defValue
        )

        fun putString(key: String, value: String) {
            instance.sharedPreferences.edit()
                    .putString(key, value)
                    .apply()
        }
        fun putInt(key: String, value: Int) {
            instance.sharedPreferences.edit()
                    .putInt(key, value)
                    .apply()
        }
        fun putLong(key: String, value: Long) {
            instance.sharedPreferences.edit()
                    .putLong(key, value)
                    .apply()
        }
        fun putBoolean(key: String, value: Boolean) {
            instance.sharedPreferences.edit()
                    .putBoolean(key, value)
                    .apply()
        }

        private fun checkAppInstalled(pkgName: String): Boolean {
            if (pkgName.isEmpty()) {
                return false
            }
            val packageInfo: PackageInfo? = try {
                contextWeakReference.get()!!.packageManager.getPackageInfo(pkgName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            return packageInfo != null
        }

        fun getQualities(): ArrayList<QualityItem> {
            val qualityItems = ArrayList<QualityItem>()
            var index = 0
            for ((key, value) in QUALITIES) {
                qualityItems.add(
                    QualityItem(value, key, index)
                )
                index++
            }
            return qualityItems
        }

        fun checkTaskCount(): Int {
            var result = getInt("task_parallel_count", 1)
            if (result > 3 || result < 1) {
                result = 1
                putInt("task_parallel_count", 1)
            }
            return result
        }

        fun checkQuality(): QualityItem {
            val qualitySet = getInt("quality", 80)
            var qualityIndex = 0
            for ((key, value) in QUALITIES) {
                if (key == qualitySet) {
                    return QualityItem(value, key, qualityIndex)
                }
                qualityIndex++
            }
            return QualityItem(QUALITIES[80]!!, 80, 2)
        }

        fun checkClient(): ClientItem {
            val clientItems: ArrayList<ClientItem> = getInstalledClients()
            val packageName: String = getString("package", "tv.danmaku.bili")
            val result = ClientItem(TYPES["tv.danmaku.bili"]!!, "tv.danmaku.bili")
            if (clientItems.size <= 0) {
                return result
            }
            for ((key, value) in TYPES) {
                if (key == packageName){
                    return ClientItem(value, key)
                }
            }
            putString("package", result.packageName)
            return result
        }

        fun getInstalledClients(): ArrayList<ClientItem> {
            val clientItems = ArrayList<ClientItem>()
            for ((key, value) in TYPES) {
                if (!checkAppInstalled(key)) {
                    continue
                }
                clientItems.add(ClientItem(value, key))
            }
            return clientItems
        }
    }

    private val sharedPreferences: SharedPreferences
    get() = contextWeakReference.get()!!.getSharedPreferences("user", Context.MODE_PRIVATE)
    
    class ClientItem internal constructor(val name: String, val packageName: String)

    class QualityItem internal constructor(val name: String, val quality: Int, val index: Int)
}