package com.sd.lib.vlevel.callback;

import com.sd.lib.vlevel.FVisibleLevelManager;
import com.sd.lib.vlevel.callback.item.FLevelItemCallback;

class TargetItemVisibleCallbackAdapter extends TargetItemVisibleCallback
{
    private final FLevelItemCallback mLevelItemCallback;

    public TargetItemVisibleCallbackAdapter(String levelName, String levelItemName, FLevelItemCallback levelItemCallback)
    {
        super(levelName, levelItemName);
        mLevelItemCallback = levelItemCallback;
    }

    @Override
    protected final void onVisibleChangedImpl(boolean visible, FVisibleLevelManager.LevelItem item)
    {
        mLevelItemCallback.onLevelItemVisibleChanged(visible);
    }
}
