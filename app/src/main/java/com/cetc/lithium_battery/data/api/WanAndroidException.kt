package com.cetc.lithium_battery.data.api

open class WanAndroidException(
    val code: Int,
    message: String
) : IllegalStateException(message.ifBlank { "WanAndroid request failed: $code" })

class AuthRequiredException(
    message: String
) : WanAndroidException(AUTH_REQUIRED_CODE, message.ifBlank { "请先登录" })

const val AUTH_REQUIRED_CODE = -1001
