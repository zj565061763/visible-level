package com.sd.lib.vlevel;

import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FVisibleLevel
{
    private static final Map<Class<? extends FVisibleLevel>, FVisibleLevel> MAP_LEVEL = new ConcurrentHashMap<>();

    private static final FVisibleLevelItem EMPTY_ITEM = new FVisibleLevelItem(null, null);
    private final Map<String, FVisibleLevelItem> mMapLevelItem = new ConcurrentHashMap<>();
    private final Map<VisibilityCallback, String> mVisibilityCallbackHolder = new WeakHashMap<>();

    private boolean mIsVisible = true;
    private FVisibleLevelItem mVisibleItem;

    private static boolean sIsDebug;

    protected FVisibleLevel()
    {
    }

    public static void setDebug(boolean isDebug)
    {
        sIsDebug = isDebug;
    }

    /**
     * 返回某个等级
     *
     * @param clazz
     * @return
     */
    public static synchronized FVisibleLevel get(Class<? extends FVisibleLevel> clazz)
    {
        if (clazz == null)
            throw new NullPointerException("clazz is null");

        if (clazz == FVisibleLevel.class)
            throw new IllegalArgumentException("clazz is " + clazz.getName());

        FVisibleLevel level = MAP_LEVEL.get(clazz);
        if (level == null)
        {
            level = createLevel(clazz);
            if (level == null)
                throw new RuntimeException("create level failed " + clazz.getName());

            final String[] items = level.onCreate();
            level.initItems(items);

            MAP_LEVEL.put(clazz, level);
        }
        return level;
    }

    /**
     * 清空所有等级
     */
    public static synchronized void clearLevel()
    {
        MAP_LEVEL.clear();

        if (sIsDebug)
            Log.i(FVisibleLevel.class.getSimpleName(), "clearLevel");
    }

    /**
     * 创建回调
     *
     * @return
     */
    protected abstract String[] onCreate();

    /**
     * 创建Item回调
     *
     * @param item
     */
    protected abstract void onCreateItem(FVisibleLevelItem item);

    /**
     * 初始化Item
     *
     * @param items
     */
    private void initItems(String[] items)
    {
        if (items == null || items.length <= 0)
            throw new RuntimeException("items is null or empty " + getClass().getName());

        mMapLevelItem.clear();
        for (String item : items)
        {
            if (TextUtils.isEmpty(item))
                throw new RuntimeException("item is empty");

            mMapLevelItem.put(item, EMPTY_ITEM);
        }

        if (sIsDebug)
        {
            final StringBuilder builder = new StringBuilder("+++++ ");
            builder.append(getClass().getName()).append(" initItems").append("\r\n");
            for (String item : items)
            {
                builder.append(item).append("\r\n");
            }
            Log.i(FVisibleLevel.class.getSimpleName(), builder.toString());
        }
    }

    /**
     * 返回某个Item
     *
     * @param name
     * @return
     */
    public final FVisibleLevelItem getItem(String name)
    {
        return getOrCreateItem(name);
    }

    private FVisibleLevelItem getOrCreateItem(String name)
    {
        if (TextUtils.isEmpty(name))
            throw new RuntimeException("name is empty");

        FVisibleLevelItem item = mMapLevelItem.get(name);
        if (item == null)
            throw new RuntimeException("Item " + name + " was not found in level " + FVisibleLevel.this);

        if (item == EMPTY_ITEM)
        {
            item = new FVisibleLevelItem(name, FVisibleLevel.this);
            mMapLevelItem.put(name, item);

            if (sIsDebug)
                Log.i(FVisibleLevel.class.getSimpleName(), getClass().getName() + " create levelItem:" + name);

            onCreateItem(item);
        }
        return item;
    }

    /**
     * 添加回调，弱引用保存回调对象
     *
     * @param callback
     */
    public final void addVisibilityCallback(VisibilityCallback callback)
    {
        if (callback != null)
            mVisibilityCallbackHolder.put(callback, "");
    }

    /**
     * 移除回调
     *
     * @param callback
     */
    public final void removeVisibilityCallback(VisibilityCallback callback)
    {
        if (callback != null)
            mVisibilityCallbackHolder.remove(callback);
    }

    private Collection<VisibilityCallback> getVisibilityCallbacks()
    {
        return Collections.unmodifiableCollection(mVisibilityCallbackHolder.keySet());
    }

    /**
     * 等级是否可见
     *
     * @return
     */
    public final boolean isVisible()
    {
        return mIsVisible;
    }

    /**
     * 返回可见的Item
     *
     * @return
     */
    public final FVisibleLevelItem getVisibleItem()
    {
        return mVisibleItem;
    }

    /**
     * 设置等级是否可见
     *
     * @param visible
     */
    public final void setVisible(boolean visible)
    {
        if (mIsVisible != visible)
        {
            mIsVisible = visible;
            if (sIsDebug)
                Log.i(FVisibleLevel.class.getSimpleName(), getClass().getName() + " setVisible:" + visible);

            for (VisibilityCallback callback : getVisibilityCallbacks())
            {
                callback.onLevelVisibilityChanged(visible, FVisibleLevel.this);
            }
            visibleItemInternal(visible, mVisibleItem);
        }
    }

    /**
     * 设置Item可见
     *
     * @param name
     */
    public final void visibleItem(String name)
    {
        if (!mIsVisible)
            throw new RuntimeException("level is not visible:" + getClass().getName());

        if (sIsDebug)
            Log.i(FVisibleLevel.class.getSimpleName(), getClass().getName() + " visibleItem:" + name);

        final FVisibleLevelItem item = getOrCreateItem(name);
        final FVisibleLevelItem old = mVisibleItem;
        if (old != item)
        {
            if (old != null)
                visibleItemInternal(false, old);

            mVisibleItem = item;
            visibleItemInternal(true, item);
        }
    }

    /**
     * 设置当前可见的Item为不可见
     */
    public final void invisibleCurrentItem()
    {
        if (sIsDebug)
            Log.i(FVisibleLevel.class.getSimpleName(), getClass().getName() + " invisibleCurrentItem");

        visibleItemInternal(false, mVisibleItem);
        mVisibleItem = null;
    }

    /**
     * 通知可见Item
     */
    public final void notifyCurrentVisibleItem()
    {
        if (sIsDebug)
            Log.i(FVisibleLevel.class.getSimpleName(), getClass().getName() + " notifyCurrentVisibleItem");

        visibleItemInternal(true, mVisibleItem);
    }

    private void visibleItemInternal(boolean visible, FVisibleLevelItem item)
    {
        if (item == null)
            return;

        if (sIsDebug)
            Log.i(FVisibleLevel.class.getSimpleName(), getClass().getName() + " visibleItemInternal visible:" + visible + " item:" + item.getName());

        if (mMapLevelItem.containsKey(item.getName()))
        {
            item.notifyVisibility(visible);
        } else
        {
            throw new RuntimeException("Item " + item.getName() + " was not found in level " + FVisibleLevel.this);
        }
    }

    private static FVisibleLevel createLevel(Class<? extends FVisibleLevel> clazz)
    {
        try
        {
            return clazz.newInstance();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public interface VisibilityCallback
    {
        /**
         * 等级可见状态变化回调
         *
         * @param visible
         * @param level
         */
        void onLevelVisibilityChanged(boolean visible, FVisibleLevel level);
    }
}
