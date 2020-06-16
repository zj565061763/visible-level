package com.sd.demo.visible_level;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.demo.visible_level.appview.HomeView;
import com.sd.demo.visible_level.appview.LiveView;
import com.sd.demo.visible_level.appview.MeView;
import com.sd.demo.visible_level.databinding.ActivityMainBinding;
import com.sd.lib.vlevel.FVisibleLevelManager;

public class MainActivity extends AppCompatActivity
{
    public static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;

    private HomeView mHomeView;
    private LiveView mLiveView;
    private MeView mMeView;

    private final FVisibleLevelManager.Level mVisibleLevel = FVisibleLevelManager.getDefault().getLevel("home");
    private final String mLevelItemHome = HomeView.class.getName();
    private final String mLevelItemLive = LiveView.class.getName();
    private final String mLevelItemMe = MeView.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mHomeView = new HomeView(this);
        mLiveView = new LiveView(this);
        mMeView = new MeView(this);

        initVisibleLevel();
        mBinding.radioMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                mBinding.flContainer.removeAllViews();
                switch (checkedId)
                {
                    case R.id.btn_home:
                        mVisibleLevel.visibleItem(mLevelItemHome);
                        mBinding.flContainer.addView(mHomeView);
                        break;
                    case R.id.btn_live:
                        mVisibleLevel.visibleItem(mLevelItemLive);
                        mBinding.flContainer.addView(mLiveView);
                        break;
                    case R.id.btn_me:
                        mVisibleLevel.visibleItem(mLevelItemMe);
                        mBinding.flContainer.addView(mMeView);
                        break;
                    default:
                        break;
                }
            }
        });

        mBinding.radioMenu.check(R.id.btn_home);
    }

    private void initVisibleLevel()
    {
        mVisibleLevel.clearItem();
        mVisibleLevel.addItem(HomeView.class.getName());
        mVisibleLevel.addItem(LiveView.class.getName());
        mVisibleLevel.addItem(MeView.class.getName());

        mVisibleLevel.addLevelItemCallback(mLevelItemHome, mHomeView);
        mVisibleLevel.addLevelItemCallback(mLevelItemLive, mLiveView);
        mVisibleLevel.addLevelItemCallback(mLevelItemMe, mMeView);
    }
}