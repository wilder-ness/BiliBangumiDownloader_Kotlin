package com.sgpublic.bilidownload.base

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.sgpublic.bilidownload.R
import com.sgpublic.bilidownload.manager.ConfigManager
import com.sgpublic.bilidownload.util.ActivityCollector
import com.sgpublic.bilidownload.util.MyLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

object CrashHandler {
    private var logPath: String? = null

    fun init(context: Application) {
        logPath = context.getExternalFilesDir("log")?.path

        Handler(context.mainLooper).post {
            while (true){
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    MyLog.e("应用意外停止", e)
                    val logContent = saveExplosion(e, -100) ?: continue
                    onExceptionCatch(context, logContent)
                }
            }
        }
    }

    private fun onExceptionCatch(context: Application, logContent: String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        MessageDialog.build()
            .setTitle(R.string.title_function_crash)
            .setMessage(R.string.text_function_crash)
            .setCancelable(false)
            .setDialogLifecycleCallback(object : DialogLifecycleCallback<MessageDialog>() {
                override fun onDismiss(dialog: MessageDialog?) {
                    super.onDismiss(dialog)
                    ActivityCollector.finishAll()
                }
            })
            .setOkButton(R.string.text_function_crash_copy) { _, _ ->
                val data = ClipData.newPlainText("Label", logContent)
                clipboard.setPrimaryClip(data)
                Toast.makeText(context, R.string.text_function_crash_copy_success, Toast.LENGTH_SHORT).show()
                false
            }
            .setCancelButton(R.string.text_function_crash_exit)
            .show()
    }

    fun saveExplosion(e: Throwable?, code: Int): String? {
        this.logPath ?: return null
        try {
            e ?: return null
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
            val logs = crashMsgJson.toString()
            fileOutputStream.write(logs.toByteArray())
            fileOutputStream.close()
            return logs
        } catch (ignore: Exception) { }
        return null
    }
}