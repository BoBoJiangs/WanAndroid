package com.cetc.lithium_battery.ui.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.data.model.HotKey
import com.cetc.lithium_battery.ui.components.LoadStateContent
import com.cetc.lithium_battery.ui.viewmodel.FeedUiState
import com.cetc.lithium_battery.ui.viewmodel.ForumSection
import com.cetc.lithium_battery.ui.viewmodel.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    keyword: String,
    hotKeysState: LoadState<List<HotKey>>,
    feedState: FeedUiState,
    onKeywordChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onRefreshHotKeys: () -> Unit,
    onRefreshResults: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (ForumPost) -> Unit,
    onAuthorClick: (ForumPost) -> Unit,
    onCollectClick: (ForumPost) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "搜索关键词") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch(keyword) }
                )
            )
            LoadStateContent(
                state = hotKeysState,
                onRetry = onRefreshHotKeys
            ) { hotKeys ->
                if (hotKeys.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(hotKeys, key = { it.id }) { hotKey ->
                            InputChip(
                                selected = keyword == hotKey.name,
                                onClick = { onSearch(hotKey.name) },
                                label = { Text(text = hotKey.name) }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "暂无热词",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ForumFeedScreen(
            section = ForumSection.SEARCH,
            feedState = feedState,
            onRefresh = onRefreshResults,
            onLoadMore = onLoadMore,
            onPostClick = onPostClick,
            onAuthorClick = onAuthorClick,
            onCollectClick = onCollectClick,
            modifier = Modifier.weight(1f)
        )
    }
}
