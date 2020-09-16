package com.sd.demo.visible_level;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.demo.visible_level.appview.HomeView;
import com.sd.demo.visible_level.appview.LiveView;
import com.sd.demo.visible_level.appview.MeView;
import com.sd.demo.visible_level.databinding.ActivityMainBinding;

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

    private final FVisibleLevelManager.Level mVisibleLevel = FVisibleLevelManager.getDefault().getLevel("home");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mHomeView = new HomeView(this);
        mLiveView = new LiveView(this);
        mMeView = new MeView(this);

        mVisibleLevel.addCallback(mLevelCallback);

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
                        mVisibleLevel.visibleItem(LEVEL_ITEM_HOME);
                        mBinding.flContainer.addView(mHomeView);
                        break;
                    case R.id.btn_live:
                        mVisibleLevel.visibleItem(LEVEL_ITEM_LIVE);
                        mBinding.flContainer.addView(mLiveView);
                        break;
                    case R.id.btn_me:
                        mVisibleLevel.visibleItem(LEVEL_ITEM_ME);
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
        mVisibleLevel.addItem(LEVEL_ITEM_HOME);
        mVisibleLevel.addItem(LEVEL_ITEM_LIVE);
        mVisibleLevel.addItem(LEVEL_ITEM_ME);

        mVisibleLevel.addLevelItemCallback(LEVEL_ITEM_HOME, mHomeView);
        mVisibleLevel.addLevelItemCallback(LEVEL_ITEM_LIVE, mLiveView);
        mVisibleLevel.addLevelItemCallback(LEVEL_ITEM_ME, mMeView);
    }

    private final FVisibleLevelManager.LevelCallback mLevelCallback = new FVisibleLevelManager.LevelCallback()
    {
        @Override
        public void onLevelVisibilityChanged(boolean visible, FVisibleLevelManager.Level level)
        {
            Log.i(TAG, "onLevelVisibilityChanged visible:" + visible + " level:" + level.getName());
        }

        @Override
        public void onItemAdded(FVisibleLevelManager.LevelItem item, FVisibleLevelManager.Level level)
        {
            Log.i(TAG, "onItemAdded item:" + item.getName() + " level:" + level.getName());
        }

        @Override
        public void onItemRemoved(FVisibleLevelManager.LevelItem item, FVisibleLevelManager.Level level)
        {
            Log.i(TAG, "onItemRemoved item:" + item.getName() + " level:" + level.getName());
        }

        @Override
        public void onItemVisibilityChanged(boolean visible, FVisibleLevelManager.LevelItem item, FVisibleLevelManager.Level level)
        {
            Log.i(TAG, "onItemVisibilityChanged visible:" + visible + " item:" + item.getName() + " level:" + level.getName());
        }
    };
}