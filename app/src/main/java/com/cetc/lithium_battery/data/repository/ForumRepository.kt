package com.cetc.lithium_battery.data.repository

import com.cetc.lithium_battery.data.auth.AuthSession
import com.cetc.lithium_battery.data.model.CommentPage
import com.cetc.lithium_battery.data.model.ForumPage
import com.cetc.lithium_battery.data.model.ForumMessagePage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.data.model.UserInfo
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.model.UserSharePage
import kotlinx.coroutines.flow.StateFlow

interface ForumRepository {
    val authSession: StateFlow<AuthSession>

    suspend fun getSquarePosts(page: Int): Result<ForumPage>
    suspend fun getQuestionPosts(page: Int): Result<ForumPage>
    suspend fun getPopularQuestions(): Result<List<ForumPost>>
    suspend fun getQuestionComments(articleId: Int): Result<CommentPage>
    suspend fun login(username: String, password: String): Result<UserInfo>
    suspend fun logout(): Result<Unit>
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun getHotKeys(): Result<List<HotKey>>
    suspend fun searchPosts(keyword: String, page: Int): Result<ForumPage>
    suspend fun getCollectedPosts(page: Int): Result<ForumPage>
    suspend fun collectArticle(articleId: Int): Result<Unit>
    suspend fun uncollectArticle(articleId: Int): Result<Unit>
    suspend fun uncollectCollectedArticle(collectId: Int, originId: Int): Result<Unit>
    suspend fun getUserSharedPosts(userId: Int, page: Int): Result<UserSharePage>
    suspend fun getMySharedPosts(page: Int): Result<UserSharePage>
    suspend fun shareArticle(title: String, link: String): Result<Unit>
    suspend fun deleteSharedArticle(articleId: Int): Result<Unit>
    suspend fun getUnreadMessageCount(): Result<Int>
    suspend fun getUnreadMessages(page: Int): Result<ForumMessagePage>
    suspend fun getReadMessages(page: Int): Result<ForumMessagePage>
}
