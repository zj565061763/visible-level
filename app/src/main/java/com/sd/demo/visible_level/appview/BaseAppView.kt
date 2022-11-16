package com.sd.demo.visible_level.appview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.sd.demo.visible_level.logMsg
import com.sd.lib.vlevel.FVisibleLevelItem

open class BaseAppView(
    context: Context,
    attrs: AttributeSet?,
) : FrameLayout(context, attrs), FVisibleLevelItem.Callback {

    override fun onLevelItemVisibilityChanged(item: FVisibleLevelItem) {
        logMsg {
            "${javaClass.simpleName} onLevelItemVisibilityChanged ${item.name} -> ${item.isVisible}"
        }
    }
}