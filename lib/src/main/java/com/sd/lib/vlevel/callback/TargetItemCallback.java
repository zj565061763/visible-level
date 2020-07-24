package com.sd.lib.vlevel.callback;

import android.text.TextUtils;

import com.sd.lib.vlevel.FVisibleLevelManager;

abstract class TargetItemCallback implements FVisibleLevelManager.LevelCallback
{
    private final String mLevelName;
    private final String mLevelItemName;

    public TargetItemCallback(String levelName, String levelItemName)
    {
        if (TextUtils.isEmpty(levelName))
            throw new IllegalArgumentException("levelName is empty");

        if (TextUtils.isEmpty(levelItemName))
            throw new IllegalArgumentException("levelItemName is empty");

        mLevelName = levelName;
        mLevelItemName = levelItemName;
    }

    @Override
    public final void onLevelVisibilityChanged(boolean visible, FVisibleLevelManager.Level level)
    {
    }

    @Override
    public final void onItemAdded(FVisibleLevelManager.LevelItem item, FVisibleLevelManager.Level level)
    {
    }

    @Override
    public final void onItemRemoved(FVisibleLevelManager.LevelItem item, FVisibleLevelManager.Level level)
    {
    }

    @Override
    public final void onItemVisibilityChanged(boolean visible, FVisibleLevelManager.LevelItem item, FVisibleLevelManager.Level level)
    {
        if (level.is(mLevelName) && item.is(mLevelItemName))
        {
            onVisibleChangedImpl(visible, item);
        }
    }

    protected abstract void onVisibleChangedImpl(boolean visible, FVisibleLevelManager.LevelItem item);
}
