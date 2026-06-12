package com.cetc.lithium_battery.ui.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cetc.lithium_battery.ui.components.ForumMessageCard
import com.cetc.lithium_battery.ui.viewmodel.MessageFeedUiState

@Composable
fun MessageScreen(
    unreadState: MessageFeedUiState,
    readState: MessageFeedUiState,
    onRefreshUnread: () -> Unit,
    onRefreshRead: () -> Unit,
    onLoadMoreUnread: () -> Unit,
    onLoadMoreRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isReadTab = selectedTab == 1
    val currentState = if (isReadTab) readState else unreadState

    LaunchedEffect(selectedTab) {
        if (currentState.items.isEmpty() && !currentState.isLoading) {
            if (isReadTab) onRefreshRead() else onRefreshUnread()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(text = "未读") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(text = "已读") }
            )
        }
        MessageList(
            state = currentState,
            onRefresh = if (isReadTab) onRefreshRead else onRefreshUnread,
            onLoadMore = if (isReadTab) onLoadMoreRead else onLoadMoreUnread,
            emptyText = if (isReadTab) "暂无已读消息" else "暂无未读消息",
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageList(
    state: MessageFeedUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    emptyText: String,
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

    LaunchedEffect(shouldLoadMore, state.canLoadMore, state.isLoading, state.isLoadingMore) {
        if (
            shouldLoadMore &&
            state.canLoadMore &&
            !state.isLoading &&
            !state.isLoadingMore &&
            state.items.isNotEmpty()
        ) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
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
                Text(
                    text = "站内消息",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            when {
                state.isLoading && state.items.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.items.isEmpty() && state.errorMessage != null -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(onClick = onRefresh) {
                            Text(text = "重试")
                        }
                    }
                }

                state.isEmpty -> item {
                    Text(
                        text = emptyText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    items(state.items, key = { it.id }) { message ->
                        ForumMessageCard(message = message)
                    }
                    item {
                        when {
                            state.isLoadingMore -> Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }

                            state.appendErrorMessage != null -> Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = state.appendErrorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                                OutlinedButton(onClick = onLoadMore) {
                                    Text(text = "重试加载")
                                }
                            }

                            !state.canLoadMore -> Text(
                                text = "没有更多了",
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
