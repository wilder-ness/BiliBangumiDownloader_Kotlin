package com.sgpublic.bilidownload.data

data class TokenData (
        var access_token: String = "",
        var refresh_token: String = "",
        var expires_in: Long = 0L
)