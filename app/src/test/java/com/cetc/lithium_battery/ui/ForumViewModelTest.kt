package com.cetc.lithium_battery.ui

import com.cetc.lithium_battery.data.auth.AuthSession
import com.cetc.lithium_battery.data.model.CommentPage
import com.cetc.lithium_battery.data.model.CoinInfo
import com.cetc.lithium_battery.data.model.ForumComment
import com.cetc.lithium_battery.data.model.ForumMessage
import com.cetc.lithium_battery.data.model.ForumMessagePage
import com.cetc.lithium_battery.data.model.ForumPage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.data.model.UserInfo
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.model.UserSharePage
import com.cetc.lithium_battery.data.repository.ForumRepository
import com.cetc.lithium_battery.ui.viewmodel.ForumSection
import com.cetc.lithium_battery.ui.viewmodel.ForumViewModel
import com.cetc.lithium_battery.ui.viewmodel.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForumViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadsInitialForumState() {
        val viewModel = ForumViewModel(FakeForumRepository())
        val state = viewModel.uiState.value

        assertEquals("广场 1", state.squareFeed.posts.first().title)
        assertEquals("问答 1", state.questionFeed.posts.first().title)
        assertTrue(state.popularState is LoadState.Content)
    }

    @Test
    fun loadMoreSquareAppendsNextPage() {
        val viewModel = ForumViewModel(FakeForumRepository())

        viewModel.loadMore(ForumSection.SQUARE)

        val titles = viewModel.uiState.value.squareFeed.posts.map { it.title }
        assertEquals(listOf("广场 1", "广场 2"), titles)
    }

    @Test
    fun openQuestionLoadsComments() {
        val viewModel = ForumViewModel(FakeForumRepository())
        val post = viewModel.uiState.value.questionFeed.posts.first()

        viewModel.openPost(ForumSection.QUESTIONS, post)

        val comments = viewModel.uiState.value.commentsState as LoadState.Content
        assertEquals("第一条评论", comments.data.first().content)
    }

    @Test
    fun collectWhenLoggedOutShowsLoginDialog() {
        val viewModel = ForumViewModel(FakeForumRepository())

        viewModel.toggleCollect(viewModel.uiState.value.squareFeed.posts.first())

        assertTrue(viewModel.uiState.value.loginState.isDialogVisible)
    }

    @Test
    fun loginThenCollectUpdatesLoadedPosts() {
        val viewModel = ForumViewModel(FakeForumRepository())

        viewModel.login("tester", "password")
        viewModel.toggleCollect(viewModel.uiState.value.squareFeed.posts.first())

        assertTrue(viewModel.uiState.value.squareFeed.posts.first().collect)
    }

    @Test
    fun searchSupportsPagination() {
        val viewModel = ForumViewModel(FakeForumRepository())

        viewModel.submitSearch("compose")
        viewModel.loadMore(ForumSection.SEARCH)

        val titles = viewModel.uiState.value.searchFeed.posts.map { it.title }
        assertEquals(listOf("compose 0", "compose 1"), titles)
    }

    @Test
    fun shareArticleRefreshesMySharedPosts() {
        val viewModel = ForumViewModel(FakeForumRepository())

        viewModel.login("tester", "password")
        viewModel.shareArticle("新分享", "https://example.com")

        assertTrue(viewModel.uiState.value.mySharedFeed.posts.any { it.title == "新分享" })
    }

    @Test
    fun loginLoadsUnreadMessageCount() {
        val viewModel = ForumViewModel(FakeForumRepository())

        viewModel.login("tester", "password")

        assertEquals(2, viewModel.uiState.value.unreadMessageCount)
    }
}

private class FakeForumRepository : ForumRepository {
    private val sessionFlow = MutableStateFlow(AuthSession())
    private val collectedIds = mutableSetOf<Int>()
    private val myShares = mutableListOf(
        ForumPost(id = 300, title = "我的分享 1", link = "https://example.com/old", userId = 7)
    )

    override val authSession: StateFlow<AuthSession> = sessionFlow

    override suspend fun getSquarePosts(page: Int): Result<ForumPage> =
        Result.success(
            if (page == 0) {
                ForumPage(
                    curPage = 1,
                    datas = listOf(
                        ForumPost(
                            id = 1,
                            title = "广场 1",
                            link = "https://example.com/1",
                            userId = 7,
                            collect = 1 in collectedIds
                        )
                    ),
                    over = false,
                    total = 2
                )
            } else {
                ForumPage(
                    curPage = 2,
                    datas = listOf(ForumPost(id = 2, title = "广场 2", link = "https://example.com/2")),
                    over = true,
                    total = 2
                )
            }
        )

