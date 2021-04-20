package com.sgpublic.bilidownload.module

import com.sgpublic.bilidownload.util.MyLog
import okhttp3.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

class BaseAPI(private val accessToken: String) {
    companion object {
        private const val build = "5442100"
        private const val android_key = "4409e2ce8ffd12b8"
        private const val platform = "android"
        private const val METHOD_GET = 0
        private const val METHOD_POST = 1
        val ts: String get() = System.currentTimeMillis().toString()
    }

    constructor(): this("")

    fun getKeyRequest(): Call {
        val url = "https://passport.bilibili.com/api/oauth2/getKey"
        val argArray: Map<String, Any> = mutableMapOf(
            "appkey" to android_key,
            "mobi_app" to platform,
            "platform" to platform,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_POST, true)
    }

    fun getLoginRequest(username: String, password_encrypted: String): Call {
        val url = "https://passport.bilibili.com/api/v3/oauth2/login"
        val argArray: Map<String, Any> = mutableMapOf(
            "appkey" to android_key,
            "build" to build,
            "gee_type" to 10,
            "mobi_app" to platform,
            "password" to password_encrypted,
            "platform" to platform,
            "ts" to ts,
            "username" to username,
        )
        val headerArray: Map<String, Any> = mutableMapOf(
            "User-Agent" to "Mozilla/5.0 (bbcallen@gmail.com)",
        )
        return onReturn(url, argArray, headerArray, METHOD_POST, true)
    }

    fun getLoginWebRequest(cookie: String, user_agent: String = "Mozilla/5.0 (bbcallen@gmail.com)"): Call {
        val url = "https://passport.bilibili.com/login/app/third"
        val argArray: Map<String, Any> = mutableMapOf(
            "appkey" to "27eb53fc9058f8c3",
            "api" to "http://link.acg.tv/forum.php",
            "sign" to "67ec798004373253d60114caaad89a8c",
        )
        val headerArray: Map<String, Any> = mutableMapOf(
            "Cookie" to cookie,
            "User-Agent" to "Mozilla/5.0 (sgpublic2002@gmail.com)",
        )
        return onReturn(url, argArray, headerArray, METHOD_GET, false)
    }

    fun getLoginConfirmRequest(url: String, cookie: String, userAgent: String = "Mozilla/5.0 (bbcallen@gmail.com)"): Call {
        val headerArray: Map<String, Any> = mutableMapOf(
            "Connection" to "keep-alive",
            "Upgrade-Insecure-Requests" to 1,
            "User-Agent" to userAgent,
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*,q=0.8",
            "Accept-Encoding" to "gzip, deflate",
            "Accept-Language" to "zh-CH,en-US;q=0.8",
            "Cookie" to cookie,
            "X-Requested-With" to "com.sgpublic.bilidownload",
        )
        return onReturn(url, null, headerArray, METHOD_GET, false)
    }

    fun getUserInfoRequest(mid: String): Call {
        val url = "https://api.bilibili.com/x/space/acc/info"
        val argArray: Map<String, Any> = mutableMapOf(
            "mid" to mid,
        )
        return onReturn(url, argArray, null, METHOD_GET, false)
    }

