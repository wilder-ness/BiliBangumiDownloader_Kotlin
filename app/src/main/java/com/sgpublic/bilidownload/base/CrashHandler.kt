package com.sgpublic.bilidownload.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Looper
import com.kongzue.dialogx.impl.ActivityLifecycleImpl
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.util.ActivityCollector
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class CrashHandler private constructor(private val context: Activity) : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var clipboard: ClipboardManager? = null

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler!!.uncaughtException(thread, ex)
        }
    }

    private fun handleException(e: Throwable?): Boolean {
        if (e == null) {
            return false
        }
        val logContent = saveExplosion(e, -100) ?: return false
        Thread {
            Looper.prepare()
//            ExceptionDialog.startActivity(context, logContent)
            AlertDialog.Builder(context)
                    .setTitle(R.string.title_function_crash)
                    .setMessage(context.getString(R.string.text_function_crash) + "\n错误信息" + e.message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.text_function_crash_copy) { _, _ ->
                        val data = ClipData.newPlainText("Label", logContent)
                        clipboard?.setPrimaryClip(data)
                    }
                    .setNegativeButton(R.string.text_function_crash_exit, null)
                    .setOnDismissListener {
                        ActivityCollector.finishAll()
                    }
                    .show()
            Looper.loop()
        }.start()
        return true
    }

    companion object {
        private var logPath: String? = null

        fun init(context: Application) {
            ActivityLifecycleImpl.init(context) {
                val instance = CrashHandler(it)
                logPath = it.applicationContext.getExternalFilesDir("log")?.path
                if (instance.mDefaultHandler == null) {
                    instance.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                    instance.clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    Thread.setDefaultUncaughtExceptionHandler(instance)
                }
            }
        }

        fun saveExplosion(e: Throwable?, code: Int): String? {
            if (this.logPath == null) {
                return null
            }
            try {
                if (e == null) {
                    return null
                }
                val exceptionLog: JSONObject
                var exceptionLogContent = JSONArray()
                val exception = File(logPath, "exception.json")
                var logContent: String
                try {
                    val fileInputStream = FileInputStream(exception)
                    val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
                    var line: String?
                    val stringBuilder = StringBuilder()
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    logContent = stringBuilder.toString()
                } catch (e1: IOException) {
                    logContent = ""
                }
                if (logContent != "") {
                    exceptionLog = JSONObject(logContent)
                    if (!exceptionLog.isNull("logs")) {
                        exceptionLogContent = exceptionLog.getJSONArray("logs")
                    }
                }
                val elements = e.stackTrace
                val crashMsgJson = JSONObject()
                val crashMsgArray = JSONArray()
                val crashMsgArrayIndex = JSONObject()
                val crashStackTrace = JSONArray()
                for (element_index in elements) {
                    val crashStackTraceIndex = JSONObject()
                    crashStackTraceIndex.put("class", element_index.className)
                    crashStackTraceIndex.put("line", element_index.lineNumber)
                    crashStackTraceIndex.put("method", element_index.methodName)
                    crashStackTrace.put(crashStackTraceIndex)
                }
                val configString = StringBuilder(e.toString())
                for (config_index in 0..2) {
                    configString.append("\nat ").append(elements[config_index].toString())
                }
                ConfigManager.putString("last_exception", configString.toString())
                crashMsgArrayIndex.put("code", code)
                crashMsgArrayIndex.put("message", e.toString())
                crashMsgArrayIndex.put("stack_trace", crashStackTrace)
                crashMsgArray.put(crashMsgArrayIndex)
                var exceptionLogIndex = 0
                while (exceptionLogIndex < exceptionLogContent.length() && exceptionLogIndex < 2) {
                    val msgIndex = exceptionLogContent.getJSONObject(exceptionLogIndex)
                    if (crashMsgArrayIndex.toString() != msgIndex.toString()) {
                        crashMsgArray.put(msgIndex)
                    }
                    exceptionLogIndex++
                }
                crashMsgJson.put("logs", crashMsgArray)
                val fileOutputStream = FileOutputStream(exception)
                val logs = crashMsgJson.toString().toByteArray()
                fileOutputStream.write(logs)
                fileOutputStream.close()
                return logs.toString()
            } catch (ignore: Exception) { }
            return null
        }
    }
}