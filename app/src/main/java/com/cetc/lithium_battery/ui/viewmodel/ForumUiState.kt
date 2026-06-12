package com.cetc.lithium_battery.ui.viewmodel

import com.cetc.lithium_battery.data.auth.AuthSession
import com.cetc.lithium_battery.data.model.ForumComment
import com.cetc.lithium_battery.data.model.ForumMessage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.model.CoinInfo

enum class ForumSection(
    val route: String,
    val title: String,
    val initialPage: Int
) {
    SQUARE("square", "广场", 0),
    QUESTIONS("questions", "问答", 1),
    HOT("hot", "热门", 0),
    SEARCH("search", "搜索", 0),
    COLLECTIONS("collections", "我的收藏", 0),
    MY_SHARED("my_shared", "我的分享", 1),
    USER_SHARED("user_shared", "分享人主页", 1);

    val supportsPaging: Boolean
        get() = this != HOT

    fun nextPageAfter(apiCurPage: Int): Int =
        if (initialPage == 0) apiCurPage else apiCurPage + 1

    companion object {
        fun fromRoute(route: String?): ForumSection? =
            entries.firstOrNull { it.route == route }
    }
}

data class PagedUiState<T>(
    val items: List<T> = emptyList(),
    val nextPage: Int = 0,
    val total: Int = 0,
    val canLoadMore: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val appendErrorMessage: String? = null
) {
    val posts: List<T>
        get() = items

    val isEmpty: Boolean
        get() = items.isEmpty() && !isLoading && errorMessage == null
}

typealias FeedUiState = PagedUiState<ForumPost>
typealias MessageFeedUiState = PagedUiState<ForumMessage>

data class LoginUiState(
    val isDialogVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ForumUiState(
    val squareFeed: FeedUiState = FeedUiState(nextPage = ForumSection.SQUARE.initialPage),
    val questionFeed: FeedUiState = FeedUiState(nextPage = ForumSection.QUESTIONS.initialPage),
    val popularState: LoadState<List<ForumPost>> = LoadState.Loading,
    val hotKeysState: LoadState<List<HotKey>> = LoadState.Loading,
    val searchKeyword: String = "",
    val searchFeed: FeedUiState = FeedUiState(nextPage = ForumSection.SEARCH.initialPage),
    val collectionsFeed: FeedUiState = FeedUiState(nextPage = ForumSection.COLLECTIONS.initialPage),
    val mySharedFeed: FeedUiState = FeedUiState(nextPage = ForumSection.MY_SHARED.initialPage),
    val userSharedFeed: FeedUiState = FeedUiState(nextPage = ForumSection.USER_SHARED.initialPage),
    val userSharedCoinInfo: CoinInfo? = null,
    val currentSharedUserId: Int? = null,
    val authSession: AuthSession = AuthSession(),
    val loginState: LoginUiState = LoginUiState(),
    val profileState: LoadState<UserProfile> = LoadState.Content(UserProfile()),
    val unreadMessageCount: Int = 0,
    val unreadMessages: MessageFeedUiState = MessageFeedUiState(nextPage = 1),
    val readMessages: MessageFeedUiState = MessageFeedUiState(nextPage = 1),
    val selectedPost: ForumPost? = null,
    val selectedSection: ForumSection? = null,
    val commentsState: LoadState<List<ForumComment>> = LoadState.Content(emptyList()),
    val isShareDialogVisible: Boolean = false,
    val isSharingArticle: Boolean = false,
    val operationMessage: String? = null
) {
    fun feedFor(section: ForumSection): FeedUiState =
        when (section) {
            ForumSection.SQUARE -> squareFeed
            ForumSection.QUESTIONS -> questionFeed
            ForumSection.HOT -> FeedUiState()
            ForumSection.SEARCH -> searchFeed
            ForumSection.COLLECTIONS -> collectionsFeed
            ForumSection.MY_SHARED -> mySharedFeed
            ForumSection.USER_SHARED -> userSharedFeed
        }
}
