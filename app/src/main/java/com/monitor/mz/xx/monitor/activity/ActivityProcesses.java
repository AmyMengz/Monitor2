package com.monitor.mz.xx.monitor.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;
import com.monitor.mz.xx.monitor.adapter.SimpleAdapterCustomised;
import com.monitor.mz.xx.monitor.utils.ProcessManager;
import com.mz.annotation.ContentViewInject;
import com.mz.annotation.InjectUtils;
import com.mz.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.monitor.mz.xx.monitor.Constants.pAppName;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectUtils.injectAll(this);
        final Resources res = getResources();

        if(Build.VERSION.SDK_INT>=19){
            float smallScreenWidth = res.getConfiguration().smallestScreenWidthDp,//声明了与你的应用程序兼容的最小的最小宽度
                    sDensity = res.getDisplayMetrics().density;//屏幕密度
            int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(Constants.sbh,Constants.dimen,Constants.android));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            if(!ViewConfiguration.get(this).hasPermanentMenuKey()&&!KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
                    &&(res.getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT||
            smallScreenWidth >560)){
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(Constants.nbh,Constants.dimen,Constants.android));
                if(navigationBarHeight == 0)
                    navigationBarHeight = (int)(48*sDensity);
                FrameLayout nb = (FrameLayout)findViewById(R.id.LNavigationBar);
                nb.setVisibility(View.VISIBLE);
                ((FrameLayout.LayoutParams)nb.getLayoutParams()).height = navigationBarHeight;
            }
            RelativeLayout lTopBar = ((RelativeLayout)findViewById(R.id.LWindowMyPlacesTopBar));
            int pLeft = lTopBar.getPaddingLeft();
            int pTop = lTopBar.getPaddingTop();
            int pRight = lTopBar.getPaddingRight();
            int pBottom = lTopBar.getPaddingBottom();
            lTopBar.setPadding(pLeft,pTop+statusBarHeight,pRight,pBottom);
        }
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mListProcesses = (List<Map<String, Object>>) savedInstanceState.getSerializable(Constants.listProcesses);
            mListSelected = (List<Map<String, Object>>) savedInstanceState.getSerializable(Constants.listSelected);
            if (mListSelected != null && !mListSelected.isEmpty()) {
                for(Map<String, Object> process : mListProcesses)
                    for (Map<String, Object> selected : mListSelected)
                        if (process.get(Constants.pId).equals(selected.get(Constants.pId)))
                            process.put(Constants.pSelected, Boolean.TRUE);
            } else mListSelected = new ArrayList<Map<String, Object>>();

        } else {
            PackageManager pm = getPackageManager();
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses;
            if(Build.VERSION.SDK_INT<22){
                runningAppProcesses = ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
            }else runningAppProcesses= ProcessManager.getRunningAppProcessInfo(this);
            if(runningAppProcesses!=null){
                int pid = Process.myPid();
                for (ActivityManager.RunningAppProcessInfo p:runningAppProcesses){
                    if(pid !=p.pid){
                        String name = null;
                        try{
                            name = (String)pm.getApplicationLabel(pm.getApplicationInfo(p.pkgList!=null
                                    &&p.pkgList.length>0?p.pkgList[0]:p.processName,0));
                        }catch (PackageManager.NameNotFoundException e){
                        }catch (Resources.NotFoundException e){
                        }
                        if(name == null){
                            name = p.processName;
                        }
                        mListProcesses.add(mapDataForPlacesList(false, name, String.valueOf(p.pid),
                                p.pkgList != null && p.pkgList.length > 0 ? p.pkgList[0] : p.processName, p.processName));
                    }
                }
                Collections.sort(mListProcesses, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        if(o1.get(pAppName).equals(o2.get(pAppName)))
                            return 0;
                        return ((String)o1.get(pAppName)).compareTo((String)o2.get(pAppName))< 0 ? -1 : 1;
                    }
                });
                List<Map<String,Object>> mListSelectedProv = (List<Map<String,Object>>)getIntent().getSerializableExtra(Constants.listSelected);
                if (mListSelectedProv!=null&&!mListSelectedProv.isEmpty()){
                    for (Map<String,Object> processSelected:mListSelectedProv){
                        Iterator<Map<String,Object>> iteratorListProcesses = mListProcesses.iterator();
                        while (iteratorListProcesses.hasNext()){
                            Map<String,Object> process = iteratorListProcesses.next();
                            if(process.get(Constants.pId).equals(processSelected.get(Constants.pId))){
                                iteratorListProcesses.remove();
                            }
                        }
                    }
                }
            }else {
                mListView.setVisibility(View.GONE);
                findViewById(R.id.LProcessesProblem).setVisibility(View.VISIBLE);
            }
        }
        if(mListProcesses==null||mListProcesses.isEmpty()){
            mListView.setVisibility(View.GONE);
            findViewById(R.id.LProcessesProblem).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.TVError)).setText(R.string.w_processes_android_51_problem);
            findViewById(R.id.BOK).setClickable(false);
            return;
        }
        mAdapter = new SimpleAdapterCustomised(this, mListProcesses, R.layout.activity_processes_entry,
                new String[] { Constants.pSelected, Constants.pPackage, Constants.pName, Constants.pId },
                new int[] { R.id.LpBG, R.id.IVpIconBig, R.id.TVpAppName, R.id.TVpName },navigationBarHeight);
        mListView.setAdapter(mAdapter);
    }
    private Map<String, Object> mapDataForPlacesList(boolean selected, String pAppName, String pid, String pPackage, String pName) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put(Constants.pSelected, selected);
        entry.put(Constants.pAppName, pAppName);
        entry.put(Constants.pId, pid);
        entry.put(Constants.pPackage, pPackage);
        entry.put(Constants.pName, pName);
        return entry;
    }
    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(receiverFinish, new IntentFilter(Constants.actionFinishActivity));
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverFinish);
    }
}

