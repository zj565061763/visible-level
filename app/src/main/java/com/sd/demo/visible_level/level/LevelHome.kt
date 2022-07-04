package com.sd.demo.visible_level.level

import com.sd.lib.vlevel.FVisibleLevel
import com.sd.lib.vlevel.FVisibleLevelItem

class LevelHome : FVisibleLevel() {
    override fun onCreate() {
        addItems(
            arrayOf(
                Home,
                Live,
                Me,
            )
        )
    }

    override fun onCreateItem(item: FVisibleLevelItem) {

    }

    companion object {
        const val Home = "Home"
        const val Live = "Live"
        const val Me = "Me"
    }
}