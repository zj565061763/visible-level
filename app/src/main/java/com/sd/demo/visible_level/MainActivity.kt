package com.sd.demo.visible_level

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.visible_level.databinding.ActivityMainBinding
import com.sd.demo.visible_level.level.LevelHome
import com.sd.lib.vlevel.FVisibleLevel
import com.sd.lib.vlevel.fVisibleLevel
import com.sd.lib.vlevel.fVisibleLevelRemove

class MainActivity : AppCompatActivity() {

    init {
        FVisibleLevel.isDebug = true
    }

    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.radioMenu.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btn_home -> {
                    fVisibleLevel<LevelHome>().setCurrentItem(LevelHome.Home)
                    selectView(R.id.view_home)
                }
                R.id.btn_live -> {
                    fVisibleLevel<LevelHome>().setCurrentItem(LevelHome.Live)
                    selectView(R.id.view_live)
                }
                R.id.btn_me -> {
                    fVisibleLevel<LevelHome>().setCurrentItem(LevelHome.Me)
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
        fVisibleLevel<LevelHome>().setVisible(true)
    }

    override fun onStop() {
        super.onStop()
        fVisibleLevel<LevelHome>().setVisible(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        fVisibleLevelRemove<LevelHome>()
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("visible-level-demo", block())
}