package com.monitor.mz.xx.monitor.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;
import com.mz.annotation.ContentViewInject;
import com.mz.annotation.InjectUtils;
import com.mz.annotation.ViewInject;

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
    @ViewInject(R.id.lv_allapp)
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
        InjectUtils.injectAll(this);
        final Resources res = getResources();
        if(Build.VERSION.SDK_INT>=19){
            float smallScreenWidth = res.getConfiguration().smallestScreenWidthDp,
                    sDensity = res.getDisplayMetrics().density;
            int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(Constants.sbh,Constants.dimen,Constants.android));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            if(!ViewConfiguration.get(this).hasPermanentMenuKey()&&!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
                    &&(res.getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT||
            ss))
        }
    }
}

