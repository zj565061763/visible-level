package com.sd.demo.visible_level;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.demo.visible_level.appview.HomeView;
import com.sd.demo.visible_level.appview.LiveView;
import com.sd.demo.visible_level.appview.MeView;
import com.sd.demo.visible_level.databinding.ActivityMainBinding;
import com.sd.demo.visible_level.level.LevelHome;
import com.sd.lib.vlevel.FVisibleLevel;

public class MainActivity extends AppCompatActivity
{
    static
    {
        FVisibleLevel.setDebug(true);
    }

    public static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;

    private HomeView mHomeView;
    private LiveView mLiveView;
    private MeView mMeView;

    private final FVisibleLevel mVisibleLevel = FVisibleLevel.get(LevelHome.class);

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
        mVisibleLevel.getItem(LevelHome.ItemHome).addVisibilityCallback(mHomeView);
        mVisibleLevel.getItem(LevelHome.ItemLive).addVisibilityCallback(mLiveView);
        mVisibleLevel.getItem(LevelHome.ItemMe).addVisibilityCallback(mMeView);

        mBinding.radioMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                mBinding.flContainer.removeAllViews();
                switch (checkedId)
                {
                    case R.id.btn_home:
                        mVisibleLevel.setCurrentItem(LevelHome.ItemHome);
                        mBinding.flContainer.addView(mHomeView);
                        break;
                    case R.id.btn_live:
                        mVisibleLevel.setCurrentItem(LevelHome.ItemLive);
                        mBinding.flContainer.addView(mLiveView);
                        break;
                    case R.id.btn_me:
                        mVisibleLevel.setCurrentItem(LevelHome.ItemMe);
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