package com.cetc.lithium_battery.ui.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.ui.components.ForumPostCard
import com.cetc.lithium_battery.ui.components.LabelBadge
import com.cetc.lithium_battery.ui.viewmodel.FeedUiState
import com.cetc.lithium_battery.ui.viewmodel.ForumSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumFeedScreen(
    section: ForumSection,
    feedState: FeedUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (ForumPost) -> Unit,
    onAuthorClick: ((ForumPost) -> Unit)? = null,
    onCollectClick: ((ForumPost) -> Unit)? = null,
    onDeleteClick: ((ForumPost) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItems = layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(
        shouldLoadMore,
        feedState.canLoadMore,
        feedState.isLoading,
        feedState.isLoadingMore
    ) {
        if (
            shouldLoadMore &&
            feedState.canLoadMore &&
            !feedState.isLoading &&
            !feedState.isLoadingMore &&
            feedState.items.isNotEmpty()
        ) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = feedState.isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FeedHeader(
                    section = section,
                    loaded = feedState.items.size,
                    total = feedState.total
                )
            }

            if (feedState.isLoading && feedState.items.isNotEmpty()) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            when {
                feedState.isLoading && feedState.items.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                feedState.items.isEmpty() && feedState.errorMessage != null -> item {
                    FeedMessage(
                        title = "加载失败",
                        message = feedState.errorMessage,
                        actionText = "重新加载",
                        onAction = onRefresh
                    )
                }

                feedState.isEmpty -> item {
                    FeedMessage(
                        title = "暂时没有内容",
                        message = section.emptyMessage(),
                        actionText = "刷新",
                        onAction = onRefresh
                    )
                }

                else -> {
                    items(
                        items = feedState.items,
                        key = { it.id }
                    ) { post ->
                        ForumPostCard(
                            post = post,
                            section = section,
                            onClick = onPostClick,
                            onAuthorClick = onAuthorClick,
                            onCollectClick = onCollectClick,
                            onDeleteClick = onDeleteClick
                        )
                    }

                    item {
                        LoadMoreFooter(
                            feedState = feedState,
                            onLoadMore = onLoadMore
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(
    section: ForumSection,
    loaded: Int,
    total: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = section.icon(),
                    contentDescription = null
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = section.subtitle(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelBadge(
                    text = "已加载 $loaded",
                    color = MaterialTheme.colorScheme.primary
                )
                if (total > 0) {
                    LabelBadge(
                        text = "总计 $total",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                LabelBadge(
                    text = "下拉刷新",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun LoadMoreFooter(
    feedState: FeedUiState,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            feedState.isLoadingMore -> CircularProgressIndicator()
            feedState.appendErrorMessage != null -> {
                Text(
                    text = feedState.appendErrorMessage,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(onClick = onLoadMore) {
                    Text(text = "重试加载")
                }
            }

            feedState.canLoadMore -> {
                CircularProgressIndicator()
                Text(
                    text = "继续向上滑动加载更多",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> Text(
                text = "没有更多了",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedMessage(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onAction) {
                Text(text = actionText)
            }
        }
    }
}

private fun ForumSection.icon(): ImageVector =
    when (this) {
        ForumSection.SQUARE -> Icons.Outlined.Forum
        ForumSection.QUESTIONS -> Icons.Outlined.QuestionAnswer
        ForumSection.HOT -> Icons.Outlined.Whatshot
        ForumSection.SEARCH -> Icons.Outlined.Search
        ForumSection.COLLECTIONS -> Icons.Outlined.Bookmarks
        ForumSection.MY_SHARED -> Icons.Outlined.Share
        ForumSection.USER_SHARED -> Icons.Outlined.AccountCircle
    }

private fun ForumSection.subtitle(): String =
    when (this) {
        ForumSection.SQUARE -> "社区分享、技术文章与开发者动态"
        ForumSection.QUESTIONS -> "Android 问答、源码讨论与经验沉淀"
        ForumSection.HOT -> "精选高赞问答"
        ForumSection.SEARCH -> "按关键词检索 WanAndroid 内容"
        ForumSection.COLLECTIONS -> "你收藏过的文章与链接"
        ForumSection.MY_SHARED -> "你发布到广场的分享"
        ForumSection.USER_SHARED -> "该分享人的公开文章"
    }

private fun ForumSection.emptyMessage(): String =
    when (this) {
        ForumSection.SEARCH -> "输入关键词后开始搜索"
        ForumSection.COLLECTIONS -> "还没有收藏内容"
        ForumSection.MY_SHARED -> "还没有分享文章"
        ForumSection.USER_SHARED -> "该用户暂时没有公开分享"
        else -> "稍后刷新看看新的社区动态"
    }
