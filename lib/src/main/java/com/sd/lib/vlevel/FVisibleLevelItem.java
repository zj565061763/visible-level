package com.sd.lib.vlevel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class FVisibleLevelItem
{
    FVisibleLevel mLevel;
    private FVisibleLevel mChildLevel;
    private final Map<VisibilityCallback, String> mVisibilityCallbackHolder = new WeakHashMap<>();

    protected FVisibleLevelItem()
    {
    }

    /**
     * 创建回调
     */
    public abstract void onCreate();

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
     * 返回Item所在的等级
     *
     * @return
     */
    public final FVisibleLevel getLevel()
    {
        return mLevel;
    }

    /**
     * 返回Item是否可见
     *
     * @return
     */
    public final boolean isVisible()
    {
        return getLevel().isVisible() && this.equals(getLevel().getVisibleItem());
    }

    /**
     * 设置Item的子等级
     *
     * @param level
     */
    public final void setChildLevel(FVisibleLevel level)
    {
        mChildLevel = level;
    }

    void notifyVisibility(boolean visible)
    {
        for (VisibilityCallback callback : getVisibilityCallbacks())
        {
            callback.onLevelItemVisibilityChanged(visible, FVisibleLevelItem.this);
        }

        if (mChildLevel != null)
            mChildLevel.setVisible(visible);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(new Object[]{getClass(), mLevel});
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        final FVisibleLevelItem other = (FVisibleLevelItem) obj;
        return mLevel.equals(other.mLevel);
    }

    public interface VisibilityCallback
    {
        /**
         * item可见状态变化回调
         *
         * @param visible
         * @param item
         */
        void onLevelItemVisibilityChanged(boolean visible, FVisibleLevelItem item);
    }
}
