package com.cetc.lithium_battery.data.repository

import com.cetc.lithium_battery.data.api.AuthRequiredException
import com.cetc.lithium_battery.data.api.ForumDataSource
import com.cetc.lithium_battery.data.auth.CookieStore
import com.cetc.lithium_battery.data.auth.SessionStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultForumRepository(
    private val remoteDataSource: ForumDataSource,
    private val authStore: SessionStore,
    private val cookieJar: CookieStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ForumRepository {
    override val authSession = authStore.session

    override suspend fun getSquarePosts(page: Int) = load {
        remoteDataSource.getSquarePosts(page)
    }

    override suspend fun getQuestionPosts(page: Int) = load {
        remoteDataSource.getQuestionPosts(page)
    }

    override suspend fun getPopularQuestions() = load {
        remoteDataSource.getPopularQuestions()
    }

    override suspend fun getQuestionComments(articleId: Int) = load {
        remoteDataSource.getQuestionComments(articleId)
    }

    override suspend fun login(username: String, password: String) = load {
        remoteDataSource.login(username, password).also(authStore::saveUser)
    }

    override suspend fun logout(): Result<Unit> {
        val result = load {
            remoteDataSource.logout()
        }
        clearLocalSession()
        return result
    }

    override suspend fun getUserProfile() = load {
        remoteDataSource.getUserProfile()
    }

    override suspend fun getHotKeys() = load {
        remoteDataSource.getHotKeys()
    }

    override suspend fun searchPosts(keyword: String, page: Int) = load {
        remoteDataSource.searchPosts(keyword, page)
    }

    override suspend fun getCollectedPosts(page: Int) = load {
        remoteDataSource.getCollectedPosts(page)
    }

    override suspend fun collectArticle(articleId: Int) = load {
        remoteDataSource.collectArticle(articleId)
    }

    override suspend fun uncollectArticle(articleId: Int) = load {
        remoteDataSource.uncollectArticle(articleId)
    }

    override suspend fun uncollectCollectedArticle(collectId: Int, originId: Int) = load {
        remoteDataSource.uncollectCollectedArticle(collectId, originId)
    }

    override suspend fun getUserSharedPosts(userId: Int, page: Int) = load {
        remoteDataSource.getUserSharedPosts(userId, page)
    }

    override suspend fun getMySharedPosts(page: Int) = load {
        remoteDataSource.getMySharedPosts(page)
    }

    override suspend fun shareArticle(title: String, link: String) = load {
        remoteDataSource.shareArticle(title, link)
    }

    override suspend fun deleteSharedArticle(articleId: Int) = load {
        remoteDataSource.deleteSharedArticle(articleId)
    }

    override suspend fun getUnreadMessageCount() = load {
        remoteDataSource.getUnreadMessageCount()
    }

    override suspend fun getUnreadMessages(page: Int) = load {
        remoteDataSource.getUnreadMessages(page)
    }

    override suspend fun getReadMessages(page: Int) = load {
        remoteDataSource.getReadMessages(page)
    }

    private suspend fun <T> load(block: suspend () -> T): Result<T> =
        withContext(ioDispatcher) {
            runCatching { block() }
                .onFailure { throwable ->
                    if (throwable is AuthRequiredException) {
                        clearLocalSession()
                    }
                }
        }

    private fun clearLocalSession() {
        authStore.clear()
        cookieJar.clear()
    }
}
