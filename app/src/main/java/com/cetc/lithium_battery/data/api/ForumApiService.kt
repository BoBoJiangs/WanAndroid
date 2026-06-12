package com.cetc.lithium_battery.data.api

import com.cetc.lithium_battery.data.model.CommentPage
import com.cetc.lithium_battery.data.model.ForumPage
import com.cetc.lithium_battery.data.model.ForumMessagePage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.data.model.UserInfo
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.model.UserSharePage
import com.cetc.lithium_battery.data.model.WanAndroidResponse
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ForumApiService {
    @GET("user_article/list/{page}/json")
    suspend fun getSquarePosts(
        @Path("page") page: Int
    ): WanAndroidResponse<ForumPage>

    @GET("wenda/list/{page}/json")
    suspend fun getQuestionPosts(
        @Path("page") page: Int
    ): WanAndroidResponse<ForumPage>

    @GET("popular/wenda/json")
    suspend fun getPopularQuestions(): WanAndroidResponse<List<ForumPost>>

    @GET("wenda/comments/{articleId}/json")
    suspend fun getQuestionComments(
        @Path("articleId") articleId: Int
    ): WanAndroidResponse<CommentPage>

    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): WanAndroidResponse<UserInfo>

    @GET("user/logout/json")
    suspend fun logout(): WanAndroidResponse<JsonElement>

    @GET("user/lg/userinfo/json")
    suspend fun getUserProfile(): WanAndroidResponse<UserProfile>

    @GET("hotkey/json")
    suspend fun getHotKeys(): WanAndroidResponse<List<HotKey>>

    @FormUrlEncoded
    @POST("article/query/{page}/json")
    suspend fun searchPosts(
        @Path("page") page: Int,
        @Field("k") keyword: String
    ): WanAndroidResponse<ForumPage>

    @GET("lg/collect/list/{page}/json")
    suspend fun getCollectedPosts(
        @Path("page") page: Int
    ): WanAndroidResponse<ForumPage>

    @POST("lg/collect/{articleId}/json")
    suspend fun collectArticle(
        @Path("articleId") articleId: Int
    ): WanAndroidResponse<JsonElement>

    @POST("lg/uncollect_originId/{articleId}/json")
    suspend fun uncollectArticle(
        @Path("articleId") articleId: Int
    ): WanAndroidResponse<JsonElement>

    @FormUrlEncoded
    @POST("lg/uncollect/{collectId}/json")
    suspend fun uncollectCollectedArticle(
        @Path("collectId") collectId: Int,
        @Field("originId") originId: Int
    ): WanAndroidResponse<JsonElement>

    @GET("user/{userId}/share_articles/{page}/json")
    suspend fun getUserSharedPosts(
        @Path("userId") userId: Int,
        @Path("page") page: Int
    ): WanAndroidResponse<UserSharePage>

    @GET("user/lg/private_articles/{page}/json")
    suspend fun getMySharedPosts(
        @Path("page") page: Int
    ): WanAndroidResponse<UserSharePage>

    @FormUrlEncoded
    @POST("lg/user_article/add/json")
    suspend fun shareArticle(
        @Field("title") title: String,
        @Field("link") link: String
    ): WanAndroidResponse<JsonElement>

    @POST("lg/user_article/delete/{articleId}/json")
    suspend fun deleteSharedArticle(
        @Path("articleId") articleId: Int
    ): WanAndroidResponse<JsonElement>

    @GET("message/lg/count_unread/json")
    suspend fun getUnreadMessageCount(): WanAndroidResponse<Int>

    @GET("message/lg/unread_list/{page}/json")
    suspend fun getUnreadMessages(
        @Path("page") page: Int
    ): WanAndroidResponse<ForumMessagePage>

    @GET("message/lg/readed_list/{page}/json")
    suspend fun getReadMessages(
        @Path("page") page: Int
    ): WanAndroidResponse<ForumMessagePage>
}
