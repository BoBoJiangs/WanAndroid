package com.cetc.lithium_battery.ui.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cetc.lithium_battery.data.auth.AuthSession
import com.cetc.lithium_battery.data.model.UserProfile
import com.cetc.lithium_battery.ui.components.ErrorState
import com.cetc.lithium_battery.ui.components.LabelBadge
import com.cetc.lithium_battery.ui.viewmodel.LoadState

@Composable
fun MineScreen(
    authSession: AuthSession,
    profileState: LoadState<UserProfile>,
    unreadMessageCount: Int,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRefreshProfile: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenMyShared: () -> Unit,
    onOpenMessages: () -> Unit,
    onShareArticle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!authSession.isLoggedIn) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Login,
                        contentDescription = null
                    )
                    Text(
                        text = "登录后使用完整论坛功能",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "收藏、分享文章、我的分享和站内消息都需要 WanAndroid 账号。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onLoginClick) {
                        Text(text = "登录")
                    }
                }
            }
            return@Column
        }

        when (profileState) {
            LoadState.Loading -> Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(text = "正在同步个人信息")
                }
            }

            is LoadState.Error -> ErrorState(
                message = profileState.message,
                onRetry = onRefreshProfile,
                modifier = Modifier.fillMaxWidth()
            )

            is LoadState.Content -> ProfileCard(
                profile = profileState.data,
                fallbackName = authSession.user?.displayName.orEmpty(),
                unreadMessageCount = unreadMessageCount,
                onRefreshProfile = onRefreshProfile,
                onLogoutClick = onLogoutClick
            )
        }

        MineAction(
            icon = Icons.Outlined.Bookmarks,
            title = "我的收藏",
            description = "查看、取消收藏过的文章",
            onClick = onOpenCollections
        )
        MineAction(
            icon = Icons.Outlined.Share,
            title = "我的分享",
            description = "管理发布到广场的文章",
            onClick = onOpenMyShared
        )
        MineAction(
            icon = Icons.Outlined.Email,
            title = "站内消息",
            description = if (unreadMessageCount > 0) {
                "$unreadMessageCount 条未读消息"
            } else {
                "查看已读和未读消息"
            },
            onClick = onOpenMessages
        )
        Button(onClick = onShareArticle, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "分享文章")
        }
    }
}

@Composable
private fun ProfileCard(
    profile: UserProfile,
    fallbackName: String,
    unreadMessageCount: Int,
    onRefreshProfile: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                    Column {
                        Text(
                            text = profile.userInfo.displayName.ifBlank { fallbackName },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID ${profile.userInfo.id.takeIf { it > 0 } ?: profile.coinInfo.userId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                LabelBadge(
                    text = if (unreadMessageCount > 0) "$unreadMessageCount 未读" else "无未读",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelBadge(
                    text = "积分 ${profile.coinInfo.coinCount}",
                    color = MaterialTheme.colorScheme.secondary
                )
                LabelBadge(
                    text = "等级 ${profile.coinInfo.level}",
                    color = MaterialTheme.colorScheme.tertiary
                )
                if (profile.coinInfo.rank.isNotBlank()) {
                    LabelBadge(
                        text = "排名 ${profile.coinInfo.rank}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onRefreshProfile) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "刷新")
                }
                OutlinedButton(onClick = onLogoutClick) {
                    Icon(imageVector = Icons.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "退出")
                }
            }
        }
    }
}

@Composable
private fun MineAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
