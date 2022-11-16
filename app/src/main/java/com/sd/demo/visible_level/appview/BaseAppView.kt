package com.sd.demo.visible_level.appview

import android.content.Context
import android.widget.FrameLayout
import com.sd.demo.visible_level.logMsg
import com.sd.lib.vlevel.FVisibleLevelItem

open class BaseAppView(
    context: Context,
) : FrameLayout(context), FVisibleLevelItem.Callback {

    override fun onLevelItemVisibilityChanged(item: FVisibleLevelItem) {
        logMsg {
            "AppView onLevelItemVisibilityChanged ${item.name} -> ${item.isVisible} $this"
        }
    }
}