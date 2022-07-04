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
    private val _visibilityCallbackHolder: MutableMap<VisibilityCallback, Boolean> = WeakHashMap()

    /**
     * Item是否可见
     */
    var isVisible: Boolean = false
        private set

    /**
     * 设置Item的子级，如果已经存在子级，则覆盖后返回旧的子级
     */
    fun setChildLevel(childLevel: FVisibleLevel?): FVisibleLevel? {
        require(this.level != childLevel) { "child level should not be current level" }
        synchronized(this.level) {
            val old = _childLevel
            if (old != childLevel) {
                if (old?.parent == this@FVisibleLevelItem) {
                    old.parent = null
                }

                _childLevel = childLevel

                childLevel?.let {
                    it.parent = this@FVisibleLevelItem
                    it.isVisible = this@FVisibleLevelItem.isVisible
                }
            }
            return old
        }
    }

    /**
     * 添加[callback]，内部使用弱引用保存[callback]。
     * 如果[callback]的状态和当前Item的状态不一致，会立即把Item的状态通知到[callback]
     */
    @JvmOverloads
    fun addVisibilityCallback(callback: VisibilityCallback?, callbackVisibility: Boolean = false) {
        if (callback == null) return
        synchronized(this.level) {
            if (_visibilityCallbackHolder.containsKey(callback)) return
            _visibilityCallbackHolder[callback] = isVisible
            if (callbackVisibility != isVisible) {
                callback.onLevelItemVisibilityChanged(this@FVisibleLevelItem)
            }
        }
    }

    /**
     * 移除回调
     */
    fun removeVisibilityCallback(callback: VisibilityCallback?) {
        if (callback == null) return
        synchronized(this.level) {
            _visibilityCallbackHolder.remove(callback)
        }
    }

    /** 是否正在通知回调对象 */
    private var _isNotifying = false
    /** 是否等待通知回调对象 */
    private var _pendingNotify = false

    /**
     * 通知可见状态
     */
    internal fun notifyVisibility(visible: Boolean) {
        if (isVisible == visible) return
        isVisible = visible

        if (_isNotifying) {
            // 如果正在通知中，则标志为等待通知后返回
            _pendingNotify = true
            return
        }

        while (true) {
            _isNotifying = true
            _pendingNotify = false

            val copyHolder = _visibilityCallbackHolder.toMap()
            for ((callback, callbackVisibility) in copyHolder) {
                if (callbackVisibility == isVisible) {
                    // 如果可见状态已经相同，则跳过当前对象
                    continue
                }

                _visibilityCallbackHolder[callback] = isVisible
                callback.onLevelItemVisibilityChanged(this@FVisibleLevelItem)

                if (_pendingNotify) {
                    // 通知回调对象之后，如果状态被修改进入待通知状态，则停止本次循环，准备下一次循环
                    break
                }
            }

            if (!_pendingNotify) {
                _childLevel?.isVisible = isVisible

                // 由于通知子级的时候也可能导致状态改变，所以这里需要再判断一下是否待通知状态
                if (!_pendingNotify) {
                    _isNotifying = false
                    break
                }
            }
        }
    }

    /**
     * 移除子级
     */
    internal fun removeChildLevel(childLevel: FVisibleLevel) {
        synchronized(this.level) {
            if (_childLevel == childLevel) {
                _childLevel = null
            }
        }
    }

    fun interface VisibilityCallback {
        /**
         * Item可见状态变化回调
         */
        fun onLevelItemVisibilityChanged(item: FVisibleLevelItem)
    }
}