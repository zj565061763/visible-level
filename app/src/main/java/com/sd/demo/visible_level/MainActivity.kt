package com.sd.demo.visible_level

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.visible_level.appview.HomeView
import com.sd.demo.visible_level.appview.LiveView
import com.sd.demo.visible_level.appview.MeView
import com.sd.demo.visible_level.databinding.ActivityMainBinding
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.FVisibleLevel

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var _homeView: HomeView
    private lateinit var _liveView: LiveView
    private lateinit var _meView: MeView

    private val _visibleLevel = FVisibleLevel.get(LevelHome::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FVisibleLevel.setDebug(true)

        setContentView(_binding.root)
        _homeView = HomeView(this)
        _liveView = LiveView(this)
        _meView = MeView(this)

        _visibleLevel.apply {
            addVisibilityCallback(_visibilityCallback)
            getItem(LevelHome.ItemHome).addVisibilityCallback(_homeView)
            getItem(LevelHome.ItemLive).addVisibilityCallback(_liveView)
            getItem(LevelHome.ItemMe).addVisibilityCallback(_meView)
            isVisible = true
        }

        _binding.radioMenu.setOnCheckedChangeListener { _, checkedId ->
            _binding.flContainer.removeAllViews()
            when (checkedId) {
                R.id.btn_home -> {
                    _visibleLevel.setCurrentItem(LevelHome.ItemHome)
                    _binding.flContainer.addView(_homeView)
                }
                R.id.btn_live -> {
                    _visibleLevel.setCurrentItem(LevelHome.ItemLive)
                    _binding.flContainer.addView(_liveView)
                }
                R.id.btn_me -> {
                    _visibleLevel.setCurrentItem(LevelHome.ItemMe)
                    _binding.flContainer.addView(_meView)
                }
                else -> {}
            }
        }
        _binding.radioMenu.check(R.id.btn_home)
    }

    private val _visibilityCallback = FVisibleLevel.VisibilityCallback { visible, level ->
        Log.i(TAG, "Level VisibilityChanged level:$level -> $visible")
    }

    companion object {
        const val TAG = "MainActivity"
    }
}