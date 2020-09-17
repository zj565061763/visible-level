package com.sd.demo.visible_level.level_home;

import com.sd.lib.vlevel.FVisibleLevel;
import com.sd.lib.vlevel.FVisibleLevelItem;

public final class LevelHome extends FVisibleLevel
{
    public static final String ITEM_HOME = "item_home";
    public static final String ITEM_LIVE = "item_live";
    public static final String ITEM_ME = "item_me";

    @Override
    protected void onCreate()
    {
        initItems(new String[]{
                ITEM_HOME,
                ITEM_LIVE,
                ITEM_ME,
        });
    }

    @Override
    protected void onCreateItem(FVisibleLevelItem item)
    {

    }
}
