package com.cetc.lithium_battery.data.api

import com.cetc.lithium_battery.data.model.CommentPage
import com.cetc.lithium_battery.data.model.ForumPage
import com.cetc.lithium_battery.data.model.ForumMessagePage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.data.model.UserInfo
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.model.UserSharePage

interface ForumDataSource {
    suspend fun getSquarePosts(page: Int): ForumPage
    suspend fun getQuestionPosts(page: Int): ForumPage
    suspend fun getPopularQuestions(): List<ForumPost>
    suspend fun getQuestionComments(articleId: Int): CommentPage
    suspend fun login(username: String, password: String): UserInfo
    suspend fun logout()
    suspend fun getUserProfile(): UserProfile
    suspend fun getHotKeys(): List<HotKey>
    suspend fun searchPosts(keyword: String, page: Int): ForumPage
    suspend fun getCollectedPosts(page: Int): ForumPage
    suspend fun collectArticle(articleId: Int)
    suspend fun uncollectArticle(articleId: Int)
    suspend fun uncollectCollectedArticle(collectId: Int, originId: Int)
    suspend fun getUserSharedPosts(userId: Int, page: Int): UserSharePage
    suspend fun getMySharedPosts(page: Int): UserSharePage
    suspend fun shareArticle(title: String, link: String)
    suspend fun deleteSharedArticle(articleId: Int)
    suspend fun getUnreadMessageCount(): Int
    suspend fun getUnreadMessages(page: Int): ForumMessagePage
    suspend fun getReadMessages(page: Int): ForumMessagePage
}
