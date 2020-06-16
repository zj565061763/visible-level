package com.sd.lib.vlevel;

import java.util.Arrays;
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

    /**
     * 清空所有等级
     */
    public void clearLevel()
    {
        mMapLevel.clear();
    }

    public final class Level
    {
        private final String mName;
        private final Map<String, LevelItem> mMapLevelItem = new ConcurrentHashMap<>();

        private boolean mIsVisible = true;
        private LevelItem mVisibleItem;

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
         * 判断是否是指定名称的等级
         *
         * @param name
         * @return
         */
        public boolean is(String name)
        {
            return mName.equals(name);
        }

        /**
         * 等级是否可见
         *
         * @return
         */
        public boolean isVisible()
        {
            return mIsVisible;
        }

        /**
         * 返回可见的Item
         *
         * @return
         */
        public LevelItem getVisibleItem()
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
         * 添加回调对象
         *
         * @param callback
         */
        public void addVisibleCallback(VisibleCallback callback)
        {
            if (callback == null)
                return;
            mListCallback.add(callback);
        }

        /**
         * 移除回调对象
         *
         * @param callback
         */
        public void removeVisibleCallback(VisibleCallback callback)
        {
            mListCallback.remove(callback);
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
                item = new LevelItem(name, this);
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
         * 返回某个Item
         *
         * @param name
         * @return
         */
        public LevelItem getItem(String name)
        {
            return mMapLevelItem.get(name);
        }

        /**
         * 清空Item
         */
        public void clearItem()
        {
            mMapLevelItem.clear();
            mVisibleItem = null;
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

            final LevelItem item = mMapLevelItem.get(name);
            if (item == null)
                return;

            final LevelItem old = mVisibleItem;
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
        public void invisibleItem()
        {
            visibleItemInternal(false, mVisibleItem);
            mVisibleItem = null;
        }

        /**
         * 通知可见Item
         */
        public void notifyVisibleItem()
        {
            visibleItemInternal(true, mVisibleItem);
        }

        private void visibleItemInternal(boolean visible, LevelItem levelItem)
        {
            if (levelItem == null)
                return;

            if (mMapLevelItem.containsKey(levelItem.getName()))
            {
                for (VisibleCallback item : mListCallback)
                {
                    item.onVisibleChanged(visible, levelItem, this);
                }

                levelItem.notifyChildLevel(visible);
            }
        }

        @Override
        public int hashCode()
        {
            return mName.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Level other = (Level) o;
            return mName.equals(other.mName);
        }
    }

    public final class LevelItem
    {
        private final String mName;
        private final Level mLevel;

        private String mChildLevel;

        private LevelItem(String name, Level level)
        {
            mName = name;
            mLevel = level;
        }

        /**
         * 返回Item所在的等级
         *
         * @return
         */
        public Level getLevel()
        {
            return mLevel;
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
         * 判断是否是指定名称的Item
         *
         * @param name
         * @return
         */
        public boolean is(String name)
        {
            return mName.equals(name);
        }

        /**
         * 返回Item是否可见
         *
         * @return
         */
        public boolean isVisible()
        {
            return getLevel().isVisible() && this.equals(getLevel().mVisibleItem);
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
            final Level level = FVisibleLevelManager.this.getLevel(mChildLevel);
            if (level != null)
                level.setVisible(visible);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(new Object[]{mName, mLevel});
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final LevelItem other = (LevelItem) o;
            return mName.equals(other.mName) && mLevel.equals(other.mLevel);
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
