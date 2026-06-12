package com.cetc.lithium_battery.ui.screens.forum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cetc.lithium_battery.data.model.ForumComment
import com.cetc.lithium_battery.data.model.ForumPost
import com.cetc.lithium_battery.ui.components.ErrorState
import com.cetc.lithium_battery.ui.components.ForumCommentCard
import com.cetc.lithium_battery.ui.components.LabelBadge
import com.cetc.lithium_battery.ui.components.hostLabel
import com.cetc.lithium_battery.ui.components.plainTextFromHtml
import com.cetc.lithium_battery.ui.viewmodel.ForumSection
import com.cetc.lithium_battery.ui.viewmodel.LoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: ForumPost?,
    section: ForumSection?,
    commentsState: LoadState<List<ForumComment>>,
    onRetry: () -> Unit,
    onCollectClick: (ForumPost) -> Unit,
    onAuthorClick: (ForumPost) -> Unit,
    modifier: Modifier = Modifier
) {
    if (post == null || section == null) {
        ErrorState(
            message = "未找到帖子，请返回列表刷新后重试",
            onRetry = onRetry,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val uriHandler = LocalUriHandler.current

    PullToRefreshBox(
        isRefreshing = commentsState is LoadState.Loading,
        onRefresh = onRetry,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PostHeader(
                    post = post,
                    section = section,
                    onCollectClick = { onCollectClick(post) },
                    onAuthorClick = { onAuthorClick(post) },
                    onOpenOriginal = {
                        if (post.link.isNotBlank()) {
                            uriHandler.openUri(post.link)
                        }
                    }
                )
            }

            if (section != ForumSection.QUESTIONS && section != ForumSection.HOT) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "当前内容来自文章/分享接口，开放 API 未提供对应评论列表",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    CommentsHeader(commentsState = commentsState)
                }
                when (commentsState) {
                    is LoadState.Content -> {
                        if (commentsState.data.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "还没有评论",
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(
                                items = commentsState.data,
                                key = { it.id }
                            ) { comment ->
                                ForumCommentCard(comment = comment)
                            }
                        }
                    }

                    is LoadState.Error -> item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = commentsState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                OutlinedButton(onClick = onRetry) {
                                    Text(text = "重试评论")
                                }
                            }
                        }
                    }

                    LoadState.Loading -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostHeader(
    post: ForumPost,
    section: ForumSection,
    onCollectClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onOpenOriginal: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = plainTextFromHtml(post.title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelBadge(
                    text = section.title,
                    color = MaterialTheme.colorScheme.primary
                )
                if (post.zan > 0) {
                    LabelBadge(
                        text = "${post.zan} 赞",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                post.link.hostLabel()?.let { host ->
                    LabelBadge(
                        text = host,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Text(
                text = "${post.displayAuthor} · ${post.displayDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val content = plainTextFromHtml(post.desc)
            if (content.isNotBlank()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (post.link.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenOriginal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "阅读原文")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onCollectClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (post.collect) {
                                    Icons.Outlined.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (post.collect) "取消收藏" else "收藏")
                        }
                        if (post.canOpenAuthor) {
                            OutlinedButton(
                                onClick = onAuthorClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonSearch,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "分享人")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsHeader(commentsState: LoadState<List<ForumComment>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "讨论",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            when (commentsState) {
                is LoadState.Content -> {
                    if (commentsState.data.isNotEmpty()) {
                        LabelBadge(
                            text = "${commentsState.data.size} 条",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                is LoadState.Error -> LabelBadge(
                    text = "加载失败",
                    color = MaterialTheme.colorScheme.error
                )

                LoadState.Loading -> LabelBadge(
                    text = "刷新中",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider()
    }
}
