package com.sd.demo.visible_level.appview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import com.sd.demo.visible_level.MainActivity
import com.sd.lib.vlevel.FVisibleLevelItem

open class BaseAppView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), FVisibleLevelItem.VisibilityCallback {

    override fun onLevelItemVisibilityChanged(visible: Boolean, item: FVisibleLevelItem) {
        Log.i(MainActivity.TAG,
            "AppView onLevelItemVisibilityChanged ${item.name} -> $visible $this")
    }
}