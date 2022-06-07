package com.sd.lib.vlevel

import java.util.*

class FVisibleLevelItem internal constructor(
    /** Item名称 */
    val name: String,
    /** Item所在的等级 */
    val level: FVisibleLevel,
) {
    /** 子级 */
    private var _childLevel: FVisibleLevel? = null
    /** 回调 */
    private val _visibilityCallbackHolder = WeakHashMap<VisibilityCallback, String>()

    /**
     * Item是否可见
     */
    val isVisible: Boolean
        get() = level.isVisible && level.currentItem == this

    /**
     * 设置Item的子等级
     */
    fun setChildLevel(childLevel: FVisibleLevel?) {
        require(level != childLevel) { "child level should not be current level" }
        val old = _childLevel
        if (old != childLevel) {
            _childLevel = childLevel
            old?.isVisible = false
            childLevel?.isVisible = isVisible
        }
    }

    /**
     * 添加回调，弱引用保存回调对象
     */
    fun addVisibilityCallback(callback: VisibilityCallback?) {
        if (callback != null) {
            _visibilityCallbackHolder[callback] = ""
        }
    }

    /**
     * 移除回调
     */
    fun removeVisibilityCallback(callback: VisibilityCallback?) {
        if (callback != null) {
            _visibilityCallbackHolder.remove(callback)
        }
    }

    /**
     * 通知可见状态
     */
    internal fun notifyVisibility(visible: Boolean) {
        val callbacks = Collections.unmodifiableCollection(_visibilityCallbackHolder.keys)
        for (callback in callbacks) {
            callback.onLevelItemVisibilityChanged(visible, this)
        }
        _childLevel?.isVisible = visible
    }

    fun interface VisibilityCallback {
        /**
         * Item可见状态变化回调
         */
        fun onLevelItemVisibilityChanged(visible: Boolean, item: FVisibleLevelItem)
    }
}