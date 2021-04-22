package com.sgpublic.bilidownload.module

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.core.content.FileProvider
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.manager.DownloadTaskManager
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

class UpdateModule(private val context: Context) {
    private lateinit var callbackPrivate: Callback

    companion object {
        fun listener(context: Context, Id: Long) {
            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val manager: DownloadManager =
                            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val ID: Long = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (ID == Id) {
                        val query: DownloadManager.Query = DownloadManager.Query()
                        query.setFilterById(Id)
                        val cursor: Cursor = manager.query(query)
                        if (cursor.moveToFirst()) {
                            val fileName =
                                    cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            fileName?.let { openAPK(context, it) }
                        }
                        cursor.close()
                    }
                }
            }
            context.applicationContext.registerReceiver(broadcastReceiver, intentFilter)
        }

        private fun openAPK(context: Context, fileSavePath: String) {
            val file = File(Uri.parse(fileSavePath).path.toString())
            val filePath = file.absolutePath
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_GRANT_READ_URI_PERMISSION
            val data: Uri = FileProvider.getUriForFile(
                    context.applicationContext,
                    "com.sgpublic.bilidownload.fileprovider",
                    File(filePath)
            )
            intent.setDataAndType(data, "application/vnd.android.package-archive")
            context.startActivity(intent)
        }
    }

    fun getUpdate(type: Int, callback: Callback) {
        val helper = BaseAPI()
        this.callbackPrivate = callback
        val version: String = if (type != 1 && !BaseAPI.ts.endsWith("387")) {
            "release"
        } else { "debug" }
        val call = helper.getUpdateRequest(version)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callbackPrivate.onFailure(-711, context.getString(R.string.error_network), e)
                } else {
                    callbackPrivate.onFailure(-712, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val verCodeNow = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionCode
                    val json = JSONObject(result)
                    val updateTable: JSONObject = json.getJSONObject("latest")
                    val disable: Long = updateTable.getInt("disable").toLong()
                    if (disable == 0L) {
                        val verCode: Long = updateTable.getInt("ver_code").toLong()
                        if (verCode > verCodeNow) {
                            val urlDl = ("https://sgpublic.xyz/bilidl/update/apk/app-$version.apk")
                            val verName: String = updateTable.getString("ver_name")
                            val sizeString: String = DownloadTaskManager.getSizeString(urlDl)
                            if (version == "debug") {
                                if (ConfigManager.getString("beta", "") != verName) {
                                    ConfigManager.putString("beta", verName)
                                    callbackPrivate.onUpdate(
                                        0, verName, sizeString,
                                        updateTable.getString("changelog"), urlDl
                                    )
                                } else {
                                    callbackPrivate.onUpToDate()
                                }
                            } else {
                                val is_force =
                                    if (updateTable.getInt("force_ver") > verCodeNow) 1 else 0
                                callbackPrivate.onUpdate(
                                    is_force, verName, sizeString,
                                    updateTable.getString("changelog"), urlDl
                                )
                            }
                        } else {
                            callbackPrivate.onUpToDate()
                        }
                    } else {
                        var disable_reason: String = updateTable.getString("disable_reason")
                        if (disable_reason == "") {
                            disable_reason = context.getString(R.string.text_update_disable_unknown)
                        }
                        callbackPrivate.onDisabled(
                            disable, disable_reason
                        )
                    }
                } catch (e: JSONException) {
                    callbackPrivate.onFailure(-703, null, e)
                } catch (e: PackageManager.NameNotFoundException) {
                    callbackPrivate.onFailure(-705, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable)
        fun onUpToDate()
        fun onUpdate(force: Int, verName: String, sizeString: String, changelog: String, dlUrl: String)
        fun onDisabled(time: Long, reason: String)
    }
}