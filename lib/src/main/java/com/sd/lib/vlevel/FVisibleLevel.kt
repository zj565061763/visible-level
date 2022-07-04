package com.sd.lib.vlevel

import android.util.Log
import java.util.*

abstract class FVisibleLevel protected constructor() {
    /** 当前等级是否可用 */
    private var _isEnabled = false
    /** 当前等级是否可见 */
    private var _isVisible = false
    /** 保存当前等级的Item */
    private val _itemHolder: MutableMap<String, FVisibleLevelItem> = mutableMapOf()

    /** 当前Item */
    var currentItem: FVisibleLevelItem = EmptyItem
        private set

    /** 父节点 */
    var parent: FVisibleLevelItem? = null
        internal set

    /**
     * 创建回调
     */
    protected abstract fun onCreate()

    /**
     * 创建Item回调
     */
    protected abstract fun onCreateItem(item: FVisibleLevelItem)

    /**
     * 设置Item列表，如果Item已存在，则跳过该Item，
     * 此方法可以重复调用来更新Item列表。
     */
    fun setItems(items: Array<String>?) {
        if (items.isNullOrEmpty()) return

        synchronized(this@FVisibleLevel) {
            for (item in items) {
                require(item.isNotEmpty()) { "item is empty" }
                if (_itemHolder.containsKey(item)) continue
                _itemHolder[item] = EmptyItem
            }
            _isEnabled = true
        }

        if (isDebug) {
            val logString = _itemHolder.keys.joinToString(
                prefix = "${this@FVisibleLevel} setItems [",
                separator = ", ",
                postfix = "]",
            )
            logMsg(logString)
        }
    }

    /**
     * 清空Item
     */
    fun clearItems() {
        logMsg("${this@FVisibleLevel} clearItems")
        synchronized(this@FVisibleLevel) {
            _isEnabled = false
            _isVisible = false
            _itemHolder.clear()
            currentItem = EmptyItem
        }
    }

    /**
     * 返回某个Item
     */
    fun getItem(name: String): FVisibleLevelItem {
        return getOrCreateItem(name)
    }

    private fun getOrCreateItem(name: String): FVisibleLevelItem {
        require(name.isNotEmpty()) { "name is empty" }
        synchronized(this@FVisibleLevel) {
            if (!_isEnabled) return EmptyItem

            val cache = _itemHolder[name]
            requireNotNull(cache) { "Item for $name was not found in level $this" }
            if (cache != EmptyItem) return cache

            return FVisibleLevelItem(name, this).also { item ->
                logMsg("${this@FVisibleLevel} create item $name")
                _itemHolder[name] = item
                onCreateItem(item)
            }
        }
    }

    /**
     * 是否可见
     */
    var isVisible: Boolean
        get() = _isVisible
        set(value) {
            synchronized(this@FVisibleLevel) {
                if (!_isEnabled) return
                if (_isVisible != value) {
                    _isVisible = value
                    logMsg("${this@FVisibleLevel} setVisible $value")
                    notifyItemVisibilityLocked(value, currentItem)
                }
            }
        }

    /**
     * 设置当前Item
     */
    @Synchronized
    fun setCurrentItem(name: String) {
        if (!_isEnabled) return

        val old = currentItem
        val uuid = if (isDebug) UUID.randomUUID().toString() else ""
        logMsg("${this@FVisibleLevel} setCurrentItem start (${old.name}) -> ($name) isVisible $isVisible uuid:$uuid")

        val item = getOrCreateItem(name)
        if (old == item) return

        currentItem = item
        notifyItemVisibilityLocked(false, old)
        if (isVisible) {
            notifyItemVisibilityLocked(true, item)
        }

        logMsg("${this@FVisibleLevel} setCurrentItem finish (${old.name}) -> ($name) isVisible $isVisible uuid:$uuid")
    }

    /**
     * 通知Item的可见状态
     */
    private fun notifyItemVisibilityLocked(visible: Boolean, item: FVisibleLevelItem) {
        if (!_isEnabled) return
        if (item == EmptyItem) return
        if (_itemHolder.containsKey(item.name)) {
            logMsg("${this@FVisibleLevel} notifyItemVisibility (${item.name}) -> $visible")
            item.notifyVisibility(visible)
        }
    }

    /**
     * 销毁
     */
    private fun destroy() {
        synchronized(this@FVisibleLevel) {
            logMsg("${this@FVisibleLevel} destroy")
            clearItems()
            // TODO wait review deadlock
            parent?.removeChildLevel(this@FVisibleLevel)
        }
    }

    companion object {
        private val sLevelHolder: MutableMap<Class<out FVisibleLevel>, FVisibleLevel> = HashMap()

        @JvmStatic
        var isDebug = false

        /**
         * 返回某个等级
         */
        @JvmStatic
        fun get(clazz: Class<out FVisibleLevel>): FVisibleLevel {
            return synchronized(this@Companion) {
                val cache = sLevelHolder[clazz]
                if (cache != null) return cache

                // 创建并保存level
                clazz.newInstance().also {
                    sLevelHolder[clazz] = it
                    logMsg("+++++ $it")
                }
            }.also {
                it.onCreate()
            }
        }

        /**
         * 清空所有等级
         */
        @JvmStatic
        fun clear() {
            synchronized(this@Companion) {
                logMsg("clear !!!!!")
                sLevelHolder.clear()
            }
        }

        /**
         * 移除等级
         */
        @JvmStatic
        fun remove(clazz: Class<out FVisibleLevel>) {
            synchronized(this@Companion) {
                sLevelHolder.remove(clazz)
            }?.let { level ->
                logMsg("----- $clazz")
                level.destroy()
            }
        }

        /**
         * 空的Item
         */
        @JvmStatic
        val EmptyItem = FVisibleLevelItem("", object : FVisibleLevel() {
            override fun onCreate() {}
            override fun onCreateItem(item: FVisibleLevelItem) {}
        })
    }
}

internal fun logMsg(msg: String) {
    if (FVisibleLevel.isDebug) {
        Log.i("FVisibleLevel", msg)
    }
}
