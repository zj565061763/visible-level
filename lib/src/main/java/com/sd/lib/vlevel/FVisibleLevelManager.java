package com.sd.lib.vlevel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class FVisibleLevelManager
{
    private static FVisibleLevelManager sDefault;

    public static FVisibleLevelManager getDefault()
    {
        if (sDefault == null)
        {
            synchronized (FVisibleLevelManager.class)
            {
                if (sDefault == null)
                    sDefault = new FVisibleLevelManager();
            }
        }
        return sDefault;
    }

    public FVisibleLevelManager()
    {
        // 保持public，允许实例化
    }

    private final Map<String, Level> mMapLevel = new ConcurrentHashMap<>();

    /**
     * 返回某个等级
     *
     * @param name
     * @return
     */
    public Level getLevel(String name)
    {
        if (name == null)
            return null;

        Level level = mMapLevel.get(name);
        if (level == null)
        {
            level = new Level(name);
            mMapLevel.put(name, level);
        }
        return level;
    }

    public final class Level
    {
        private final String mName;
        private final Map<String, LevelItem> mMapLevelItem = new ConcurrentHashMap<>();

        private boolean mIsVisible = true;
        private String mVisibleItem;

        private final Collection<VisibleCallback> mListCallback = new CopyOnWriteArraySet<>();

        private Level(String name)
        {
            mName = name;
        }

        /**
         * 返回等级名称
         *
         * @return
         */
        public String getName()
        {
            return mName;
        }

        /**
         * 返回可见的Item
         *
         * @return
         */
        public String getVisibleItem()
        {
            return mVisibleItem;
        }

        /**
         * 设置等级是否可见
         *
         * @param visible
         */
        public void setVisible(boolean visible)
        {
            if (mIsVisible != visible)
            {
                mIsVisible = visible;
                visibleItemInternal(visible, mVisibleItem);
            }
        }

        /**
         * 添加Item
         *
         * @param name
         */
        public LevelItem addItem(String name)
        {
            if (name == null)
                return null;

            LevelItem item = mMapLevelItem.get(name);
            if (item == null)
            {
                item = new LevelItem(name);
                mMapLevelItem.put(name, item);
            }
            return item;
        }

        /**
         * 移除Item
         *
         * @param name
         */
        public void removeItem(String name)
        {
            mMapLevelItem.remove(name);
        }

        /**
         * 清空Item
         */
        public void clearItem()
        {
            mMapLevelItem.clear();
        }

        /**
         * 设置Item可见
         *
         * @param name
         */
        public void visibleItem(String name)
        {
            if (!mIsVisible)
                throw new RuntimeException("level is not visible:" + mName);

            if (!mMapLevelItem.containsKey(name))
                return;

            final String old = mVisibleItem;
            if (!name.equals(old))
            {
                if (old != null)
                    visibleItemInternal(false, old);

                mVisibleItem = name;
                visibleItemInternal(true, name);
            }
        }

        /**
         * 设置当前可见的Item为不可见
         */
        public void invisibleItem()
        {
            visibleItemInternal(false, mVisibleItem);
            mVisibleItem = null;
        }

        private void visibleItemInternal(boolean visible, String name)
        {
            if (name == null)
                return;

            final LevelItem levelItem = mMapLevelItem.get(name);
            if (levelItem == null)
                return;

            for (VisibleCallback item : mListCallback)
            {
                item.onVisibleChanged(visible, levelItem, this);
            }

            levelItem.notifyChildLevel(visible);
        }
    }

    public final class LevelItem
    {
        private final String mName;
        private String mChildLevel;

        private LevelItem(String name)
        {
            mName = name;
        }

        /**
         * 返回Item名称
         *
         * @return
         */
        public String getName()
        {
            return mName;
        }

        /**
         * 设置Item的子等级
         *
         * @param level
         */
        public void setChildLevel(String level)
        {
            mChildLevel = level;
        }

        private void notifyChildLevel(boolean visible)
        {
            final Level level = getLevel(mChildLevel);
            if (level != null)
                level.setVisible(visible);
        }
    }

    public interface VisibleCallback
    {
        /**
         * 可见状态变化回调
         *
         * @param visible
         * @param levelItem
         * @param level
         */
        void onVisibleChanged(boolean visible, LevelItem levelItem, Level level);
    }
}
