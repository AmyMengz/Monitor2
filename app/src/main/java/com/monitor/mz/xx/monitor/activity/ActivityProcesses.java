package com.monitor.mz.xx.monitor.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.monitor.mz.xx.monitor.R;
import com.mz.annotation.ContentViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/4.
 */
@ContentViewInject(R.layout.activity_processes)
public class ActivityProcesses extends AppCompatActivity {
    private int navigationBarHeight;
    private List<Map<String,Object>> mListProcesses = new ArrayList<Map<String,Object>>(),
                        mListSelected = new ArrayList<Map<String,Object>>();
    private SimpleAdapter mAdapter;
    private ListView mListView;
    private BroadcastReceiver receiverFinish = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }
}

