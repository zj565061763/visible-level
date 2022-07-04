package com.sd.lib.vlevel

import android.util.Log
import java.util.*
import kotlin.reflect.KClass

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

    /**
     * 等级创建回调
     */
    protected abstract fun onCreate()

    /**
     * Item创建回调
     */
    protected abstract fun onCreateItem(item: FVisibleLevelItem)

    /**
     * 添加Item，跳过重复的Item
     */
    fun addItems(items: Array<String>) {
        if (items.isEmpty()) return
        if (!hasLevel(this@FVisibleLevel)) return
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
                prefix = "${this@FVisibleLevel} addItems [",
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
        synchronized(this@FVisibleLevel) {
            logMsg("${this@FVisibleLevel} clearItems")
            _isEnabled = false
            _isVisible = false
            _itemHolder.clear()
            currentItem = EmptyItem
        }
    }

    /**
     * 获取名称为[name]的Item
     */
    fun getItem(name: String): FVisibleLevelItem {
        return getOrCreateItem(name)
    }

    private fun getOrCreateItem(name: String): FVisibleLevelItem {
        require(name.isNotEmpty()) { "name is empty" }
        return synchronized(this@FVisibleLevel) {
            if (!_isEnabled) return EmptyItem

            val cache = requireNotNull(_itemHolder[name]) { "Item ($name) was not found in level ${this@FVisibleLevel}" }
            if (cache != EmptyItem) return cache

            FVisibleLevelItem(name, this@FVisibleLevel).also { item ->
                logMsg("${this@FVisibleLevel} create item $name")
                _itemHolder[name] = item
            }
        }.also {
            onCreateItem(it)
        }
    }

    /**
     * 当前等级可见状态
     */
    var isVisible: Boolean
        get() = synchronized(this@FVisibleLevel) { _isVisible }
        set(value) = setVisibleInternal(value)

    private fun setVisibleInternal(value: Boolean) {
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
     * 设置当前等级可见Item为[name]
     */
    fun setCurrentItem(name: String) {
        val uuid = if (isDebug) UUID.randomUUID().toString() else ""
        synchronized(this@FVisibleLevel) {
            if (!_isEnabled) return

            val oldItem = currentItem
            val newItem = getOrCreateItem(name)

            if (oldItem == newItem) return
            currentItem = newItem

            logMsg("${this@FVisibleLevel} setCurrentItem start (${oldItem.name}) -> ($name) isVisible $isVisible uuid:$uuid")
            notifyItemVisibilityLocked(false, oldItem)
            notifyItemVisibilityLocked(true, newItem)
            logMsg("${this@FVisibleLevel} setCurrentItem finish (${oldItem.name}) -> ($name) isVisible $isVisible uuid:$uuid")
        }
    }

    /**
     * 通知Item的可见状态
     */
    private fun notifyItemVisibilityLocked(value: Boolean, item: FVisibleLevelItem) {
        if (!_isEnabled) return
        if (item == EmptyItem) return
        if (value && !_isVisible) return
        if (_itemHolder.containsKey(item.name)) {
            logMsg("${this@FVisibleLevel} notify (${item.name}) -> $value")
            item.notifyVisibility(value)
        }
    }

    companion object {
        /** 保存等级对象 */
        private val sLevelHolder: MutableMap<Class<out FVisibleLevel>, FVisibleLevel> = HashMap()

        @JvmStatic
        var isDebug = false

        /**
         * 获取等级
         */
        @JvmStatic
        fun get(clazz: Class<out FVisibleLevel>): FVisibleLevel {
            return synchronized(this@Companion) {
                val cache = sLevelHolder[clazz]
                if (cache != null) return cache

                // 创建并保存level
                clazz.newInstance().also { level ->
                    sLevelHolder[clazz] = level
                    logMsg("+++++ $level")
                }
            }.also {
                it.onCreate()
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
                level.clearItems()
                // 通知父级Item移除当前level?
            }
        }

        /**
         * 清空等级
         */
        @JvmStatic
        fun clear() {
            synchronized(this@Companion) {
                logMsg("clear !!!!!")
                sLevelHolder.clear()
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

        private fun hasLevel(level: FVisibleLevel): Boolean {
            synchronized(this@Companion) {
                return level == sLevelHolder[level.javaClass]
            }
        }
    }
}

internal fun logMsg(msg: String) {
    if (FVisibleLevel.isDebug) {
        Log.i("FVisibleLevel", msg)
    }
}

/**
 * 当前等级可见状态
 */
var KClass<out FVisibleLevel>.isVisible: Boolean
    get() = FVisibleLevel.get(this.java).isVisible
    set(value) {
        FVisibleLevel.get(this.java).isVisible = value
    }

/**
 * 设置当前等级的可见Item为[name]
 */
fun KClass<out FVisibleLevel>.setCurrentItem(name: String) {
    FVisibleLevel.get(this.java).setCurrentItem(name)
}

/**
 * 获取名称为[name]的Item
 */
fun KClass<out FVisibleLevel>.getItem(name: String): FVisibleLevelItem {
    return FVisibleLevel.get(this.java).getItem(name)
}

/**
 * 移除等级
 */
fun KClass<out FVisibleLevel>.remove() {
    FVisibleLevel.remove(this.java)
}