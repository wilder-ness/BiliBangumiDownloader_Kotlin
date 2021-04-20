package com.sgpublic.bilidownload.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.util.*

class ConfigManager(val context: Context) {

    companion object {
        private val TYPES_PACK = arrayOf(
            "tv.danmaku.bili",
            "com.bilibili.app.blue",
            "com.bilibili.app.in"
        )
        private val TYPES_STRING = arrayOf(
            "正式版",
            "概念版",
            "国际版"
        )

        private val QUALITIES_DESCRIPTION = arrayOf(
            "4K 超清",
            "1080P 高码率",
            "1080P 高清",
            "720P 高清",
            "480P 清晰",
            "360P 流畅"
        )
        private val QUALITIES_INT = intArrayOf(
            120, 112, 80, 64, 32, 16
        )

        private fun checkAppInstalled(context: Context, pkgName: String?): Boolean {
            if (pkgName == null || pkgName.isEmpty()) {
                return false
            }
            val packageInfo: PackageInfo? = try {
                context.packageManager.getPackageInfo(pkgName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            return packageInfo != null
        }

        fun getQualities(): ArrayList<QualityItem> {
            val qualityItems = ArrayList<QualityItem>()
            for (index in QUALITIES_INT.indices) {
                qualityItems.add(
                    QualityItem(
                        QUALITIES_DESCRIPTION[index],
                        QUALITIES_INT[index],
                        index
                    )
                )
            }
            return qualityItems
        }

        fun checkQuality(context: Context): QualityItem {
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var qualityIndex = 2
            val qualitySet =
                sharedPreferences.getInt("quality", QUALITIES_INT[qualityIndex])
            MyLog.d("quality_set: $qualitySet")
            for (index in QUALITIES_DESCRIPTION.indices) {
                val quality: Int = QUALITIES_INT[index]
                if (quality == qualitySet) {
                    MyLog.d("quality: $quality")
                    qualityIndex = index
                    break
                }
            }
            MyLog.d("quality_index: $qualityIndex")
            sharedPreferences.edit()
                .putInt("quality", QUALITIES_INT[qualityIndex])
                .apply()
            return QualityItem(
                QUALITIES_DESCRIPTION[qualityIndex],
                QUALITIES_INT[qualityIndex],
                qualityIndex
            )
        }

        fun checkClient(context: Context): ClientItem? {
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val clientItems: ArrayList<ClientItem> = getInstalledClients(context)
            var package_name =
                sharedPreferences.getString("package", TYPES_PACK[0])
            var package_index = -1
            var result: ClientItem? = null
            if (clientItems.size > 0) {
                for (index in clientItems.indices) {
                    if (clientItems[index].packageName == package_name) {
                        package_index = index
                        break
                    }
                }
                if (package_index < 0) {
                    package_index = 0
                }
                package_name = clientItems[package_index].packageName
                sharedPreferences.edit()
                    .putString("package", package_name)
                    .apply()
                result = ClientItem(TYPES_STRING[package_index], package_name)
            }
            return result
        }

        fun getInstalledClients(context: Context?): ArrayList<ClientItem> {
            val clientItems = ArrayList<ClientItem>()
            for (index in TYPES_PACK.indices) {
                if (checkAppInstalled(context!!, TYPES_STRING[index])) {
                    clientItems.add(
                        ClientItem(
                            TYPES_STRING[index],
                            TYPES_PACK[index]
                        )
                    )
                }
            }
            return clientItems
        }
    }

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "user",
        Context.MODE_PRIVATE
    )
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()


    fun getString(key: String, defValue: String = "") = sharedPreferences.getString(key, defValue).toString()
    fun getInt(key: String, defValue: Int = 0) = sharedPreferences.getInt(key, defValue)
    fun getLong(key: String, defValue: Long = 0L) = sharedPreferences.getLong(key, defValue)
    fun getBoolean(key: String, defValue: Boolean = false) = sharedPreferences.getBoolean(
        key,
        defValue
    )

    fun putString(key: String, value: String): ConfigManager{
        editor.putString(key, value)
        return this
    }
    fun putInt(key: String, value: Int): ConfigManager{
        editor.putInt(key, value)
        return this
    }
    fun putLong(key: String, value: Long): ConfigManager{
        editor.putLong(key, value)
        return this
    }
    fun putBoolean(key: String, value: Boolean): ConfigManager{
        editor.putBoolean(key, value)
        return this
    }
    fun apply(){
        editor.apply()
    }

    class ClientItem internal constructor(val name: String, val packageName: String)

    class QualityItem internal constructor(val name: String, val quality: Int, val index: Int)
}