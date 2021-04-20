package com.sgpublic.bilidownload.module

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.sgpublic.bilidownload.util.DownloadTaskManager
import okhttp3.Call
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

class UpdateModule(private val context: Context) {
    private val TAG = "UpdateHelper"
    private var callback_private: Callback? = null
    fun getUpdate(type: Int, callback: Callback?) {
        val helper = BaseAPI()
        callback_private = callback
        val version: String
        version = if (type != 1 && !BaseAPI.Companion.getTS().endsWith("387")) {
            "release"
        } else {
            "debug"
        }
        val call = helper.getUpdateRequest(version)
        call!!.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback_private!!.onFailure(-711, context.getString(R.string.error_network), e)
                } else {
                    callback_private!!.onFailure(-712, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val ver_code_now = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionCode
                    val `object` = JSONObject(result)
                    val update_table: JSONObject = `object`.getJSONObject("latest")
                    val disable: Long = update_table.getInt("disable").toLong()
                    if (disable == 0L) {
                        val ver_code: Long = update_table.getInt("ver_code").toLong()
                        if (ver_code > ver_code_now) {
                            val url_dl = ("https://sgpublic.xyz/bilidl/update/apk/app-"
                                    + version + ".apk")
                            val ver_name: String = update_table.getString("ver_name")
                            val size_string: String = DownloadTaskManager.getSizeString(url_dl)
                            if (version == "debug") {
                                val sharedPreferences: SharedPreferences =
                                    context.getSharedPreferences("user", Context.MODE_PRIVATE)
                                if (sharedPreferences.getString("beta", "") != ver_name) {
                                    sharedPreferences.edit()
                                        .putString("beta", ver_name)
                                        .apply()
                                    callback_private!!.onUpdate(
                                        0, ver_name, size_string,
                                        update_table.getString("changelog"), url_dl
                                    )
                                } else {
                                    callback_private!!.onUpToDate()
                                }
                            } else {
                                val is_force =
                                    if (update_table.getInt("force_ver") > ver_code_now) 1 else 0
                                callback_private!!.onUpdate(
                                    is_force, ver_name, size_string,
                                    update_table.getString("changelog"), url_dl
                                )
                            }
                        } else {
                            callback_private!!.onUpToDate()
                        }
                    } else {
                        var disable_reason: String = update_table.getString("disable_reason")
                        if (disable_reason == "") {
                            disable_reason = context.getString(R.string.text_update_disable_unknown)
                        }
                        callback_private!!.onDisabled(
                            disable, disable_reason
                        )
                    }
                } catch (e: JSONException) {
                    callback_private!!.onFailure(-703, null, e)
                } catch (e: PackageManager.NameNotFoundException) {
                    callback_private!!.onFailure(-705, null, e)
                }
            }
        })
    }

    fun listener(Id: Long) {
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
                        fileName?.let { openAPK(it) }
                    }
                    cursor.close()
                }
            }
        }
        context.applicationContext.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun openAPK(fileSavePath: String) {
        val file = File(
            Objects
                .requireNonNull(Uri.parse(fileSavePath).path)
        )
        val filePath = file.absolutePath
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val data: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data = FileProvider.getUriForFile(
                context.applicationContext,
                "com.sgpublic.bilidownload.fileprovider",
                File(filePath)
            )
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            data = Uri.fromFile(file)
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onUpToDate()
        fun onUpdate(
            force: Int,
            ver_name: String?,
            size_string: String?,
            changelog: String?,
            dl_url: String?
        )

        fun onDisabled(time: Long, reason: String?)
    }
}