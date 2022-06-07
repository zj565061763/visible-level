package com.sd.lib.vlevel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class FVisibleLevelItem {
    private final String mName;
    private final FVisibleLevel mLevel;

    private FVisibleLevel mChildLevel;
    private final Map<VisibilityCallback, String> mVisibilityCallbackHolder = new WeakHashMap<>();

    FVisibleLevelItem(String name, FVisibleLevel level) {
        mName = name;
        mLevel = level;
    }

    /**
     * 添加回调，弱引用保存回调对象
     *
     * @param callback
     */
    public void addVisibilityCallback(VisibilityCallback callback) {
        if (callback != null)
            mVisibilityCallbackHolder.put(callback, "");
    }

    /**
     * 移除回调
     *
     * @param callback
     */
    public void removeVisibilityCallback(VisibilityCallback callback) {
        if (callback != null)
            mVisibilityCallbackHolder.remove(callback);
    }

    private Collection<VisibilityCallback> getVisibilityCallbacks() {
        return Collections.unmodifiableCollection(mVisibilityCallbackHolder.keySet());
    }

    /**
     * Item名称
     *
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * 返回Item所在的等级
     *
     * @return
     */
    public FVisibleLevel getLevel() {
        return mLevel;
    }

    /**
     * 返回Item是否可见
     *
     * @return
     */
    public boolean isVisible() {
        return getLevel().isVisible() && getLevel().getVisibleItem() == this;
    }

    /**
     * 设置Item的子等级
     *
     * @param level
     */
    public void setChildLevel(FVisibleLevel level) {
        if (level == mLevel)
            throw new IllegalArgumentException("child level should not be current level");

        mChildLevel = level;
    }

    void notifyVisibility(boolean visible) {
        for (VisibilityCallback callback : getVisibilityCallbacks()) {
            callback.onLevelItemVisibilityChanged(visible, FVisibleLevelItem.this);
        }

        if (mChildLevel != null)
            mChildLevel.setVisible(visible);
    }

    public interface VisibilityCallback {
        /**
         * item可见状态变化回调
         *
         * @param visible
         * @param item
         */
        void onLevelItemVisibilityChanged(boolean visible, FVisibleLevelItem item);
    }
}
