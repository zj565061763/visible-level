package com.sd.demo.visible_level;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.demo.visible_level.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
{
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
    }
}