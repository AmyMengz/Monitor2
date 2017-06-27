package com.monitor.mz.xx.monitor.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.monitor.mz.xx.monitor.R;
import com.monitor.mz.xx.monitor.View.MainViewInterface;
import com.mz.annotation.ContentViewInject;

@ContentViewInject(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainViewInterface{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}
