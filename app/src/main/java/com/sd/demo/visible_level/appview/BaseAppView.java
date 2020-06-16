package com.sd.demo.visible_level.appview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sd.demo.visible_level.MainActivity;
import com.sd.lib.vlevel.callback.item.FLevelItemCallback;

public class BaseAppView extends FrameLayout implements FLevelItemCallback
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
    public void onLevelItemVisibleChanged(boolean visible)
    {
        Log.i(MainActivity.TAG, "onLevelItemVisibleChanged visible:" + visible + " view:" + this);
    }
}
