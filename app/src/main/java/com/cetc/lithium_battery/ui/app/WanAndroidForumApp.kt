package com.cetc.lithium_battery.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.repository.ForumRepository
import com.cetc.lithium_battery.ui.components.LoginDialog
import com.cetc.lithium_battery.ui.components.ShareArticleDialog
import com.cetc.lithium_battery.ui.screens.forum.ForumFeedScreen
import com.cetc.lithium_battery.ui.screens.forum.MessageScreen
import com.cetc.lithium_battery.ui.screens.forum.MineScreen
import com.cetc.lithium_battery.ui.screens.forum.PopularQuestionsScreen
import com.cetc.lithium_battery.ui.screens.forum.PostDetailScreen
import com.cetc.lithium_battery.ui.screens.forum.SearchScreen
import com.cetc.lithium_battery.ui.viewmodel.ForumSection
import com.cetc.lithium_battery.ui.viewmodel.ForumViewModel

private data class TopLevelDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val section: ForumSection? = null
)

private object Routes {
    const val MINE = "mine"
    const val SEARCH = "search"
    const val COLLECTIONS = "collections"
    const val MY_SHARED = "my_shared"
    const val MESSAGES = "messages"
    const val USER_SHARED = "user/{userId}"
    const val POST_DETAIL = "post/{section}/{postId}"
    const val SECTION_ARGUMENT = "section"
    const val POST_ID_ARGUMENT = "postId"
    const val USER_ID_ARGUMENT = "userId"

