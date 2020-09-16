package com.sd.demo.visible_level;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.demo.visible_level.appview.HomeView;
import com.sd.demo.visible_level.appview.LiveView;
import com.sd.demo.visible_level.appview.MeView;
import com.sd.demo.visible_level.databinding.ActivityMainBinding;
import com.sd.demo.visible_level.level_home.HomeLevel;
import com.sd.lib.vlevel.FVisibleLevel;

public class MainActivity extends AppCompatActivity
{
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String LEVEL_ITEM_HOME = HomeView.class.getName();
    private static final String LEVEL_ITEM_LIVE = LiveView.class.getName();
    private static final String LEVEL_ITEM_ME = MeView.class.getName();

    private ActivityMainBinding mBinding;

    private HomeView mHomeView;
    private LiveView mLiveView;
    private MeView mMeView;

    private final FVisibleLevel mVisibleLevel = FVisibleLevel.get(HomeLevel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mHomeView = new HomeView(this);
        mLiveView = new LiveView(this);
        mMeView = new MeView(this);

        mVisibleLevel.addVisibilityCallback(mVisibilityCallback);
        mVisibleLevel.getItem(HomeLevel.ItemHome.class).addVisibilityCallback(mHomeView);
        mVisibleLevel.getItem(HomeLevel.ItemLive.class).addVisibilityCallback(mLiveView);
        mVisibleLevel.getItem(HomeLevel.ItemMe.class).addVisibilityCallback(mMeView);

        mBinding.radioMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                mBinding.flContainer.removeAllViews();
                switch (checkedId)
                {
                    case R.id.btn_home:
                        mVisibleLevel.visibleItem(HomeLevel.ItemHome.class);
                        mBinding.flContainer.addView(mHomeView);
                        break;
                    case R.id.btn_live:
                        mVisibleLevel.visibleItem(HomeLevel.ItemLive.class);
                        mBinding.flContainer.addView(mLiveView);
                        break;
                    case R.id.btn_me:
                        mVisibleLevel.visibleItem(HomeLevel.ItemMe.class);
                        mBinding.flContainer.addView(mMeView);
                        break;
                    default:
                        break;
                }
            }
        });

        mBinding.radioMenu.check(R.id.btn_home);
    }

    private final FVisibleLevel.VisibilityCallback mVisibilityCallback = new FVisibleLevel.VisibilityCallback()
    {
        @Override
        public void onLevelVisibilityChanged(boolean visible, FVisibleLevel level)
        {
            Log.i(TAG, "onLevelVisibilityChanged visible:" + visible + " level:" + level);
        }
    };
}