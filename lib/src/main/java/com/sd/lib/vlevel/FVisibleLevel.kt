package com.sd.lib.vlevel

import android.util.Log
import java.util.*

abstract class FVisibleLevel protected constructor() {
    private var _isActive = false
    private var _isVisible = false
    private val _itemHolder: MutableMap<String, FVisibleLevelItem> = mutableMapOf()

    /** 父节点 */
    var parent: FVisibleLevelItem? = null
        internal set

    /**
     * 当前Item
     */
    var currentItem: FVisibleLevelItem = EmptyItem
        private set

    /**
     * 创建回调
     */
    protected abstract fun onCreate()

    /**
     * 创建Item回调
     */
    protected abstract fun onCreateItem(item: FVisibleLevelItem)

    /**
     * 初始化Item，如果Item已存在，则跳过该Item，
     * 此方法可以重复调用来更新Item列表。
     */
    @Synchronized
    fun initItems(items: Array<String>?) {
        if (items.isNullOrEmpty()) {
            return
        }

        for (item in items) {
            require(item.isNotEmpty()) { "item is empty" }
            if (_itemHolder.containsKey(item)) continue
            _itemHolder[item] = EmptyItem
        }
        _isActive = true

        if (isDebug) {
            val logString = _itemHolder.keys.joinToString(
                prefix = "${this@FVisibleLevel} initItems \r\n",
                separator = "\r\n",
                postfix = "\r\n",
            )
            Log.i(LogTag, logString)
        }
    }

    /**
     * 清空Item
     */
    @Synchronized
    fun clearItems() {
        if (isDebug) {
            Log.i(LogTag, "${this@FVisibleLevel} clearItems")
        }
        _isActive = false
        _isVisible = false
        _itemHolder.clear()
        currentItem = EmptyItem
    }

    /**
     * 返回某个Item
     */
    fun getItem(name: String): FVisibleLevelItem {
        return getOrCreateItem(name)
    }

    @Synchronized
    private fun getOrCreateItem(name: String): FVisibleLevelItem {
        require(name.isNotEmpty()) { "name is empty" }
        if (!_isActive) return EmptyItem

        val cache = _itemHolder[name]
        requireNotNull(cache) { "Item for $name was not found in level $this" }
        if (cache != EmptyItem) return cache

        return FVisibleLevelItem(name, this).also { item ->
            if (isDebug) {
                Log.i(LogTag, "${this@FVisibleLevel} create item $name")
            }
            _itemHolder[name] = item
            onCreateItem(item)
        }
    }

    /**
     * 是否可见
     */
    var isVisible: Boolean
        get() = _isVisible
        set(value) {
            synchronized(this@FVisibleLevel) {
                if (!_isActive) return
                if (_isVisible != value) {
                    _isVisible = value
                    if (isDebug) {
                        Log.i(LogTag, "${this@FVisibleLevel} setVisible $value")
                    }
                    notifyItemVisibility(value, currentItem)
                }
            }
        }

    /**
     * 设置当前Item
     */
    @Synchronized
    fun setCurrentItem(name: String) {
        if (!_isActive) return

        val old = currentItem
        var uuid = ""

        if (isDebug) {
            uuid = UUID.randomUUID().toString()
            Log.i(LogTag,
                "${this@FVisibleLevel} setCurrentItem start (${old.name}) -> ($name) isVisible $isVisible uuid:$uuid")
        }

        val item = getOrCreateItem(name)
        if (old == item) return

        currentItem = item

        if (old != EmptyItem) {
            notifyItemVisibility(false, old)
        }
        if (isVisible) {
            notifyItemVisibility(true, item)
        }

        if (isDebug) {
            Log.i(LogTag,
                "${this@FVisibleLevel} setCurrentItem finish (${old.name}) -> ($name) isVisible $isVisible uuid:$uuid")
        }
    }

    /**
     * 通知Item的可见状态
     */
    private fun notifyItemVisibility(visible: Boolean, item: FVisibleLevelItem) {
        if (!_isActive) return
        if (_itemHolder.containsKey(item.name)) {
            if (isDebug) {
                Log.i(LogTag, "${this@FVisibleLevel} notifyItemVisibility ${item.name} -> $visible")
            }
            item.notifyVisibility(visible)
        }
    }

    companion object {
        private const val LogTag = "FVisibleLevel"
        private val levelHolder = mutableMapOf<Class<out FVisibleLevel>, FVisibleLevel>()

        @JvmStatic
        var isDebug = false

        /**
         * 返回某个等级
         */
        @JvmStatic
        fun get(clazz: Class<out FVisibleLevel>): FVisibleLevel {
            val level = synchronized(levelHolder) {
                val cache = levelHolder[clazz]
                if (cache != null) return cache

                // 创建并保存level
                clazz.newInstance().also {
                    levelHolder[clazz] = it
                    if (isDebug) {
                        Log.i(LogTag, "create level +++++ $it")
                    }
                }
            }
            level.onCreate()
            return level
        }

        /**
         * 清空所有等级
         */
        @JvmStatic
        fun clear() {
            synchronized(levelHolder) {
                if (isDebug) {
                    Log.i(LogTag, "clear")
                }
                levelHolder.clear()
            }
        }

        /**
         * 移除等级
         */
        @JvmStatic
        fun remove(clazz: Class<out FVisibleLevel>) {
            val level = synchronized(levelHolder) {
                levelHolder.remove(clazz)
            }

            if (level != null) {
                if (isDebug) {
                    Log.i(LogTag, "remove $clazz")
                }
                level.clearItems()
                synchronized(level) {
                    level.parent?.removeChildLevel(level)
                }
            }
        }

        /**
         * 空的Item
         */
        @JvmStatic
        val EmptyItem = FVisibleLevelItem("", object : FVisibleLevel() {
            override fun onCreate() {
            }

            override fun onCreateItem(item: FVisibleLevelItem) {
            }
        })
    }
}