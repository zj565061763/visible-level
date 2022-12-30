package com.sd.demo.visible_level.appview

import android.content.Context
import android.util.AttributeSet
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.fVisibleLevel

class MeView(
    context: Context,
    attrs: AttributeSet?,
) : BaseAppView(context, attrs) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        fVisibleLevel<LevelHome>().getItem(LevelHome.Me).addCallback(this)
    }
}