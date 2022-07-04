package com.sd.demo.visible_level

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.visible_level.appview.HomeView
import com.sd.demo.visible_level.appview.LiveView
import com.sd.demo.visible_level.appview.MeView
import com.sd.demo.visible_level.databinding.ActivityMainBinding
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.FVisibleLevel

class MainActivity : AppCompatActivity() {
    init {
        FVisibleLevel.isDebug = true
    }

    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var _homeView: HomeView
    private lateinit var _liveView: LiveView
    private lateinit var _meView: MeView

    private val _visibleLevel = FVisibleLevel.get(LevelHome::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _homeView = HomeView(this)
        _liveView = LiveView(this)
        _meView = MeView(this)

        _visibleLevel.apply {
            getItem(LevelHome.Home).addVisibilityCallback(_homeView)
            getItem(LevelHome.Live).addVisibilityCallback(_liveView)
            getItem(LevelHome.Me).addVisibilityCallback(_meView)
        }

        _binding.radioMenu.setOnCheckedChangeListener { _, checkedId ->
            _binding.flContainer.removeAllViews()
            when (checkedId) {
                R.id.btn_home -> {
                    _visibleLevel.setCurrentItem(LevelHome.Home)
                    _binding.flContainer.addView(_homeView)
                }
                R.id.btn_live -> {
                    _visibleLevel.setCurrentItem(LevelHome.Live)
                    _binding.flContainer.addView(_liveView)
                }
                R.id.btn_me -> {
                    _visibleLevel.setCurrentItem(LevelHome.Me)
                    _binding.flContainer.addView(_meView)
                }
                else -> {}
            }
        }
        _binding.radioMenu.check(R.id.btn_home)
    }

    override fun onStart() {
        super.onStart()
        _visibleLevel.isVisible = true
    }

    override fun onStop() {
        super.onStop()
        _visibleLevel.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        FVisibleLevel.remove(LevelHome::class.java)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}