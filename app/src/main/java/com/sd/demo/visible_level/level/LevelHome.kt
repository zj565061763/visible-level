package com.sd.demo.visible_level.level

import com.sd.lib.vlevel.FVisibleLevel
import com.sd.lib.vlevel.FVisibleLevelItem

class LevelHome : FVisibleLevel() {
    override fun onCreate() {
        initItems(
            arrayOf(
                ItemHome,
                ItemLive,
                ItemMe,
            )
        )
    }

    override fun onCreateItem(item: FVisibleLevelItem) {

    }

    companion object {
        const val ItemHome = "item_home"
        const val ItemLive = "item_live"
        const val ItemMe = "item_me"
    }
}