    fun postDetail(section: ForumSection, postId: Int) = "post/${section.route}/$postId"
    fun userShared(userId: Int) = "user/$userId"
}

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = ForumSection.SQUARE.route,
        title = ForumSection.SQUARE.title,
        icon = Icons.Outlined.Forum,
        section = ForumSection.SQUARE
    ),
    TopLevelDestination(
        route = ForumSection.QUESTIONS.route,
        title = ForumSection.QUESTIONS.title,
        icon = Icons.Outlined.QuestionAnswer,
        section = ForumSection.QUESTIONS
    ),
    TopLevelDestination(
        route = ForumSection.HOT.route,
        title = ForumSection.HOT.title,
        icon = Icons.Outlined.Whatshot,
        section = ForumSection.HOT
    ),
    TopLevelDestination(
        route = Routes.MINE,
        title = "我的",
        icon = Icons.Outlined.AccountCircle
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WanAndroidForumApp(
    repository: ForumRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: ForumViewModel = viewModel(
        factory = ForumViewModel.factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentSection = ForumSection.fromRoute(currentRoute)
    val isTopLevelRoute = topLevelDestinations.any { it.route == currentRoute }
    val title = currentTitle(currentRoute, currentSection)

    LaunchedEffect(uiState.operationMessage) {
        val message = uiState.operationMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearOperationMessage()
        }
    }

    fun navigateToPost(section: ForumSection, post: ForumPost) {
        viewModel.openPost(section, post)
        navController.navigate(Routes.postDetail(section, post.id))
    }

    fun navigateToAuthor(post: ForumPost) {
        if (post.userId > 0) {
            navController.navigate(Routes.userShared(post.userId))
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    if (!isTopLevelRoute) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute != Routes.SEARCH) {
                        IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "搜索"
                            )
                        }
                    }
                    if (currentRoute == ForumSection.SQUARE.route || currentRoute == Routes.MY_SHARED) {
                        IconButton(onClick = viewModel::showShareDialog) {
                            Icon(
                                imageVector = Icons.Outlined.AddLink,
                                contentDescription = "分享文章"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            when (currentRoute) {
                                Routes.POST_DETAIL -> viewModel.refreshSelectedPost()
                                Routes.SEARCH -> {
                                    if (uiState.searchKeyword.isBlank()) {
                                        viewModel.refreshHotKeys()
                                    } else {
                                        viewModel.refreshSection(ForumSection.SEARCH)
                                    }
                                }

                                Routes.COLLECTIONS -> viewModel.refreshSection(ForumSection.COLLECTIONS)
                                Routes.MY_SHARED -> viewModel.refreshSection(ForumSection.MY_SHARED)
                                Routes.MESSAGES -> {
                                    viewModel.refreshUnreadMessageCount()
                                    viewModel.refreshMessages(read = false)
                                }

                                Routes.USER_SHARED -> viewModel.refreshSection(ForumSection.USER_SHARED)
                                Routes.MINE -> {
                                    viewModel.refreshProfile()
                                    viewModel.refreshUnreadMessageCount()
                                }

                                else -> viewModel.refreshSection(currentSection ?: ForumSection.SQUARE)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(ForumSection.SQUARE.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(text = destination.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ForumSection.SQUARE.route,
            modifier = modifier
        ) {
            composable(ForumSection.SQUARE.route) {
                ForumFeedScreen(
                    section = ForumSection.SQUARE,
                    feedState = uiState.squareFeed,
                    onRefresh = { viewModel.refreshSection(ForumSection.SQUARE) },
                    onLoadMore = { viewModel.loadMore(ForumSection.SQUARE) },
                    onPostClick = { navigateToPost(ForumSection.SQUARE, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(ForumSection.QUESTIONS.route) {
                ForumFeedScreen(
                    section = ForumSection.QUESTIONS,
                    feedState = uiState.questionFeed,
                    onRefresh = { viewModel.refreshSection(ForumSection.QUESTIONS) },
                    onLoadMore = { viewModel.loadMore(ForumSection.QUESTIONS) },
                    onPostClick = { navigateToPost(ForumSection.QUESTIONS, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(ForumSection.HOT.route) {
                PopularQuestionsScreen(
                    postsState = uiState.popularState,
                    onRefresh = { viewModel.refreshSection(ForumSection.HOT) },
                    onPostClick = { navigateToPost(ForumSection.HOT, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.MINE) {
                MineScreen(
                    authSession = uiState.authSession,
                    profileState = uiState.profileState,
                    unreadMessageCount = uiState.unreadMessageCount,
                    onLoginClick = viewModel::showLoginDialog,
                    onLogoutClick = viewModel::logout,
                    onRefreshProfile = viewModel::refreshProfile,
                    onOpenCollections = { navController.navigate(Routes.COLLECTIONS) },
                    onOpenMyShared = { navController.navigate(Routes.MY_SHARED) },
                    onOpenMessages = { navController.navigate(Routes.MESSAGES) },
                    onShareArticle = viewModel::showShareDialog,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    keyword = uiState.searchKeyword,
                    hotKeysState = uiState.hotKeysState,
                    feedState = uiState.searchFeed,
                    onKeywordChange = viewModel::updateSearchKeyword,
                    onSearch = viewModel::submitSearch,
                    onRefreshHotKeys = viewModel::refreshHotKeys,
                    onRefreshResults = { viewModel.refreshSection(ForumSection.SEARCH) },
                    onLoadMore = { viewModel.loadMore(ForumSection.SEARCH) },
                    onPostClick = { navigateToPost(ForumSection.SEARCH, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.COLLECTIONS) {
                LaunchedEffect(Unit) {
                    viewModel.refreshSection(ForumSection.COLLECTIONS)
                }
                ForumFeedScreen(
                    section = ForumSection.COLLECTIONS,
                    feedState = uiState.collectionsFeed,
                    onRefresh = { viewModel.refreshSection(ForumSection.COLLECTIONS) },
                    onLoadMore = { viewModel.loadMore(ForumSection.COLLECTIONS) },
                    onPostClick = { navigateToPost(ForumSection.COLLECTIONS, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it, fromCollections = true) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.MY_SHARED) {
                LaunchedEffect(Unit) {
                    viewModel.refreshSection(ForumSection.MY_SHARED)
                }
                ForumFeedScreen(
                    section = ForumSection.MY_SHARED,
                    feedState = uiState.mySharedFeed,
                    onRefresh = { viewModel.refreshSection(ForumSection.MY_SHARED) },
                    onLoadMore = { viewModel.loadMore(ForumSection.MY_SHARED) },
                    onPostClick = { navigateToPost(ForumSection.MY_SHARED, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it) },
                    onDeleteClick = viewModel::deleteSharedArticle,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Routes.MESSAGES) {
                LaunchedEffect(Unit) {
                    viewModel.refreshUnreadMessageCount()
                    viewModel.refreshMessages(read = false)
                }
                MessageScreen(
                    unreadState = uiState.unreadMessages,
                    readState = uiState.readMessages,
                    onRefreshUnread = { viewModel.refreshMessages(read = false) },
                    onRefreshRead = { viewModel.refreshMessages(read = true) },
                    onLoadMoreUnread = { viewModel.loadMoreMessages(read = false) },
                    onLoadMoreRead = { viewModel.loadMoreMessages(read = true) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(
                route = Routes.USER_SHARED,
                arguments = listOf(
                    navArgument(Routes.USER_ID_ARGUMENT) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt(Routes.USER_ID_ARGUMENT) ?: 0
                LaunchedEffect(userId) {
                    viewModel.openUserShared(userId)
                }
                ForumFeedScreen(
                    section = ForumSection.USER_SHARED,
                    feedState = uiState.userSharedFeed,
                    onRefresh = { viewModel.refreshSection(ForumSection.USER_SHARED) },
                    onLoadMore = { viewModel.loadMore(ForumSection.USER_SHARED) },
                    onPostClick = { navigateToPost(ForumSection.USER_SHARED, it) },
                    onAuthorClick = ::navigateToAuthor,
                    onCollectClick = { viewModel.toggleCollect(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(
                route = Routes.POST_DETAIL,
                arguments = listOf(
                    navArgument(Routes.SECTION_ARGUMENT) {
                        type = NavType.StringType
                    },
                    navArgument(Routes.POST_ID_ARGUMENT) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val sectionRoute = backStackEntry.arguments
                    ?.getString(Routes.SECTION_ARGUMENT)
                val section = ForumSection.fromRoute(sectionRoute)
                val postId = backStackEntry.arguments
                    ?.getInt(Routes.POST_ID_ARGUMENT)
                    ?: 0

                LaunchedEffect(section, postId) {
                    if (section != null && postId > 0) {
                        viewModel.ensurePostSelected(section, postId)
                    }
                }

                PostDetailScreen(
                    post = uiState.selectedPost,
                    section = uiState.selectedSection ?: section,
                    commentsState = uiState.commentsState,
                    onRetry = viewModel::refreshSelectedPost,
                    onCollectClick = { viewModel.toggleCollect(it, fromCollections = section == ForumSection.COLLECTIONS) },
                    onAuthorClick = ::navigateToAuthor,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        LoginDialog(
            loginState = uiState.loginState,
            onDismiss = viewModel::dismissLoginDialog,
            onLogin = viewModel::login
        )
        ShareArticleDialog(
            isVisible = uiState.isShareDialogVisible,
            isSubmitting = uiState.isSharingArticle,
            onDismiss = viewModel::dismissShareDialog,
            onSubmit = viewModel::shareArticle
        )
    }
}

private fun currentTitle(
    currentRoute: String?,
    currentSection: ForumSection?
): String =
    when (currentRoute) {
        Routes.MINE -> "我的"
        Routes.SEARCH -> "搜索"
        Routes.COLLECTIONS -> "我的收藏"
        Routes.MY_SHARED -> "我的分享"
        Routes.MESSAGES -> "站内消息"
        Routes.USER_SHARED -> "分享人主页"
        Routes.POST_DETAIL -> "帖子详情"
        else -> currentSection?.title ?: "WanAndroid 论坛"
    }
