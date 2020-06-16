package com.sd.lib.vlevel.callback;

import com.sd.lib.vlevel.FVisibleLevelManager;
import com.sd.lib.vlevel.callback.item.FLevelItemCallback;

class TargetItemVisibleCallbackAdapter extends TargetItemVisibleCallback
{
    private final FLevelItemCallback mItemCallback;

    public TargetItemVisibleCallbackAdapter(String levelName, String levelItemName, FLevelItemCallback itemCallback)
    {
        super(levelName, levelItemName);
        mItemCallback = itemCallback;
    }

    @Override
    protected final void onVisibleChangedImpl(boolean visible, FVisibleLevelManager.LevelItem item)
    {
        mItemCallback.onLevelItemVisibleChanged(visible);
    }
}
