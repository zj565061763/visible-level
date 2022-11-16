package com.sd.demo.visible_level

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.visible_level.databinding.ActivityMainBinding
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.FVisibleLevel
import com.sd.lib.vlevel.isVisible
import com.sd.lib.vlevel.remove
import com.sd.lib.vlevel.setCurrentItem

class MainActivity : AppCompatActivity() {

    init {
        FVisibleLevel.isDebug = true
    }

    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.radioMenu.setOnCheckedChangeListener { _, checkedId ->
            _binding.flContainer.removeAllViews()
            when (checkedId) {
                R.id.btn_home -> {
                    LevelHome::class.setCurrentItem(LevelHome.Home)
                    selectView(R.id.view_home)
                }
                R.id.btn_live -> {
                    LevelHome::class.setCurrentItem(LevelHome.Live)
                    selectView(R.id.view_live)
                }
                R.id.btn_me -> {
                    LevelHome::class.setCurrentItem(LevelHome.Me)
                    selectView(R.id.view_me)
                }
                else -> {}
            }
        }
        _binding.radioMenu.check(R.id.btn_home)
    }

    private fun selectView(id: Int) {
        repeat(_binding.flContainer.childCount) { index ->
            val child = _binding.flContainer.getChildAt(index)
            if (child?.id == id) {
                child.visibility = View.VISIBLE
            } else {
                child.visibility = View.GONE
            }
        }
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