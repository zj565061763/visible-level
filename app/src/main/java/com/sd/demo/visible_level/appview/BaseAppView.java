package com.sd.demo.visible_level.appview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sd.demo.visible_level.MainActivity;
import com.sd.lib.vlevel.FVisibleLevelItem;

public class BaseAppView extends FrameLayout implements FVisibleLevelItem.VisibilityCallback
{
    public BaseAppView(@NonNull Context context)
    {
        super(context);
    }

    public BaseAppView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void onLevelItemVisibilityChanged(boolean visible, FVisibleLevelItem item)
    {
        Log.i(MainActivity.TAG, "AppView onLevelItemVisibilityChanged visible:" + visible
                + " item:" + item
                + " view:" + this);
    }
}
