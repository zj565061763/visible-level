package com.sd.lib.vlevel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FVisibleLevel
{
    private static final Map<Class<? extends FVisibleLevel>, FVisibleLevel> MAP_LEVEL = new ConcurrentHashMap<>();

    private final Map<Class<? extends FVisibleLevelItem>, FVisibleLevelItem> mMapLevelItem = new ConcurrentHashMap<>();
    private final Map<VisibilityCallback, String> mVisibilityCallbackHolder = new WeakHashMap<>();

    private boolean mIsVisible = true;
    private FVisibleLevelItem mVisibleItem;

    protected FVisibleLevel()
    {
    }

    /**
     * 返回某个等级
     *
     * @param clazz
     * @return
     */
    public static synchronized FVisibleLevel get(Class<? extends FVisibleLevel> clazz)
    {
        checkLevelClass(clazz);

        FVisibleLevel level = MAP_LEVEL.get(clazz);
        if (level == null)
        {
            level = createLevel(clazz);
            if (level == null)
                throw new RuntimeException("create level failed " + clazz.getName());

            MAP_LEVEL.put(clazz, level);
            level.onCreate();
        }
        return level;
    }

    /**
     * 清空所有等级
     */
    public static synchronized void clearLevel()
    {
        MAP_LEVEL.clear();
    }

    /**
     * 创建回调
     */
    public abstract void onCreate();

    /**
     * 初始化Item
     *
     * @param classes
     */
    protected final void initItem(Class<? extends FVisibleLevelItem>... classes)
    {
        if (classes == null || classes.length <= 0)
            throw new IllegalArgumentException("classes is null or empty");

        for (Class<? extends FVisibleLevelItem> clazz : classes)
        {
            checkLevelItemClass(clazz);
            if (mMapLevelItem.containsKey(clazz))
                continue;

            final FVisibleLevelItem item = createLevelItem(clazz);
            if (item == null)
                throw new RuntimeException("create level item failed " + clazz.getName());

            mMapLevelItem.put(clazz, item);
            item.onCreate();
        }
    }

    /**
     * 返回某个Item
     *
     * @param clazz
     * @return
     */
    public final FVisibleLevelItem getItem(Class<? extends FVisibleLevelItem> clazz)
    {
        checkLevelItemClass(clazz);

        final FVisibleLevelItem item = mMapLevelItem.get(clazz);
        if (item == null)
            throw new RuntimeException("Item was not found in level " + FVisibleLevel.this);

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
     * @param clazz
     */
    public final void visibleItem(Class<? extends FVisibleLevelItem> clazz)
    {
        checkLevelItemClass(clazz);

        if (!mIsVisible)
            throw new RuntimeException("level is not visible:" + getClass().getName());

        final FVisibleLevelItem item = mMapLevelItem.get(clazz);
        if (item == null)
            return;

        final FVisibleLevelItem old = mVisibleItem;
        if (!item.equals(old))
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
    public final void invisibleItem()
    {
        visibleItemInternal(false, mVisibleItem);
        mVisibleItem = null;
    }

    /**
     * 通知可见Item
     */
    public final void notifyVisibleItem()
    {
        visibleItemInternal(true, mVisibleItem);
    }

    private void visibleItemInternal(boolean visible, FVisibleLevelItem item)
    {
        if (item == null)
            return;

        if (mMapLevelItem.containsKey(item.getClass()))
        {
            for (VisibilityCallback callback : getVisibilityCallbacks())
            {
                callback.onLevelItemVisibilityChanged(visible, item, FVisibleLevel.this);
            }
            item.notifyVisibility(visible);
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

    private static FVisibleLevelItem createLevelItem(Class<? extends FVisibleLevelItem> clazz)
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

    private static void checkLevelClass(Class<? extends FVisibleLevel> clazz)
    {
        if (clazz == null)
            throw new NullPointerException("clazz is null");

        if (clazz == FVisibleLevel.class)
            throw new IllegalArgumentException("clazz is " + clazz.getName());
    }

    private static void checkLevelItemClass(Class<? extends FVisibleLevelItem> clazz)
    {
        if (clazz == null)
            throw new NullPointerException("clazz is null");

        if (clazz == FVisibleLevelItem.class)
            throw new IllegalArgumentException("clazz is " + clazz.getName());
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

        /**
         * item可见状态变化回调
         *
         * @param visible
         * @param item
         * @param level
         */
        void onLevelItemVisibilityChanged(boolean visible, FVisibleLevelItem item, FVisibleLevel level);
    }
}
