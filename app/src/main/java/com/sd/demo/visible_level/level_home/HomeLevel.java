package com.sd.demo.visible_level.level_home;

import com.sd.lib.vlevel.FVisibleLevel;

public class HomeLevel extends FVisibleLevel
{
    @Override
    public void onCreate()
    {
        getItem(HomeLevelItemHome.class);
        getItem(HomeLevelItemLive.class);
        getItem(HomeLevelItemMe.class);
    }
}
