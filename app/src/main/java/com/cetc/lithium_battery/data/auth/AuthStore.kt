package com.cetc.lithium_battery.data.auth

import android.content.Context
import com.cetc.lithium_battery.data.model.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthSession(
    val user: UserInfo? = null
) {
    val isLoggedIn: Boolean
        get() = user != null
}

interface SessionStore {
    val session: StateFlow<AuthSession>
    fun saveUser(user: UserInfo)
    fun clear()
}

class AuthStore(context: Context) : SessionStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(AuthSession(loadUser()))
    override val session = _session.asStateFlow()

    override fun saveUser(user: UserInfo) {
        prefs.edit()
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_NICKNAME, user.nickname)
            .putString(KEY_PUBLIC_NAME, user.publicName)
            .putString(KEY_EMAIL, user.email)
            .putInt(KEY_COIN_COUNT, user.coinCount)
            .apply()
        _session.value = AuthSession(loadUser())
    }

    override fun clear() {
        prefs.edit().clear().apply()
        _session.value = AuthSession()
    }

    private fun loadUser(): UserInfo? {
        val id = prefs.getInt(KEY_USER_ID, 0)
        val username = prefs.getString(KEY_USERNAME, "").orEmpty()
        if (id <= 0 && username.isBlank()) {
            return null
        }
        return UserInfo(
            id = id,
            username = username,
            nickname = prefs.getString(KEY_NICKNAME, "").orEmpty(),
            publicName = prefs.getString(KEY_PUBLIC_NAME, "").orEmpty(),
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            coinCount = prefs.getInt(KEY_COIN_COUNT, 0)
        )
    }

    companion object {
        private const val PREFS_NAME = "wan_android_auth"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_PUBLIC_NAME = "public_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_COIN_COUNT = "coin_count"
    }
}
