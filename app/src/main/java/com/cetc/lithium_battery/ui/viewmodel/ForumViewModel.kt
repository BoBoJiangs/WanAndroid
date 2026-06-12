package com.cetc.lithium_battery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cetc.lithium_battery.data.model.ForumMessagePage
import com.cetc.lithium_battery.data.model.ForumPage
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.data.repository.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForumViewModel(
    private val repository: ForumRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ForumUiState(authSession = repository.authSession.value)
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authSession.collect { session ->
                val wasLoggedIn = _uiState.value.authSession.isLoggedIn
                _uiState.update { state ->
                    state.copy(
                        authSession = session,
                        loginState = if (session.isLoggedIn) LoginUiState() else state.loginState,
                        profileState = if (session.isLoggedIn) {
                            state.profileState
                        } else {
                            LoadState.Content(UserProfile())
                        },
                        unreadMessageCount = if (session.isLoggedIn) {
                            state.unreadMessageCount
                        } else {
                            0
                        },
                        collectionsFeed = if (session.isLoggedIn) {
                            state.collectionsFeed
                        } else {
                            FeedUiState(nextPage = ForumSection.COLLECTIONS.initialPage)
                        },
                        mySharedFeed = if (session.isLoggedIn) {
                            state.mySharedFeed
                        } else {
                            FeedUiState(nextPage = ForumSection.MY_SHARED.initialPage)
                        },
                        unreadMessages = if (session.isLoggedIn) {
                            state.unreadMessages
                        } else {
                            MessageFeedUiState(nextPage = 1)
                        },
                        readMessages = if (session.isLoggedIn) {
                            state.readMessages
                        } else {
                            MessageFeedUiState(nextPage = 1)
                        }
                    )
                }
                if (session.isLoggedIn && !wasLoggedIn) {
                    refreshProfile()
                    refreshUnreadMessageCount()
                    refreshSection(ForumSection.COLLECTIONS)
                    refreshSection(ForumSection.MY_SHARED)
                }
            }
        }

        refreshAll()
    }

    fun refreshAll() {
        refreshSection(ForumSection.SQUARE)
        refreshSection(ForumSection.QUESTIONS)
        refreshSection(ForumSection.HOT)
        refreshHotKeys()
        if (_uiState.value.authSession.isLoggedIn) {
            refreshProfile()
            refreshUnreadMessageCount()
        }
    }

    fun refreshSection(section: ForumSection) {
        when (section) {
            ForumSection.HOT -> refreshPopularQuestions()
            ForumSection.SEARCH -> submitSearch(_uiState.value.searchKeyword)
            ForumSection.COLLECTIONS,
            ForumSection.MY_SHARED -> {
                if (ensureLoggedIn()) {
                    loadFeed(section = section, refresh = true)
                }
            }

            ForumSection.USER_SHARED -> loadFeed(section = section, refresh = true)
            else -> loadFeed(section = section, refresh = true)
        }
    }

    fun loadMore(section: ForumSection) {
        if (section.supportsPaging) {
            loadFeed(section = section, refresh = false)
        }
    }

    fun showLoginDialog() {
        _uiState.update {
            it.copy(loginState = it.loginState.copy(isDialogVisible = true, errorMessage = null))
        }
    }

    fun dismissLoginDialog() {
        _uiState.update {
            it.copy(loginState = LoginUiState())
        }
    }

    fun login(username: String, password: String) {
        val account = username.trim()
        if (account.length < 3 || password.length < 6) {
            _uiState.update {
                it.copy(
                    loginState = it.loginState.copy(
                        errorMessage = "用户名至少 3 位，密码至少 6 位"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(loginState = it.loginState.copy(isLoading = true, errorMessage = null))
            }
            repository.login(account, password).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            loginState = LoginUiState(),
                            operationMessage = "已登录：${user.displayName}"
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                errorMessage = throwable.toUserMessage()
                            )
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                it.copy(operationMessage = "已退出登录")
            }
        }
    }

    fun refreshProfile() {
        if (!_uiState.value.authSession.isLoggedIn) return

        viewModelScope.launch {
            _uiState.update { it.copy(profileState = LoadState.Loading) }
            repository.getUserProfile().fold(
                onSuccess = { profile ->
                    _uiState.update { state ->
                        state.copy(profileState = LoadState.Content(profile))
                            .withCollectIds(profile.userInfo.collectIds)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(profileState = LoadState.Error(throwable.toUserMessage()))
                    }
                }
            )
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _uiState.update { it.copy(searchKeyword = keyword) }
    }

    fun submitSearch(keyword: String) {
        val trimmed = keyword.trim()
        _uiState.update {
            it.copy(
                searchKeyword = trimmed,
                searchFeed = if (trimmed.isBlank()) {
                    FeedUiState(nextPage = ForumSection.SEARCH.initialPage)
                } else {
                    it.searchFeed
                }
            )
        }
        if (trimmed.isNotBlank()) {
            loadFeed(ForumSection.SEARCH, refresh = true)
        }
    }

    fun refreshHotKeys() {
        viewModelScope.launch {
            _uiState.update { it.copy(hotKeysState = LoadState.Loading) }
            val nextState = repository.getHotKeys().fold(
                onSuccess = { LoadState.Content(it) },
                onFailure = { LoadState.Error(it.toUserMessage()) }
            )
            _uiState.update { it.copy(hotKeysState = nextState) }
        }
    }

    fun openUserShared(userId: Int) {
        if (userId <= 0) {
            _uiState.update { it.copy(operationMessage = "该作者没有公开主页") }
            return
        }
        _uiState.update {
            it.copy(
                currentSharedUserId = userId,
                userSharedCoinInfo = null,
                userSharedFeed = FeedUiState(nextPage = ForumSection.USER_SHARED.initialPage)
            )
        }
        loadFeed(ForumSection.USER_SHARED, refresh = true)
    }

    fun openPost(section: ForumSection, post: ForumPost) {
        val shouldLoadComments = section == ForumSection.QUESTIONS || section == ForumSection.HOT
        _uiState.update {
            it.copy(
                selectedPost = post,
                selectedSection = section,
                commentsState = if (shouldLoadComments) {
                    LoadState.Loading
                } else {
                    LoadState.Content(emptyList())
                }
            )
        }
        if (shouldLoadComments) {
            loadComments(post.id)
        }
    }

    fun ensurePostSelected(section: ForumSection, postId: Int) {
        val state = _uiState.value
        if (state.selectedPost?.id == postId && state.selectedSection == section) {
            return
        }

        val post = state.allLoadedPosts().firstOrNull { it.id == postId }
        if (post == null) {
            _uiState.update {
                it.copy(
                    selectedPost = null,
                    selectedSection = section,
                    commentsState = LoadState.Error("未找到帖子，请返回列表刷新后重试")
                )
            }
        } else {
            openPost(section, post)
        }
    }

    fun refreshSelectedPost() {
        val state = _uiState.value
        val post = state.selectedPost ?: return
        val section = state.selectedSection ?: return
        if (section == ForumSection.QUESTIONS || section == ForumSection.HOT) {
            loadComments(post.id)
        } else {
            _uiState.update { it.copy(commentsState = LoadState.Content(emptyList())) }
        }
    }

    fun toggleCollect(post: ForumPost, fromCollections: Boolean = false) {
        if (!ensureLoggedIn()) return

        val shouldCollect = !post.collect
        val articleId = post.actionArticleId
        viewModelScope.launch {
            val result = when {
                shouldCollect -> repository.collectArticle(articleId)
                fromCollections -> repository.uncollectCollectedArticle(
                    collectId = post.id,
                    originId = post.originId
                )

                else -> repository.uncollectArticle(articleId)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        if (fromCollections && !shouldCollect) {
                            state.removeCollectedArticle(articleId)
                        } else {
                            state.updateCollectState(articleId, shouldCollect)
                        }.copy(
                            operationMessage = if (shouldCollect) "收藏成功" else "已取消收藏"
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(operationMessage = throwable.toUserMessage()) }
                }
            )
        }
    }

    fun showShareDialog() {
        if (ensureLoggedIn()) {
            _uiState.update {
                it.copy(isShareDialogVisible = true, operationMessage = null)
            }
        }
    }

    fun dismissShareDialog() {
        _uiState.update {
            it.copy(isShareDialogVisible = false, isSharingArticle = false)
        }
    }

    fun shareArticle(title: String, link: String) {
        if (!ensureLoggedIn()) return

        val cleanTitle = title.trim()
        val cleanLink = link.trim()
        if (cleanTitle.isBlank() || cleanLink.isBlank()) {
            _uiState.update { it.copy(operationMessage = "标题和链接不能为空") }
            return
        }
        if (!cleanLink.startsWith("http://") && !cleanLink.startsWith("https://")) {
            _uiState.update { it.copy(operationMessage = "链接需要以 http:// 或 https:// 开头") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSharingArticle = true) }
            repository.shareArticle(cleanTitle, cleanLink).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isShareDialogVisible = false,
                            isSharingArticle = false,
                            operationMessage = "分享成功"
                        )
                    }
                    refreshSection(ForumSection.SQUARE)
                    refreshSection(ForumSection.MY_SHARED)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSharingArticle = false,
                            operationMessage = throwable.toUserMessage()
                        )
                    }
                }
            )
        }
    }

    fun deleteSharedArticle(post: ForumPost) {
        if (!ensureLoggedIn()) return

        viewModelScope.launch {
            repository.deleteSharedArticle(post.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.removeMySharedArticle(post.id)
                            .copy(operationMessage = "已删除分享")
                    }
                    refreshSection(ForumSection.SQUARE)
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(operationMessage = throwable.toUserMessage()) }
                }
            )
        }
    }

    fun refreshUnreadMessageCount() {
        if (!_uiState.value.authSession.isLoggedIn) return

        viewModelScope.launch {
            repository.getUnreadMessageCount().onSuccess { count ->
                _uiState.update { it.copy(unreadMessageCount = count) }
            }
        }
    }

    fun refreshMessages(read: Boolean) {
        if (ensureLoggedIn()) {
            loadMessages(read = read, refresh = true)
        }
    }

    fun loadMoreMessages(read: Boolean) {
        if (ensureLoggedIn()) {
            loadMessages(read = read, refresh = false)
        }
    }

    fun clearOperationMessage() {
        _uiState.update { it.copy(operationMessage = null) }
    }

    private fun ensureLoggedIn(): Boolean {
        if (_uiState.value.authSession.isLoggedIn) {
            return true
        }
        _uiState.update {
            it.copy(
                loginState = it.loginState.copy(isDialogVisible = true),
                operationMessage = "请先登录"
            )
        }
        return false
    }

    private fun loadFeed(section: ForumSection, refresh: Boolean) {
        val keyword = _uiState.value.searchKeyword
        if (section == ForumSection.SEARCH && keyword.isBlank()) return
        if ((section == ForumSection.COLLECTIONS || section == ForumSection.MY_SHARED) &&
            !_uiState.value.authSession.isLoggedIn
        ) {
            ensureLoggedIn()
            return
        }

        val current = _uiState.value.feedFor(section)
        if (refresh && current.isLoading) return
        if (!refresh && (!current.canLoadMore || current.isLoadingMore || current.isLoading)) return

        val targetPage = if (refresh) section.initialPage else current.nextPage
        updateFeed(section) {
            it.copy(
                isLoading = refresh,
                isLoadingMore = !refresh,
                errorMessage = null,
                appendErrorMessage = null
            )
        }

        viewModelScope.launch {
            val result = when (section) {
                ForumSection.SQUARE -> repository.getSquarePosts(targetPage)
                ForumSection.QUESTIONS -> repository.getQuestionPosts(targetPage)
                ForumSection.SEARCH -> repository.searchPosts(keyword, targetPage)
                ForumSection.COLLECTIONS -> repository.getCollectedPosts(targetPage)
                ForumSection.MY_SHARED -> repository.getMySharedPosts(targetPage)
                    .map { it.shareArticles }

                ForumSection.USER_SHARED -> {
                    val userId = _uiState.value.currentSharedUserId
                    if (userId == null) {
                        Result.failure(IllegalStateException("未找到分享人"))
                    } else {
                        repository.getUserSharedPosts(userId, targetPage)
                            .onSuccess { sharePage ->
                                _uiState.update {
                                    it.copy(userSharedCoinInfo = sharePage.coinInfo)
                                }
                            }
                            .map { it.shareArticles }
                    }
                }

                ForumSection.HOT -> error("Hot section is not paged")
            }

            result.fold(
                onSuccess = { page -> applyPage(section, page, refresh) },
                onFailure = { throwable ->
                    updateFeed(section) { feed ->
                        feed.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = if (feed.items.isEmpty()) {
                                throwable.toUserMessage()
                            } else {
                                null
                            },
                            appendErrorMessage = if (feed.items.isNotEmpty()) {
                                throwable.toUserMessage()
                            } else {
                                null
                            }
                        )
                    }
                }
            )
        }
    }

    private fun applyPage(section: ForumSection, page: ForumPage, refresh: Boolean) {
        updateFeed(section) { feed ->
            val base = if (refresh) emptyList() else feed.items
            val incoming = if (section == ForumSection.COLLECTIONS) {
                page.datas.map { it.copy(collect = true) }
            } else {
                page.datas
            }
            val mergedPosts = (base + incoming).distinctBy { it.id }
            feed.copy(
                items = mergedPosts,
                nextPage = section.nextPageAfter(page.curPage),
                total = page.total,
                canLoadMore = !page.over,
                isLoading = false,
                isLoadingMore = false,
                errorMessage = null,
                appendErrorMessage = null
            )
        }
    }

    private fun refreshPopularQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(popularState = LoadState.Loading) }
            val nextState = repository.getPopularQuestions().fold(
                onSuccess = { LoadState.Content(it) },
                onFailure = { LoadState.Error(it.toUserMessage()) }
            )
            _uiState.update { it.copy(popularState = nextState) }
        }
    }

    private fun loadComments(articleId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(commentsState = LoadState.Loading) }
            val nextState = repository.getQuestionComments(articleId).fold(
                onSuccess = { LoadState.Content(it.datas) },
                onFailure = { LoadState.Error(it.toUserMessage()) }
            )
            _uiState.update { it.copy(commentsState = nextState) }
        }
    }

    private fun loadMessages(read: Boolean, refresh: Boolean) {
        val current = if (read) _uiState.value.readMessages else _uiState.value.unreadMessages
        if (refresh && current.isLoading) return
        if (!refresh && (!current.canLoadMore || current.isLoadingMore || current.isLoading)) return

        val targetPage = if (refresh) 1 else current.nextPage
        updateMessageFeed(read) {
            it.copy(
                isLoading = refresh,
                isLoadingMore = !refresh,
                errorMessage = null,
                appendErrorMessage = null
            )
        }

        viewModelScope.launch {
            val result = if (read) {
                repository.getReadMessages(targetPage)
            } else {
                repository.getUnreadMessages(targetPage)
            }
            result.fold(
                onSuccess = { page ->
                    applyMessagePage(read, page, refresh)
                    if (!read) refreshUnreadMessageCount()
                },
                onFailure = { throwable ->
                    updateMessageFeed(read) { feed ->
                        feed.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = if (feed.items.isEmpty()) {
                                throwable.toUserMessage()
                            } else {
                                null
                            },
                            appendErrorMessage = if (feed.items.isNotEmpty()) {
                                throwable.toUserMessage()
                            } else {
                                null
                            }
                        )
                    }
                }
            )
        }
    }

    private fun applyMessagePage(read: Boolean, page: ForumMessagePage, refresh: Boolean) {
        updateMessageFeed(read) { feed ->
            val base = if (refresh) emptyList() else feed.items
            feed.copy(
                items = (base + page.datas).distinctBy { it.id },
                nextPage = page.curPage + 1,
                total = page.total,
                canLoadMore = !page.over,
                isLoading = false,
                isLoadingMore = false,
                errorMessage = null,
                appendErrorMessage = null
            )
        }
    }

    private fun updateFeed(
        section: ForumSection,
        transform: (FeedUiState) -> FeedUiState
    ) {
        _uiState.update { state ->
            when (section) {
                ForumSection.SQUARE -> state.copy(squareFeed = transform(state.squareFeed))
                ForumSection.QUESTIONS -> state.copy(questionFeed = transform(state.questionFeed))
                ForumSection.SEARCH -> state.copy(searchFeed = transform(state.searchFeed))
                ForumSection.COLLECTIONS -> state.copy(collectionsFeed = transform(state.collectionsFeed))
                ForumSection.MY_SHARED -> state.copy(mySharedFeed = transform(state.mySharedFeed))
                ForumSection.USER_SHARED -> state.copy(userSharedFeed = transform(state.userSharedFeed))
                ForumSection.HOT -> state
            }
        }
    }

    private fun updateMessageFeed(
        read: Boolean,
        transform: (MessageFeedUiState) -> MessageFeedUiState
    ) {
        _uiState.update { state ->
            if (read) {
                state.copy(readMessages = transform(state.readMessages))
            } else {
                state.copy(unreadMessages = transform(state.unreadMessages))
            }
        }
    }

    private fun ForumUiState.allLoadedPosts(): List<ForumPost> {
        val popularPosts = (popularState as? LoadState.Content)?.data.orEmpty()
        return squareFeed.items +
            questionFeed.items +
            searchFeed.items +
            collectionsFeed.items +
            mySharedFeed.items +
            userSharedFeed.items +
            popularPosts
    }

    private fun ForumUiState.updateCollectState(articleId: Int, collected: Boolean): ForumUiState {
        fun FeedUiState.updatePosts() = copy(
            items = items.map { post ->
                if (post.actionArticleId == articleId || post.id == articleId) {
                    post.copy(collect = collected)
                } else {
                    post
                }
            }
        )

        val nextPopularState = when (popularState) {
            is LoadState.Content -> LoadState.Content(
                popularState.data.map { post ->
                    if (post.actionArticleId == articleId || post.id == articleId) {
                        post.copy(collect = collected)
                    } else {
                        post
                    }
                }
            )

            else -> popularState
        }

        return copy(
            squareFeed = squareFeed.updatePosts(),
            questionFeed = questionFeed.updatePosts(),
            searchFeed = searchFeed.updatePosts(),
            collectionsFeed = collectionsFeed.updatePosts(),
            mySharedFeed = mySharedFeed.updatePosts(),
            userSharedFeed = userSharedFeed.updatePosts(),
            popularState = nextPopularState,
            selectedPost = selectedPost?.let { post ->
                if (post.actionArticleId == articleId || post.id == articleId) {
                    post.copy(collect = collected)
                } else {
                    post
                }
            }
        )
    }

    private fun ForumUiState.withCollectIds(collectIds: List<Int>): ForumUiState {
        if (collectIds.isEmpty()) return this
        fun FeedUiState.updatePosts() = copy(
            items = items.map { post ->
                post.copy(collect = post.collect || post.actionArticleId in collectIds || post.id in collectIds)
            }
        )
        return copy(
            squareFeed = squareFeed.updatePosts(),
            questionFeed = questionFeed.updatePosts(),
            searchFeed = searchFeed.updatePosts(),
            mySharedFeed = mySharedFeed.updatePosts(),
            userSharedFeed = userSharedFeed.updatePosts()
        )
    }

    private fun ForumUiState.removeCollectedArticle(articleId: Int): ForumUiState =
        updateCollectState(articleId, collected = false).copy(
            collectionsFeed = collectionsFeed.copy(
                items = collectionsFeed.items.filterNot {
                    it.actionArticleId == articleId || it.id == articleId
                },
                total = (collectionsFeed.total - 1).coerceAtLeast(0)
            )
        )

    private fun ForumUiState.removeMySharedArticle(articleId: Int): ForumUiState =
        copy(
            mySharedFeed = mySharedFeed.copy(
                items = mySharedFeed.items.filterNot { it.id == articleId },
                total = (mySharedFeed.total - 1).coerceAtLeast(0)
            )
        )

    companion object {
        fun factory(repository: ForumRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ForumViewModel::class.java)) {
                        return ForumViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
