package com.cetc.lithium_battery.ui.viewmodel

/**
 * 页面加载状态。
 *
 * sealed interface 表示“受限制的继承结构”：LoadState 只会有下面这几种状态。
 * 这样 UI 使用 when 判断时，编译器能帮我们检查是否漏掉某种状态。
 *
 * out T 是 Kotlin 泛型协变写法。初学时可以先理解为：
 * Content 里可以携带任意类型的数据，例如 ForumPage、List<ForumPost>。
 */
sealed interface LoadState<out T> {
    // data object 表示没有额外字段的单例状态，适合 Loading 这种唯一状态。
    data object Loading : LoadState<Nothing>

    // 加载成功，data 是页面真正要展示的数据。
    data class Content<T>(
        val data: T
    ) : LoadState<T>

    // 加载失败，只给 UI 暴露可显示的错误文案。
    data class Error(
        val message: String
    ) : LoadState<Nothing>
}

// Throwable 是 Kotlin/Java 的异常基类。
// 这个扩展函数把异常转换成用户能看到的中文兜底提示。
fun Throwable.toUserMessage(): String = localizedMessage?.takeIf { it.isNotBlank() }
    ?: "数据加载失败"
