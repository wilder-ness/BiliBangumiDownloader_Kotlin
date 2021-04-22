package com.sgpublic.bilidownload.util

import android.util.Log
import com.sgpublic.bilidownload.BuildConfig

object MyLog {
    private val out = BuildConfig.DEBUG

    fun v(message: Any) {
        doLog(object : DoLogSimplify {
            override fun onLog(tag: String, message: String) {
                Log.v(tag, message)
            }
        }, message)
    }

    fun v(message: Any, e: Throwable) {
        doLog(object : DoLog {
            override fun onLog(tag: String, message: String, e: Throwable) {
                Log.v(tag, message)
            }
        }, message, e)
    }

    fun d(message: Any) {
        doLog(object : DoLogSimplify {
            override fun onLog(tag: String, message: String) {
                Log.d(tag, message)
            }
        }, message)
    }

    fun d(message: Any, e: Throwable) {
        doLog(object : DoLog {
            override fun onLog(tag: String, message: String, e: Throwable) {
                Log.d(tag, message)
            }
        }, message, e)
    }

    fun i(message: Any) {
        doLog(object : DoLogSimplify {
            override fun onLog(tag: String, message: String) {
                Log.w(tag, message)
            }
        }, message)
    }

    fun i(message: Any, e: Throwable) {
        doLog(object : DoLog {
            override fun onLog(tag: String, message: String, e: Throwable) {
                Log.i(tag, message)
            }
        }, message, e)
    }

    fun w(message: Any) {
        doLog(object : DoLogSimplify {
            override fun onLog(tag: String, message: String) {
                Log.w(tag, message)
            }
        }, message)
    }

    fun w(message: Any, e: Throwable) {
        doLog(object : DoLog {
            override fun onLog(tag: String, message: String, e: Throwable) {
                Log.w(tag, message)
            }
        }, message, e)
    }

    fun e(message: Any) {
        doLog(object : DoLogSimplify {
            override fun onLog(tag: String, message: String) {
                Log.e(tag, message)
            }
        }, message)
    }

    fun e(msg: Any, e: Throwable) {
        doLog(object : DoLog {
            override fun onLog(tag: String, message: String, e: Throwable) {
                Log.e("MyLog", msg.toString(), e)
            }
        }, msg, e)
    }

    private fun doLog(doLog: DoLogSimplify, message: Any) {
        if (!out) {
            return
        }
        val ste = Throwable().stackTrace[2]
        val tagName = "MyLog (" + ste.fileName + ":" + ste.lineNumber + ")"
        val messageString = message.toString()
        if (messageString.length > 1024) {
            var index = 0
            while (index < messageString.length - 1024) {
                val out = messageString.substring(index, index + 1024)
                doLog.onLog(tagName, out)
                index += 1024
            }
            doLog.onLog(tagName, messageString.substring(index))
        } else {
            doLog.onLog(tagName, messageString)
        }
    }

    private fun doLog(doLog: DoLog, message: Any, e: Throwable) {
        if (!out) {
            return
        }
        val ste = Throwable().stackTrace[2]
        val tagName = "MyLog (" + ste.fileName + ":" + ste.lineNumber + ")"
        val messageString = message.toString()
        if (messageString.length > 1024) {
            var index = 0
            while (index < messageString.length - 1024) {
                val out = messageString.substring(index, index + 1024)
                doLog.onLog(tagName, out, e)
                index += 1024
            }
            doLog.onLog(tagName, messageString.substring(index), e)
        } else {
            doLog.onLog(tagName, messageString + "，[" + e.javaClass.canonicalName + "] " + e.localizedMessage, e)
        }
    }

    private interface DoLogSimplify {
        fun onLog(tag: String, message: String)
    }

    private interface DoLog {
        fun onLog(tag: String, message: String, e: Throwable)
    }
}