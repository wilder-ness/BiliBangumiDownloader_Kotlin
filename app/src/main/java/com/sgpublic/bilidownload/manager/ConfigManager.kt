package com.sgpublic.bilidownload.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.kongzue.dialogx.impl.ActivityLifecycleImpl
import com.kongzue.dialogx.interfaces.BaseDialog
import com.sgpublic.bilidownload.util.MyLog
import java.lang.ref.WeakReference
import java.util.*

class ConfigManager {
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

        private val instance = ConfigManager()
        private lateinit var contextWeakReference: WeakReference<Activity>

        fun init(context: Application){
            ActivityLifecycleImpl.init(context) {
                contextWeakReference = WeakReference(it)
            }
        }

        fun getString(key: String, defValue: String = "") = instance.sharedPreferences.getString(key, defValue).toString()
        fun getInt(key: String, defValue: Int = 0) = instance.sharedPreferences.getInt(key, defValue)
        fun getLong(key: String, defValue: Long = 0L) = instance.sharedPreferences.getLong(key, defValue)
        fun getBoolean(key: String, defValue: Boolean = false) = instance.sharedPreferences.getBoolean(key, defValue)

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
            var qualityIndex = 2
            val qualitySet = getInt("quality", QUALITIES_INT[qualityIndex])
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
            putInt("quality", QUALITIES_INT[qualityIndex])
            return QualityItem(
                    QUALITIES_DESCRIPTION[qualityIndex],
                    QUALITIES_INT[qualityIndex],
                    qualityIndex
            )
        }

        fun checkClient(context: Context): ClientItem? {
            val clientItems: ArrayList<ClientItem> = getInstalledClients(context)
            var packageName = getString("package", TYPES_PACK[0])
            var packageIndex = -1
            var result: ClientItem? = null
            if (clientItems.size > 0) {
                for (index in clientItems.indices) {
                    if (clientItems[index].packageName == packageName) {
                        packageIndex = index
                        break
                    }
                }
                if (packageIndex < 0) {
                    packageIndex = 0
                }
                packageName = clientItems[packageIndex].packageName
                putString("package", packageName)
                result = ClientItem(TYPES_STRING[packageIndex], packageName)
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

    private val sharedPreferences: SharedPreferences
    get() = contextWeakReference.get()!!.getSharedPreferences("user", Context.MODE_PRIVATE)
    
    class ClientItem internal constructor(val name: String, val packageName: String)

    class QualityItem internal constructor(val name: String, val quality: Int, val index: Int)
}