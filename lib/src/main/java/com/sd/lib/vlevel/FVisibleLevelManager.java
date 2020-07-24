package com.sd.lib.vlevel;

import android.text.TextUtils;
import android.view.View;

import com.sd.lib.vlevel.callback.ViewItemCallbackAdapter;
import com.sd.lib.vlevel.callback.item.FLevelItemCallback;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

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
        if (TextUtils.isEmpty(name))
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

        private final Map<LevelCallback, String> mCallbackHolder = new WeakHashMap<>();

        private Level(String name)
        {
            if (TextUtils.isEmpty(name))
                throw new IllegalArgumentException("name is empty");
            mName = name;
        }

        private Collection<LevelCallback> getCallbacks()
        {
            return Collections.unmodifiableCollection(mCallbackHolder.keySet());
        }

        /**
         * 添加回调，弱引用保存回调对象
         *
         * @param callback
         */
        public void addCallback(LevelCallback callback)
        {
            if (callback == null)
                return;

            mCallbackHolder.put(callback, "");
        }

        /**
         * 移除回调
         *
         * @param callback
         */
        public void removeCallback(LevelCallback callback)
        {
            mCallbackHolder.remove(callback);
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

                for (LevelCallback item : getCallbacks())
                {
                    item.onLevelVisibilityChanged(visible, Level.this);
                }

                visibleItemInternal(visible, mVisibleItem);
            }
        }

        /**
         * 添加指定Item的可见状态变化回调
         *
         * @param itemName Item名称
         * @param view     必须实现{@link FLevelItemCallback}接口
         */
        public void addLevelItemCallback(String itemName, View view)
        {
            if (TextUtils.isEmpty(itemName))
                throw new IllegalArgumentException("itemName is empty");

            if (!(view instanceof FLevelItemCallback))
                throw new IllegalArgumentException("view should be instance of " + FLevelItemCallback.class.getName());

            final ViewItemCallbackAdapter adapter = new ViewItemCallbackAdapter(mName, itemName, view);
            addCallback(adapter);
        }

        /**
         * 添加Item
         *
         * @param name
         */
        public LevelItem addItem(String name)
        {
            if (TextUtils.isEmpty(name))
                throw new IllegalArgumentException("name is empty");

            LevelItem item = mMapLevelItem.get(name);
            if (item == null)
            {
                item = new LevelItem(name, this);
                mMapLevelItem.put(name, item);

                for (LevelCallback callback : getCallbacks())
                {
                    callback.onItemAdded(item, Level.this);
                }
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
            final LevelItem item = mMapLevelItem.remove(name);
            if (item != null)
            {
                for (LevelCallback callback : getCallbacks())
                {
                    callback.onItemRemoved(item, Level.this);
                }
            }
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
            // notify item invisible ?
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
                for (LevelCallback item : getCallbacks())
                {
                    item.onItemVisibilityChanged(visible, levelItem, Level.this);
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
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null) return false;
            if (obj.getClass() != getClass()) return false;

            final Level other = (Level) obj;
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
            if (TextUtils.isEmpty(name))
                throw new IllegalArgumentException("name is empty");

            if (level == null)
                throw new NullPointerException("level is null");

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
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null) return false;
            if (obj.getClass() != getClass()) return false;

            final LevelItem other = (LevelItem) obj;
            return mName.equals(other.mName) && mLevel.equals(other.mLevel);
        }
    }

    /**
     * 等级回调
     */
    public interface LevelCallback
    {
        /**
         * 等级可见状态变化回调
         *
         * @param visible
         * @param level
         */
        void onLevelVisibilityChanged(boolean visible, Level level);

        /**
         * item被添加
         *
         * @param item
         * @param level
         */
        void onItemAdded(LevelItem item, Level level);

        /**
         * item被移除
         *
         * @param item
         * @param level
         */
        void onItemRemoved(LevelItem item, Level level);

        /**
         * item可见状态变化回调
         *
         * @param visible
         * @param item
         * @param level
         */
        void onItemVisibilityChanged(boolean visible, LevelItem item, Level level);
    }
}
