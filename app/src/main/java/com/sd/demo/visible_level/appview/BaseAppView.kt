package com.sd.demo.visible_level.appview

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.sd.demo.visible_level.MainActivity
import com.sd.lib.vlevel.FVisibleLevelItem

open class BaseAppView(
    context: Context,
) : FrameLayout(context), FVisibleLevelItem.VisibilityCallback {

    override fun onLevelItemVisibilityChanged(item: FVisibleLevelItem) {
        Log.i(MainActivity.TAG,
            "AppView VisibilityChanged ${item.name} -> ${item.isVisible} $this")
    }
}