package com.sd.demo.visible_level

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.visible_level.appview.HomeView
import com.sd.demo.visible_level.appview.LiveView
import com.sd.demo.visible_level.appview.MeView
import com.sd.demo.visible_level.databinding.ActivityMainBinding
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.*

class MainActivity : AppCompatActivity() {
    init {
        FVisibleLevel.isDebug = true
    }

    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var _homeView: HomeView
    private lateinit var _liveView: LiveView
    private lateinit var _meView: MeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _homeView = HomeView(this)
        _liveView = LiveView(this)
        _meView = MeView(this)

        LevelHome::class.apply {
            getItem(LevelHome.Home).addCallback(_homeView)
            getItem(LevelHome.Live).addCallback(_liveView)
            getItem(LevelHome.Me).addCallback(_meView)
        }

        _binding.radioMenu.setOnCheckedChangeListener { _, checkedId ->
            _binding.flContainer.removeAllViews()
            when (checkedId) {
                R.id.btn_home -> {
                    LevelHome::class.setCurrentItem(LevelHome.Home)
                    _binding.flContainer.addView(_homeView)
                }
                R.id.btn_live -> {
                    LevelHome::class.setCurrentItem(LevelHome.Live)
                    _binding.flContainer.addView(_liveView)
                }
                R.id.btn_me -> {
                    LevelHome::class.setCurrentItem(LevelHome.Me)
                    _binding.flContainer.addView(_meView)
                }
                else -> {}
            }
        }
        _binding.radioMenu.check(R.id.btn_home)
    }

    override fun onStart() {
        super.onStart()
        LevelHome::class.isVisible = true
    }

    override fun onStop() {
        super.onStop()
        LevelHome::class.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        LevelHome::class.remove()
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("FVisibleLevel-demo", block())
}