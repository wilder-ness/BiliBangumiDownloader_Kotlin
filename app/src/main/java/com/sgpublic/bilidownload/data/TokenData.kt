package com.sgpublic.bilidownload.data

data class TokenData (
        var accessToken: String = "",
        var refreshToken: String = "",
        var expiresIn: Long = 0L
)