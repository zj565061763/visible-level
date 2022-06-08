package com.sd.lib.vlevel

import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class FVisibleLevel protected constructor() {
    private var _isActive = false
    private var _isVisible = false
    private val _itemHolder = ConcurrentHashMap<String, FVisibleLevelItem>()

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
     * 初始化Item
     */
    @Synchronized
    fun initItems(items: Array<String>?) {
        _isActive = false
        _isVisible = false
        _itemHolder.clear()
        currentItem = EmptyItem

        if (items.isNullOrEmpty()) {
            return
        }

        for (item in items) {
            require(item.isNotEmpty()) { "item is empty" }
            val old = _itemHolder.put(item, EmptyItem)
            require(old == null) { "there is already has an item with name:$item" }
        }
        _isActive = true

        if (isDebug) {
            val logString = items.joinToString(
                prefix = "${this@FVisibleLevel} initItems \r\n",
                separator = "\r\n",
                postfix = "\r\n",
            )
            Log.i(FVisibleLevel::class.java.simpleName, logString)
        }
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
                Log.i(FVisibleLevel::class.java.simpleName, "${this@FVisibleLevel} create item $name")
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
                        Log.i(FVisibleLevel::class.java.simpleName, "${this@FVisibleLevel} setVisible $value")
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
            Log.i(FVisibleLevel::class.java.simpleName,
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
            Log.i(FVisibleLevel::class.java.simpleName,
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
                Log.i(FVisibleLevel::class.java.simpleName, "${this@FVisibleLevel} notifyItemVisibility ${item.name} -> $visible")
            }
            item.notifyVisibility(visible)
        }
    }

    companion object {
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
                        Log.i(FVisibleLevel::class.java.simpleName, "create level +++++ $it")
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
                    Log.i(FVisibleLevel::class.java.simpleName, "clear")
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
                    Log.i(FVisibleLevel::class.java.simpleName, "remove $clazz")
                }
                level.initItems(null)
                level.parent?.removeChildLevel(level)
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