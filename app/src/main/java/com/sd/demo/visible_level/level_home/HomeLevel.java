package com.sd.demo.visible_level.level_home;

import com.sd.lib.vlevel.FVisibleLevel;
import com.sd.lib.vlevel.FVisibleLevelItem;

public final class HomeLevel extends FVisibleLevel
{
    @Override
    public void onCreate()
    {
        getItem(ItemHome.class);
        getItem(ItemLive.class);
        getItem(ItemMe.class);
    }

    public static final class ItemHome extends FVisibleLevelItem
    {
        @Override
        public void onCreate()
        {
        }
    }

    public static final class ItemLive extends FVisibleLevelItem
    {
        @Override
        public void onCreate()
        {
        }
    }

    public static final class ItemMe extends FVisibleLevelItem
    {
        @Override
        public void onCreate()
        {
        }
    }
}
