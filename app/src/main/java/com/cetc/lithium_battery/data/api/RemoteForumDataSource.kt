package com.cetc.lithium_battery.data.api

import com.cetc.lithium_battery.data.model.WanAndroidResponse

class RemoteForumDataSource(
    private val apiService: ForumApiService
) : ForumDataSource {
    override suspend fun getSquarePosts(page: Int) =
        apiService.getSquarePosts(page).requireData()

    override suspend fun getQuestionPosts(page: Int) =
        apiService.getQuestionPosts(page).requireData()

    override suspend fun getPopularQuestions() =
        apiService.getPopularQuestions().requireData()

    override suspend fun getQuestionComments(articleId: Int) =
        apiService.getQuestionComments(articleId).requireData()

    override suspend fun login(username: String, password: String) =
        apiService.login(username, password).requireData()

    override suspend fun logout() =
        apiService.logout().requireSuccess()

    override suspend fun getUserProfile() =
        apiService.getUserProfile().requireData()

    override suspend fun getHotKeys() =
        apiService.getHotKeys().requireData()

    override suspend fun searchPosts(keyword: String, page: Int) =
        apiService.searchPosts(page, keyword).requireData()

    override suspend fun getCollectedPosts(page: Int) =
        apiService.getCollectedPosts(page).requireData()

    override suspend fun collectArticle(articleId: Int) =
        apiService.collectArticle(articleId).requireSuccess()

    override suspend fun uncollectArticle(articleId: Int) =
        apiService.uncollectArticle(articleId).requireSuccess()

    override suspend fun uncollectCollectedArticle(collectId: Int, originId: Int) =
        apiService.uncollectCollectedArticle(collectId, originId).requireSuccess()

    override suspend fun getUserSharedPosts(userId: Int, page: Int) =
        apiService.getUserSharedPosts(userId, page).requireData()

    override suspend fun getMySharedPosts(page: Int) =
        apiService.getMySharedPosts(page).requireData()

    override suspend fun shareArticle(title: String, link: String) =
        apiService.shareArticle(title, link).requireSuccess()

    override suspend fun deleteSharedArticle(articleId: Int) =
        apiService.deleteSharedArticle(articleId).requireSuccess()

    override suspend fun getUnreadMessageCount() =
        apiService.getUnreadMessageCount().requireData()

    override suspend fun getUnreadMessages(page: Int) =
        apiService.getUnreadMessages(page).requireData()

    override suspend fun getReadMessages(page: Int) =
        apiService.getReadMessages(page).requireData()

    private fun <T> WanAndroidResponse<T>.requireData(): T {
        if (errorCode == 0 && data != null) {
            return data
        }
        throw asException()
    }

    private fun WanAndroidResponse<*>.requireSuccess() {
        if (errorCode == 0) {
            return
        }
        throw asException()
    }

    private fun WanAndroidResponse<*>.asException(): Throwable {
        return if (errorCode == AUTH_REQUIRED_CODE) {
            AuthRequiredException(errorMsg)
        } else {
            WanAndroidException(errorCode, errorMsg)
        }
    }
}