    override suspend fun getQuestionPosts(page: Int): Result<ForumPage> =
        Result.success(
            ForumPage(
                curPage = 1,
                datas = listOf(ForumPost(id = 10, title = "问答 1", link = "https://example.com/q")),
                over = true,
                total = 1
            )
        )

    override suspend fun getPopularQuestions(): Result<List<ForumPost>> =
        Result.success(listOf(ForumPost(id = 20, title = "热门 1", link = "https://example.com/hot")))

    override suspend fun getQuestionComments(articleId: Int): Result<CommentPage> =
        Result.success(
            CommentPage(
                datas = listOf(
                    ForumComment(id = 100, articleId = articleId, content = "第一条评论")
                ),
                total = 1
            )
        )

    override suspend fun login(username: String, password: String): Result<UserInfo> {
        val user = UserInfo(id = 7, username = username, nickname = "测试用户")
        sessionFlow.value = AuthSession(user)
        return Result.success(user)
    }

    override suspend fun logout(): Result<Unit> {
        sessionFlow.value = AuthSession()
        return Result.success(Unit)
    }

    override suspend fun getUserProfile(): Result<UserProfile> =
        Result.success(
            UserProfile(
                coinInfo = CoinInfo(coinCount = 100, level = 1, rank = "10", userId = 7),
                userInfo = sessionFlow.value.user ?: UserInfo(id = 7, username = "tester")
            )
        )

    override suspend fun getHotKeys(): Result<List<HotKey>> =
        Result.success(listOf(HotKey(id = 1, name = "compose")))

    override suspend fun searchPosts(keyword: String, page: Int): Result<ForumPage> =
        Result.success(
            ForumPage(
                curPage = page + 1,
                datas = listOf(ForumPost(id = 1000 + page, title = "$keyword $page")),
                over = page >= 1,
                total = 2
            )
        )

    override suspend fun getCollectedPosts(page: Int): Result<ForumPage> =
        Result.success(
            ForumPage(
                curPage = 1,
                datas = collectedIds.map { id ->
                    ForumPost(id = 900 + id, originId = id, title = "收藏 $id", collect = true)
                },
                over = true,
                total = collectedIds.size
            )
        )

    override suspend fun collectArticle(articleId: Int): Result<Unit> {
        collectedIds += articleId
        return Result.success(Unit)
    }

    override suspend fun uncollectArticle(articleId: Int): Result<Unit> {
        collectedIds -= articleId
        return Result.success(Unit)
    }

    override suspend fun uncollectCollectedArticle(collectId: Int, originId: Int): Result<Unit> {
        collectedIds -= originId
        return Result.success(Unit)
    }

    override suspend fun getUserSharedPosts(userId: Int, page: Int): Result<UserSharePage> =
        Result.success(
            UserSharePage(
                coinInfo = CoinInfo(userId = userId, username = "author"),
                shareArticles = ForumPage(
                    curPage = 1,
                    datas = listOf(ForumPost(id = 80, title = "作者分享", userId = userId)),
                    over = true,
                    total = 1
                )
            )
        )

    override suspend fun getMySharedPosts(page: Int): Result<UserSharePage> =
        Result.success(
            UserSharePage(
                shareArticles = ForumPage(
                    curPage = 1,
                    datas = myShares.toList(),
                    over = true,
                    total = myShares.size
                )
            )
        )

    override suspend fun shareArticle(title: String, link: String): Result<Unit> {
        myShares += ForumPost(id = 301, title = title, link = link, userId = 7)
        return Result.success(Unit)
    }

    override suspend fun deleteSharedArticle(articleId: Int): Result<Unit> {
        myShares.removeAll { it.id == articleId }
        return Result.success(Unit)
    }

    override suspend fun getUnreadMessageCount(): Result<Int> =
        Result.success(2)

    override suspend fun getUnreadMessages(page: Int): Result<ForumMessagePage> =
        Result.success(
            ForumMessagePage(
                curPage = 1,
                datas = listOf(ForumMessage(id = 1, title = "未读消息")),
                over = true,
                total = 1
            )
        )

    override suspend fun getReadMessages(page: Int): Result<ForumMessagePage> =
        Result.success(
            ForumMessagePage(
                curPage = 1,
                datas = listOf(ForumMessage(id = 2, title = "已读消息")),
                over = true,
                total = 1
            )
        )
}
