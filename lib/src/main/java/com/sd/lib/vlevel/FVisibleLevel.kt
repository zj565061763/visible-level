package com.sd.lib.vlevel

import android.os.Looper
import android.util.Log
import java.util.*

abstract class FVisibleLevel protected constructor() {
    /** 保存当前等级的Item */
    private val _itemHolder: MutableMap<String, FVisibleLevelItem> = mutableMapOf()

    /** 当前等级是否可见 */
    private var _isVisible = false

    /** 当前等级是否已经被移除 */
    var isRemoved = false
        private set(value) {
            require(value) { "Can not set false to this flag" }
            field = value
        }

    /** 当前Item */
    var currentItem: FVisibleLevelItem = EmptyItem
        private set

    /** 当前等级是否可见 */
    val isVisible: Boolean get() = _isVisible

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
        checkUiThread()
        if (items.isEmpty()) return
        if (isRemoved) return

        for (item in items) {
            require(item.isNotEmpty()) { "item is empty" }
            if (!_itemHolder.containsKey(item)) {
                _itemHolder[item] = EmptyItem
            }
        }

        logMsg {
            _itemHolder.keys.joinToString(
                prefix = "${this@FVisibleLevel} addItems [",
                separator = ", ",
                postfix = "]",
            )
        }
    }

    /**
     * 获取名称为[name]的Item
     */
    fun getItem(name: String): FVisibleLevelItem {
        return getOrCreateItem(name)
    }

    private fun getOrCreateItem(name: String): FVisibleLevelItem {
        checkUiThread()
        require(name.isNotEmpty()) { "name is empty" }
        if (isRemoved) return EmptyItem

        val cache = requireNotNull(_itemHolder[name]) { "Item ($name) was not found in level ${this@FVisibleLevel}" }
        if (cache != EmptyItem) return cache

        return FVisibleLevelItem(
            name = name,
            level = this@FVisibleLevel,
        ).also { item ->
            _itemHolder[name] = item
            onCreateItem(item)
        }
    }

    /**
     * 设置当前等级是否可见
     */
    fun setVisible(visible: Boolean) {
        checkUiThread()
        if (isRemoved) return
        if (isVisible == visible) return

        _isVisible = visible
        logMsg { "${this@FVisibleLevel} setVisible $visible" }
        notifyItemVisibility(visible, currentItem)
    }

    /**
     * 设置当前等级可见Item为[name]
     */
    fun setCurrentItem(name: String) {
        checkUiThread()
        if (isRemoved) return
        val uuid = if (isDebug) UUID.randomUUID().toString() else ""

        val oldItem = currentItem
        val newItem = getOrCreateItem(name)
        if (newItem == oldItem) return

        currentItem = newItem

        logMsg { "${this@FVisibleLevel} start (${oldItem.name}) -> ($name) currentItem:${currentItem.name} isVisible:$isVisible uuid:$uuid" }
        notifyItemVisibility(false, oldItem)
        notifyItemVisibility(true, newItem)
        logMsg { "${this@FVisibleLevel} finish (${oldItem.name}) -> ($name) currentItem:${currentItem.name} isVisible:$isVisible uuid:$uuid" }
    }

    /**
     * 通知Item的可见状态
     */
    private fun notifyItemVisibility(value: Boolean, item: FVisibleLevelItem) {
        if (isRemoved) return
        if (item == EmptyItem) return

        if (value && !_isVisible) return
        if (value && currentItem != item) return

        if (_itemHolder.containsKey(item.name)) {
            logMsg { "${this@FVisibleLevel} item (${item.name}) -> $value" }
            item.notifyVisibility(value)
        }
    }

    /**
     * 清空Item并设置当前等级为不可见状态
     */
    fun reset() {
        checkUiThread()
        logMsg { "${this@FVisibleLevel} reset" }
        _isVisible = false
        _itemHolder.clear()
        currentItem = EmptyItem
    }

    companion object {
        /** 保存等级对象 */
        private val sLevelHolder: MutableMap<Class<out FVisibleLevel>, FVisibleLevel> = hashMapOf()

        @JvmStatic
        var isDebug = false

        /**
         * 获取等级
         */
        @JvmStatic
        fun get(clazz: Class<out FVisibleLevel>): FVisibleLevel {
            checkUiThread()
            val cache = sLevelHolder[clazz]
            if (cache != null) return cache

            return clazz.newInstance().also { level ->
                sLevelHolder[clazz] = level
                logMsg { "$level +++++" }
                level.onCreate()
            }
        }

        /**
         * 移除等级
         */
        @JvmStatic
        fun remove(clazz: Class<out FVisibleLevel>) {
            checkUiThread()
            sLevelHolder.remove(clazz)?.let { level ->
                logMsg { "$level -----" }
                level.isRemoved = true
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

internal inline fun logMsg(block: () -> String) {
    if (FVisibleLevel.isDebug) {
        Log.i("FVisibleLevel", block())
    }
}

internal fun checkUiThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) { "You should do this on ui thread." }
}

inline fun <reified T : FVisibleLevel> fVisibleLevel(): FVisibleLevel {
    return FVisibleLevel.get(T::class.java)
}

inline fun <reified T : FVisibleLevel> fVisibleLevelRemove() {
    FVisibleLevel.remove(T::class.java)
}