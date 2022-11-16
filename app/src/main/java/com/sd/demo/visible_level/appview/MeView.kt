package com.sd.demo.visible_level.appview

import android.content.Context
import android.util.AttributeSet
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.getItem

class MeView(
    context: Context,
    attrs: AttributeSet?,
) : BaseAppView(context, attrs) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LevelHome::class.getItem(LevelHome.Me).addCallback(this)
    }
}