package com.sd.lib.vlevel.callback;

import com.sd.lib.vlevel.FVisibleLevelManager;

abstract class TargetItemVisibleCallback implements FVisibleLevelManager.ItemVisibleCallback
{
    private final String mLevelName;
    private final String mLevelItemName;

    public TargetItemVisibleCallback(String levelName, String levelItemName)
    {
        mLevelName = levelName;
        mLevelItemName = levelItemName;
    }

    @Override
    public final void onVisibleChanged(boolean visible, FVisibleLevelManager.LevelItem item)
    {
        if (item.getLevel().is(mLevelName) && item.is(mLevelItemName))
        {
            onVisibleChangedImpl(visible, item);
        }
    }

    protected abstract void onVisibleChangedImpl(boolean visible, FVisibleLevelManager.LevelItem item);
}
