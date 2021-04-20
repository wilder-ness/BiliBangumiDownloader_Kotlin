package com.sgpublic.bilidownload.base

import android.app.Application
import android.content.Context
import com.sgpublic.bilidownload.util.ActivityCollector
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var context: Application

    companion object {
        private val instance = CrashHandler()

        fun init(context: Application) {
            instance.context = context
            if (instance.mDefaultHandler == null) {
                instance.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                Thread.setDefaultUncaughtExceptionHandler(instance)
            }
        }
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            ActivityCollector.finishAll()
        }
    }

    private fun handleException(e: Throwable?): Boolean {
        if (e == null) {
            return false
        }
        saveExplosion(context, e, -100)
        return true
    }

    fun saveExplosion(context: Context, e: Throwable?, code: Int) {
        try {
            if (e == null) {
                return
            }

            val exceptionLog: JSONObject
            var exceptionLogContent = JSONArray()
            val exception = File(
                    context.applicationContext.getExternalFilesDir("log")?.path,
                    "exception.json"
            )
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
            val editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
            editor.putString("last_exception", configString.toString())
            editor.apply()
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
            fileOutputStream.write(crashMsgJson.toString().toByteArray())
            fileOutputStream.close()
        } catch (ignore: Exception) { }
    }
}