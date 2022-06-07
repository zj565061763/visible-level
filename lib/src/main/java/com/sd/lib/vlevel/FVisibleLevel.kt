package com.sd.lib.vlevel

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

abstract class FVisibleLevel protected constructor() {
    private val _itemHolder = ConcurrentHashMap<String, FVisibleLevelItem>()

    private var _isActive = false

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
        isVisible = false
        currentItem = EmptyItem

        if (items.isNullOrEmpty()) {
            return
        }

        _itemHolder.clear()
        for (item in items) {
            require(item.isNotEmpty()) { "item is empty" }
            _itemHolder[item] = EmptyItem
        }
        _isActive = true

        if (sIsDebug) {
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
            if (sIsDebug) {
                Log.i(FVisibleLevel::class.java.simpleName, "${this@FVisibleLevel} create item $name")
            }
            _itemHolder[name] = item
            onCreateItem(item)
        }
    }

    /**
     * 是否可见
     */
    var isVisible: Boolean = false
        set(value) {
            synchronized(this@FVisibleLevel) {
                if (!_isActive && value) return
                if (field != value) {
                    field = value
                    if (sIsDebug) {
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
        val item = getOrCreateItem(name)
        val old = currentItem
        if (old == item) return

        if (sIsDebug) {
            Log.i(FVisibleLevel::class.java.simpleName, "${this@FVisibleLevel} setCurrentItem ${old.name} -> $name isVisible $isVisible")
        }

        currentItem = item

        if (old != EmptyItem) {
            notifyItemVisibility(false, old)
        }
        if (isVisible) {
            notifyItemVisibility(true, item)
        }
    }

    /**
     * 通知Item的可见状态
     */
    private fun notifyItemVisibility(visible: Boolean, item: FVisibleLevelItem) {
        callbackHandler.post {
            if (_itemHolder.containsKey(item.name)) {
                item.notifyVisibility(visible)
            }
        }
    }

    companion object {
        private val levelHolder = mutableMapOf<Class<out FVisibleLevel>, FVisibleLevel>()
        private val callbackHandler = Handler(Looper.getMainLooper())

        @JvmStatic
        private var sIsDebug = false

        @JvmStatic
        fun setDebug(isDebug: Boolean) {
            sIsDebug = isDebug
        }

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
                    if (sIsDebug) {
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
                if (sIsDebug) {
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
                if (sIsDebug) {
                    Log.i(FVisibleLevel::class.java.simpleName, "remove $clazz")
                }
                level.initItems(null)
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