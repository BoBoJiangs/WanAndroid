package com.cetc.lithium_battery.ui.screens.forum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.ui.components.ErrorState
import com.cetc.lithium_battery.ui.components.ForumPostCard
import com.cetc.lithium_battery.ui.components.LabelBadge
import com.cetc.lithium_battery.ui.viewmodel.ForumSection
import com.cetc.lithium_battery.ui.viewmodel.LoadState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularQuestionsScreen(
    postsState: LoadState<List<ForumPost>>,
    onRefresh: () -> Unit,
    onPostClick: (ForumPost) -> Unit,
    onAuthorClick: ((ForumPost) -> Unit)? = null,
    onCollectClick: ((ForumPost) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = postsState is LoadState.Loading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when (postsState) {
            LoadState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is LoadState.Error -> ErrorState(
                message = postsState.message,
                onRetry = onRefresh,
                modifier = Modifier.fillMaxSize()
            )

            is LoadState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PopularHeader(count = postsState.data.size)
                    }
                    items(
                        items = postsState.data,
                        key = { it.id }
                    ) { post ->
                        ForumPostCard(
                            post = post,
                            section = ForumSection.HOT,
                            onClick = onPostClick,
                            onAuthorClick = onAuthorClick,
                            onCollectClick = onCollectClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopularHeader(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Whatshot,
                contentDescription = null
            )
            Text(
                text = "热门问答",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "WanAndroid 社区精选高赞讨论，适合快速扫一眼值得收藏的问题",
                style = MaterialTheme.typography.bodyMedium
            )
            LabelBadge(
                text = "精选 $count 条",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
