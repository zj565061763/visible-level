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
    private val _callbackHolder: MutableMap<Callback, CallbackInfo> = WeakHashMap()

    /**
     * 可见状态
     */
    @Volatile
    var isVisible: Boolean = false
        private set

    /**
     * 设置子级，如果Item已经存在子级，则覆盖后返回旧的子级。
     */
    fun setChildLevel(clazz: Class<out FVisibleLevel>?): FVisibleLevel? {
        val newChild = if (clazz == null) null else FVisibleLevel.get(clazz)
        synchronized(FVisibleLevel::class.java) {
            require(this.level != newChild) { "child level should not be current level" }

            val oldChild = _childLevel
            if (oldChild == newChild) return null

            _childLevel = newChild
            _childLevel?.isVisible = isVisible

            return oldChild
        }
    }

    /**
     * 添加[callback]，内部使用弱引用保存[callback]。
     * 如果[callback]的状态[callbackVisibility]和当前Item的状态不一致，会立即通知[callback]。
     */
    @JvmOverloads
    fun addCallback(callback: Callback, callbackVisibility: Boolean = false) {
        synchronized(FVisibleLevel::class.java) {
            if (_callbackHolder.containsKey(callback)) return

            val visible = isVisible
            _callbackHolder[callback] = CallbackInfo(visible)

            if (callbackVisibility != visible) {
                callback.onLevelItemVisibilityChanged(this@FVisibleLevelItem)
            }
        }
    }

    /**
     * 移除回调
     */
    fun removeCallback(callback: Callback) {
        synchronized(FVisibleLevel::class.java) {
            _callbackHolder.remove(callback)
        }
    }

    /** 是否正在通知回调对象 */
    private var _isNotifying = false
    /** 是否需要重新同步可见状态给回调对象 */
    private var _shouldReSync = false

    /**
     * 通知可见状态
     */
    internal fun notifyVisibility(visible: Boolean) {
        if (isVisible == visible) return
        isVisible = visible

        if (_isNotifying) {
            // 如果正在通知中，则标志为需要重新同步
            _shouldReSync = true
            return
        }

        while (true) {
            _isNotifying = true
            _shouldReSync = false

            // --------------- Notify ---------------
            val copyHolder = _callbackHolder.toMap()
            for ((callback, info) in copyHolder) {
                if (info.isVisible == isVisible) {
                    // 可见状态已经相同，则跳过当前对象
                    continue
                }

                info.isVisible = isVisible
                callback.onLevelItemVisibilityChanged(this@FVisibleLevelItem)

                if (_shouldReSync) {
                    // 通知callback的时候，外部触发了可见状态变化 停止本次遍历，准备下一次遍历。
                    break
                }
            }
            // --------------- Notify ---------------

            if (!_shouldReSync) {
                _childLevel?.isVisible = isVisible

                // 通知子级之后状态可能改变，所以这里需要再判断一下
                if (_shouldReSync) {
                    // 状态被子级改变了，继续循环
                } else {
                    _isNotifying = false
                    break
                }
            }
        }
    }

    interface Callback {
        /**
         * Item可见状态变化回调
         */
        fun onLevelItemVisibilityChanged(item: FVisibleLevelItem)
    }
}

private class CallbackInfo(
    var isVisible: Boolean = false,
)