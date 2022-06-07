package com.sd.lib.vlevel

import java.util.*

class FVisibleLevelItem internal constructor(
    name: String,
    level: FVisibleLevel,
) {
    /** Item名称 */
    val name = name
    /** Item所在的等级 */
    private val _level = level
    /** 子级 */
    private var _childLevel: FVisibleLevel? = null
    /** 回调 */
    private val _visibilityCallbackHolder = WeakHashMap<VisibilityCallback, String>()

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
     * Item是否可见
     */
    val isVisible: Boolean
        get() = _level.isVisible && _level.visibleItem == this

    /**
     * 设置Item的子等级
     */
    fun setChildLevel(level: FVisibleLevel) {
        require(level != this._level) { "child level should not be current level" }
        _childLevel = level
    }

    /**
     * 通知可见状态
     */
    fun notifyVisibility(visible: Boolean) {
        val callbacks = Collections.unmodifiableCollection(_visibilityCallbackHolder.keys)
        for (callback in callbacks) {
            callback.onLevelItemVisibilityChanged(visible, this@FVisibleLevelItem)
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