package com.cetc.lithium_battery.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WanAndroidResponse<T>(
    val data: T? = null,
    val errorCode: Int = 0,
    val errorMsg: String = ""
)

@Serializable
data class ForumPage(
    val curPage: Int = 0,
    val datas: List<ForumPost> = emptyList(),
    val offset: Int = 0,
    val over: Boolean = true,
    val pageCount: Int = 0,
    val size: Int = 0,
    val total: Int = 0
)

@Serializable
data class CommentPage(
    val curPage: Int = 0,
    val datas: List<ForumComment> = emptyList(),
    val offset: Int = 0,
    val over: Boolean = true,
    val pageCount: Int = 0,
    val size: Int = 0,
    val total: Int = 0
)

@Serializable
data class UserSharePage(
    val coinInfo: CoinInfo? = null,
    val shareArticles: ForumPage = ForumPage()
)

@Serializable
data class ForumMessagePage(
    val curPage: Int = 0,
    val datas: List<ForumMessage> = emptyList(),
    val offset: Int = 0,
    val over: Boolean = true,
    val pageCount: Int = 0,
    val size: Int = 0,
    val total: Int = 0
)

@Serializable
data class ForumTag(
    val name: String = "",
    val url: String = ""
)

@Serializable
data class ForumPost(
    val apkLink: String = "",
    val audit: Int = 0,
    val canEdit: Boolean = false,
    val chapterId: Int = 0,
    val courseId: Int = 0,
    val descMd: String = "",
    val envelopePic: String = "",
    val host: String = "",
    val id: Int = 0,
    val title: String = "",
    val desc: String = "",
    val author: String = "",
    val shareUser: String = "",
    val niceDate: String = "",
    val niceShareDate: String = "",
    val publishTime: Long = 0L,
    val shareDate: Long = 0L,
    val chapterName: String = "",
    val origin: String = "",
    val originId: Int = -1,
    val projectLink: String = "",
    val realSuperChapterId: Int = 0,
    val selfVisible: Int = 0,
    val superChapterId: Int = 0,
    val superChapterName: String = "",
    val link: String = "",
    val fresh: Boolean = false,
    val collect: Boolean = false,
    val type: Int = 0,
    val userId: Int = -1,
    val visible: Int = 0,
    val zan: Int = 0,
    val tags: List<ForumTag> = emptyList()
) {
    val actionArticleId: Int
        get() = originId.takeIf { it > 0 } ?: id

    val displayAuthor: String
        get() = author.ifBlank { shareUser }.ifBlank { "匿名用户" }

    val displayDate: String
        get() = niceShareDate.ifBlank { niceDate }.ifBlank { "刚刚" }

    val displayCategory: String
        get() = chapterName.ifBlank { superChapterName }.ifBlank { "论坛" }

    val canOpenAuthor: Boolean
        get() = userId > 0
}

@Serializable
data class ForumComment(
    val anonymous: Int = 0,
    val articleId: Int = 0,
    val content: String = "",
    val contentMd: String = "",
    val id: Int = 0,
    val niceDate: String = "",
    val publishDate: Long = 0L,
    val replyCommentId: Int = 0,
    val replyComments: List<ForumComment> = emptyList(),
    val rootCommentId: Int = 0,
    val status: Int = 0,
    val toUserId: Int = 0,
    val toUserName: String = "",
    val userId: Int = 0,
    val userName: String = "",
    val zan: Int = 0
) {
    val displayUser: String
        get() = userName.ifBlank { "匿名用户" }
}

@Serializable
data class HotKey(
    val id: Int = 0,
    val link: String = "",
    val name: String = "",
    val order: Int = 0,
    val visible: Int = 0
)

@Serializable
data class CoinInfo(
    val coinCount: Int = 0,
    val level: Int = 0,
    val nickname: String = "",
    val rank: String = "",
    val userId: Int = 0,
    val username: String = ""
)

@Serializable
data class UserInfo(
    val admin: Boolean = false,
    val chapterTops: List<Int> = emptyList(),
    val coinCount: Int = 0,
    val collectIds: List<Int> = emptyList(),
    val email: String = "",
    val icon: String = "",
    val id: Int = 0,
    val nickname: String = "",
    val password: String = "",
    val publicName: String = "",
    val token: String = "",
    val type: Int = 0,
    val username: String = ""
) {
    val displayName: String
        get() = nickname.ifBlank { publicName }.ifBlank { username }.ifBlank { "未命名用户" }
}

@Serializable
data class UserProfile(
    val coinInfo: CoinInfo = CoinInfo(),
    val userInfo: UserInfo = UserInfo()
)

@Serializable
data class ForumMessage(
    val category: Int = 0,
    val date: Long = 0L,
    val fromUser: String = "",
    val fromUserId: Int = 0,
    val fullLink: String = "",
    val id: Int = 0,
    val isRead: Int = 0,
    val link: String = "",
    val message: String = "",
    val niceDate: String = "",
    val tag: String = "",
    val title: String = "",
    val userId: Int = 0
) {
    val displayTitle: String
        get() = title.ifBlank { message }.ifBlank { "站内消息" }

    val displaySource: String
        get() = fromUser.ifBlank { tag }.ifBlank { "WanAndroid" }
}