    fun getFollowsRequest(mid: Long, pageIndex: Int, status: Int): Call {
        val url = "https://api.bilibili.com/pgc/app/follow/v2/bangumi"
        val argArray: Map<String, Any> = mutableMapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "mid" to mid,
            "pn" to pageIndex,
            "ps" to 18,
            "status" to status,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_GET, true)
    }

    fun getHotWordRequest(): Call {
        val url = "https://s.search.bilibili.com/main/hotword"
        return onReturn(url, null, null, METHOD_GET, false)
    }

    fun getSearchResultRequest(keyword: String): Call {
        val url = "https://api.bilibili.com/x/web-interface/search/type"
        val argArray: Map<String, Any> = mutableMapOf(
            "search_type" to "media_bangumi",
            "keyword" to keyword,
        )
        val headerArray: Map<String, Any> = mutableMapOf(
            "Referer" to "https://search.bilibili.com",
        )
        return onReturn(url, argArray, headerArray, METHOD_GET, false)
    }

    fun getSearchSuggestRequest(keyword: String): Call {
        val url = "https://s.search.bilibili.com/main/suggest"
        val argArray: Map<String, Any> = mutableMapOf(
            "main_ver" to "v1",
            "special_acc_num" to 1,
            "topic_acc_num" to 1,
            "upuser_acc_num" to 3,
            "tag_num" to 10,
            "special_num" to 10,
            "bangumi_num" to 10,
            "upuser_num" to 3,
            "term" to keyword,
        )
        val headerArray: Map<String, Any> = mutableMapOf(
            "Referer" to "https://search.bilibili.com",
        )
        return onReturn(url, argArray, headerArray, METHOD_GET, false)
    }

    fun getSeasonInfoAppRequest(sid: Long): Call {
        val url = "https://api.bilibili.com/pgc/view/app/season"
        val argArray: Map<String, Any> = mutableMapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "platform" to platform,
            "season_id" to sid,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_GET, true)
    }

    fun getSeasonInfoWebRequest(sid: Long): Call {
        val url = "https://api.bilibili.com/pgc/view/web/season"
        val argArray: Map<String, Any> = mutableMapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "c_locale" to "hk_CN",
            "platform" to platform,
            "s_locale" to "hk_CN",
            "season_id" to sid,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_GET, true)
    }

    @Deprecated("")
    fun getSeasonInfoOldRequest(sid: Long): Call {
        val url = "https://bangumi.bilibili.com/view/web_api/season"
        val argArray: Map<String, Any> = mutableMapOf(
            "season_id" to sid,
        )
        return onReturn(url, argArray, null, METHOD_GET, false)
    }

    fun getEpisodeOfficialRequest(cid: Long, qn: Int): Call {
        val url = "https://api.bilibili.com/pgc/player/api/playurl"
        val argArray: Map<String, Any> = mutableMapOf(
            "access_key" to accessToken,
            "build" to build,
            "cid" to cid,
            "fnval" to 16,
            "fnver" to 0,
            "fourk" to 1,
            "module" to "bangumi",
            "otype" to "otype",
            "platform" to platform,
            "qn" to qn,
            "season_type" to 1,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_GET, true)
    }

    fun getEpisodeBiliplusRequest(cid: Long, qn: Int): Call {
        val url = "https://www.biliplus.com/BPplayurl.php"
        val argArray: Map<String, Any> = mutableMapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "cid" to cid,
            "fnval" to 16,
            "fnver" to 0,
            "fourk" to 1,
            "module" to "bangumi",
            "otype" to "otype",
            "platform" to platform,
            "qn" to qn,
            "season_type" to 1,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_GET, true)
    }

    fun getEpisodeKghostRequest(cid: Long, qn: Int): Call {
        val url = "https://bilibili-tw-api.kghost.info/pgc/player/web/playurl"
        val argArray: Map<String, Any> = mutableMapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "cid" to cid,
            "device" to platform,
            "fnval" to 16,
            "fnver" to 0,
            "fourk" to 1,
            "module" to "bangumi",
            "otype" to "otype",
            "platform" to platform,
            "qn" to qn,
            "season_type" to 1,
            "ts" to ts,
        )
        return onReturn(url, argArray, null, METHOD_GET, true)
    }

    fun getProxyRequest_iill(call: Call): Call {
        val url = "https://biliproxy.iill.moe/"
        val argArray: Map<String, Any> = mutableMapOf(
            "url" to call.request().url,
        )
        return onReturn(url, argArray, null, METHOD_GET, false)
    }

    fun getDanmakuRequest(cid: Long): Call {
        val url = "https://api.bilibili.com/x/v1/dm/list.so"
        val argArray: Map<String, Any> = mutableMapOf(
            "oid" to cid,
        )
        return onReturn(url, argArray, null, METHOD_GET, false)
    }

    fun getUpdateRequest(version: String): Call {
        val url = "https://sgpublic.xyz/bilidl/update/index.php"
        val argArray: Map<String, Any> = mutableMapOf(
            "version" to version,
        )
        return onReturn(url, argArray, null, METHOD_POST, false)
    }

    private fun onReturn(url: String, argArray: Map<String, Any>?, headerArray: Map<String, Any>?,
                         method: Int = METHOD_POST, withSign: Boolean = true): Call {
        val client: OkHttpClient = OkHttpClient.Builder().run{
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(5, TimeUnit.SECONDS)
            connectTimeout(10, TimeUnit.SECONDS)
            callTimeout(5, TimeUnit.MINUTES)
            followRedirects(false)
            followSslRedirects(false)
            build()
        }
        val request: Request = Request.Builder().run {
            val body = GetArgs(argArray)
            if (method == METHOD_POST) {
                MyLog.v("HTTP请求：POST $url, [Body]" + body.getString(withSign))
                url(url)
                post(body.getForm(withSign))
            } else {
                val urlFinal = url + "?" + body.getString(withSign)
                MyLog.v("HTTP请求：GET $urlFinal")
                url(urlFinal)
            }
            if (headerArray != null) {
                for ((key, value) in headerArray) {
                    addHeader(key, value.toString())
                }
            }
            build()
        }
        return client.newCall(request)
    }

    private class GetArgs(val argArray: Map<String, Any>?){
        private var string: String
        init {
            string = StringBuilder().run {
                argArray?.let{
                    for ((argName, argValue) in it){
                        val argValueDecoded = argValue.toString()
                        append("&$argName=$argValueDecoded")
                    }
                }
                toString()
            }
            if (string.length > 1){
                string = string.substring(1)
            }
        }

        fun getString(outSign: Boolean): String {
            return StringBuilder(string).run {
                if (outSign){
                    append("&sign=" + getSign())
                }
                toString()
            }
        }

        fun getForm(outSign: Boolean): FormBody {
            return FormBody.Builder().run {
                argArray?.let {
                    for ((argName, argValue) in argArray){
                        val argValueDecoded = argValue.toString()
                        add(argName, argValueDecoded)
                    }
                }
                if (outSign){
                    add("sign", getSign())
                }
                build()
            }
        }

        private fun getSign(): String {
            val content = string + "59b43e04ad6965f34319062b478f83dd"
            try {
                val instance:MessageDigest = MessageDigest.getInstance("MD5")
                val digest:ByteArray = instance.digest(content.toByteArray())
                return StringBuffer().run {
                    for (b in digest) {
                        val i :Int = b.toInt() and 0xff
                        var hexString = Integer.toHexString(i)
                        if (hexString.length < 2) {
                            hexString = "0$hexString"
                        }
                        append(hexString)
                    }
                    toString()
                }
            } catch (e: NoSuchAlgorithmException) {
                return ""
            }
        }
    }
}