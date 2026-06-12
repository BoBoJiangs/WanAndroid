package com.cetc.lithium_battery.core

import android.content.Context
import com.cetc.lithium_battery.data.api.ForumApiService
import com.cetc.lithium_battery.data.api.RemoteForumDataSource
import com.cetc.lithium_battery.data.auth.AuthStore
import com.cetc.lithium_battery.data.auth.PersistentCookieJar
import com.cetc.lithium_battery.data.repository.DefaultForumRepository
import com.cetc.lithium_battery.data.repository.ForumRepository
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(context: Context) {
    private val authStore = AuthStore(context)
    private val cookieJar = PersistentCookieJar(context)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(DEFAULT_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val apiService = retrofit.create(ForumApiService::class.java)

    val forumRepository: ForumRepository = DefaultForumRepository(
        remoteDataSource = RemoteForumDataSource(apiService),
        authStore = authStore,
        cookieJar = cookieJar
    )

    companion object {
        const val DEFAULT_API_BASE_URL = "https://wanandroid.com/"
    }
}
