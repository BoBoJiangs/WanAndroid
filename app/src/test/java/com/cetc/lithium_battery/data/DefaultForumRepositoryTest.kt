package com.cetc.lithium_battery.data

import com.cetc.lithium_battery.data.api.AuthRequiredException
import com.cetc.lithium_battery.data.api.ForumDataSource
import com.cetc.lithium_battery.data.auth.AuthSession
import com.cetc.lithium_battery.data.auth.CookieStore
import com.cetc.lithium_battery.data.auth.SessionStore
import com.cetc.lithium_battery.data.model.CommentPage
import com.cetc.lithium_battery.data.model.ForumMessagePage
import com.cetc.lithium_battery.data.model.ForumPage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.data.model.UserInfo
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.model.UserSharePage
import com.cetc.lithium_battery.data.repository.DefaultForumRepository
import com.cetc.lithium_battery.core.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultForumRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun apiBaseUrlUsesOfficialHost() {
        assertEquals("https://wanandroid.com/", AppContainer.DEFAULT_API_BASE_URL)
    }

    @Test
    fun returnsRemoteSquarePosts() = runTest(testDispatcher) {
        val repository = DefaultForumRepository(
            remoteDataSource = SuccessfulForumDataSource,
            authStore = FakeSessionStore(),
            cookieJar = FakeCookieStore(),
            ioDispatcher = testDispatcher
        )

        val page = repository.getSquarePosts(0).getOrThrow()

        assertEquals("广场帖子", page.datas.first().title)
    }

    @Test
    fun remoteFailureReturnsFailure() = runTest(testDispatcher) {
        val repository = DefaultForumRepository(
            remoteDataSource = FailingForumDataSource,
            authStore = FakeSessionStore(),
            cookieJar = FakeCookieStore(),
            ioDispatcher = testDispatcher
        )

        val result = repository.getQuestionPosts(1)

        assertTrue(result.isFailure)
    }

    @Test
    fun loginStoresSession() = runTest(testDispatcher) {
        val sessionStore = FakeSessionStore()
        val repository = DefaultForumRepository(
            remoteDataSource = SuccessfulForumDataSource,
            authStore = sessionStore,
            cookieJar = FakeCookieStore(),
            ioDispatcher = testDispatcher
        )

        repository.login("tester", "password").getOrThrow()

        assertTrue(repository.authSession.value.isLoggedIn)
        assertEquals("tester", repository.authSession.value.user?.username)
    }

    @Test
    fun authRequiredClearsSessionAndCookies() = runTest(testDispatcher) {
        val sessionStore = FakeSessionStore(UserInfo(id = 1, username = "tester"))
        val cookieStore = FakeCookieStore()
        val repository = DefaultForumRepository(
            remoteDataSource = AuthRequiredForumDataSource,
            authStore = sessionStore,
            cookieJar = cookieStore,
            ioDispatcher = testDispatcher
        )

        val result = repository.getCollectedPosts(0)

        assertTrue(result.isFailure)
        assertFalse(repository.authSession.value.isLoggedIn)
        assertTrue(cookieStore.cleared)
    }
}

private class FakeSessionStore(user: UserInfo? = null) : SessionStore {
    private val sessionFlow = MutableStateFlow(AuthSession(user))
    override val session: StateFlow<AuthSession> = sessionFlow

    override fun saveUser(user: UserInfo) {
        sessionFlow.value = AuthSession(user)
    }

    override fun clear() {
        sessionFlow.value = AuthSession()
    }
}

private class FakeCookieStore : CookieStore {
    var cleared = false

    override fun clear() {
        cleared = true
    }
}

private object SuccessfulForumDataSource : ForumDataSource {
    override suspend fun getSquarePosts(page: Int): ForumPage =
        ForumPage(
            curPage = 1,
            datas = listOf(ForumPost(id = 1, title = "广场帖子")),
            over = true,
            total = 1
        )

    override suspend fun getQuestionPosts(page: Int): ForumPage =
        ForumPage(
            curPage = 1,
            datas = listOf(ForumPost(id = 2, title = "问答帖子")),
            over = true,
            total = 1
        )

    override suspend fun getPopularQuestions(): List<ForumPost> =
        listOf(ForumPost(id = 3, title = "热门问答"))

    override suspend fun getQuestionComments(articleId: Int): CommentPage =
        CommentPage(total = 0)

    override suspend fun login(username: String, password: String): UserInfo =
        UserInfo(id = 1, username = username)

    override suspend fun logout() = Unit
    override suspend fun getUserProfile(): UserProfile = UserProfile()
    override suspend fun getHotKeys(): List<HotKey> = emptyList()
    override suspend fun searchPosts(keyword: String, page: Int): ForumPage = ForumPage()
    override suspend fun getCollectedPosts(page: Int): ForumPage = ForumPage()
    override suspend fun collectArticle(articleId: Int) = Unit
    override suspend fun uncollectArticle(articleId: Int) = Unit
    override suspend fun uncollectCollectedArticle(collectId: Int, originId: Int) = Unit
    override suspend fun getUserSharedPosts(userId: Int, page: Int): UserSharePage = UserSharePage()
    override suspend fun getMySharedPosts(page: Int): UserSharePage = UserSharePage()
    override suspend fun shareArticle(title: String, link: String) = Unit
    override suspend fun deleteSharedArticle(articleId: Int) = Unit
    override suspend fun getUnreadMessageCount(): Int = 0
    override suspend fun getUnreadMessages(page: Int): ForumMessagePage = ForumMessagePage()
    override suspend fun getReadMessages(page: Int): ForumMessagePage = ForumMessagePage()
}

private object FailingForumDataSource : ForumDataSource by SuccessfulForumDataSource {
    override suspend fun getSquarePosts(page: Int): ForumPage = error("Remote unavailable")
    override suspend fun getQuestionPosts(page: Int): ForumPage = error("Remote unavailable")
    override suspend fun getPopularQuestions(): List<ForumPost> = error("Remote unavailable")
    override suspend fun getQuestionComments(articleId: Int): CommentPage = error("Remote unavailable")
}

private object AuthRequiredForumDataSource : ForumDataSource by SuccessfulForumDataSource {
    override suspend fun getCollectedPosts(page: Int): ForumPage =
        throw AuthRequiredException("请先登录")
}
