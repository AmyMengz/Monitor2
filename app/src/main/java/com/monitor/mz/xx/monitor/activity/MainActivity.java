package com.monitor.mz.xx.monitor.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;
import com.monitor.mz.xx.monitor.Services.ReaderService;
import com.monitor.mz.xx.monitor.SprefUtil;
import com.monitor.mz.xx.monitor.View.MainViewInterface;
import com.mz.annotation.ContentViewInject;
import com.mz.annotation.InjectUtils;
import com.mz.annotation.OnClick;
import com.mz.annotation.ViewInject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

@ContentViewInject(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements
        MainViewInterface/*,View.OnClickListener*/{
    private DecimalFormat mFormat = new DecimalFormat("##,###,##0"), mFormatPercent = new DecimalFormat("##0.0"),
            mFormatTime = new DecimalFormat("0.#");
    private int intervalRead, intervalUpdate, intervalWidth
            ,processesMode//CPU或是memory
             ;
    private ReaderService mRservice;
    private SprefUtil sprefUtil;
    private boolean bCpuTotal,bcpuMy,bmemUsed,bmemAvailable,bmemFree,bCached,bThreshold;
    private Resources res;
    private float density;
    private int orientation;
    private List<Map<String, Object>> mListSelected;

    private Handler mHandler = new Handler();
    @ViewInject(R.id.LTopBar)
    Button mLTopBar;
    @ViewInject(R.id.BMemory)
    Button mBtnMemory;
    @ViewInject(R.id.LProcessContainer)
    LinearLayout mLProcessContainer;
    @ViewInject(R.id.LCPUTotal)
    LinearLayout mLCPUTotal;
    @ViewInject(R.id.LCPUMY)
    LinearLayout mLCPUMY;
    @ViewInject(R.id.LMemUsed)
    LinearLayout mLMemUsed;
    @ViewInject(R.id.LMemAvailable)
    LinearLayout mLMemAvailable;
    @ViewInject(R.id.LMemFree)
    LinearLayout mLMemFree;
    @ViewInject(R.id.LCached)
    LinearLayout mLCached;
    @ViewInject(R.id.LThreshold)
    LinearLayout mLThreshold;
    @ViewInject(R.id.TVCPUTotalP)
    TextView mTVCPUTotalP;
    @ViewInject(R.id.TVCPUAMP)
    TextView mTVCPUAMP;
    @ViewInject(R.id.TVMemoryMy)
    TextView mTVMemoryMy;
    @ViewInject(R.id.TVMemTotal)
    TextView mTVMemTotal;
    @ViewInject(R.id.TVMemUsed)
    TextView mTVMemUsed;
    @ViewInject(R.id.TVMemUsedP)
    TextView mTVMemUsedP;
    @ViewInject(R.id.TVMemAvailable)
    TextView mTVMemAvailable;
    @ViewInject(R.id.TVMemAvailableP)
    TextView mTVMemAvailableP;
    @ViewInject(R.id.TVMemFree)
    TextView mTVMemFree;
    @ViewInject(R.id.TVMemFreeP)
    TextView mTVMemFreeP;
    @ViewInject(R.id.TVCached)
    TextView mTVCached;
    @ViewInject(R.id.TVCachedP)
    TextView mTVCachedP;
    @ViewInject(R.id.TVThreshold)
    TextView mTVThreshold;
    @ViewInject(R.id.TVThresholdP)
    TextView mTVThresholdP;

    @ViewInject(R.id.BChooseProcess)
    Button mBChooseProcess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ReaderService.class));
        InjectUtils.injectAll(this);
        sprefUtil = new SprefUtil(getApplicationContext());
        intervalRead = sprefUtil.getInt(Constants.intervalRead,Constants.defaultIntervalUpdate);
        intervalUpdate = sprefUtil.getInt(Constants.intervalUpdate,Constants.defaultIntervalUpdate);
        intervalWidth = sprefUtil.getInt(Constants.intervalWidth,Constants.defaultIntervalWidth);
        bCpuTotal = sprefUtil.getBoolean(Constants.bcpuTotal,true);
        bcpuMy = sprefUtil.getBoolean(Constants.bcpuMy,true);
        bmemUsed = sprefUtil.getBoolean(Constants.bMemUsed,true);
        bmemAvailable = sprefUtil.getBoolean(Constants.bMemAvailable,true);
        bmemFree = sprefUtil.getBoolean(Constants.bMemFree,false);
        bCached = sprefUtil.getBoolean(Constants.bCached,false);
        bThreshold = sprefUtil.getBoolean(Constants.bThreshold,true);
        res = getResources();
        density = res.getDisplayMetrics().density;
        orientation = res.getConfiguration().orientation;
        processesMode = sprefUtil.getInt(Constants.processesMode,Constants.processesModeShowCPU);
        mBtnMemory.setText(processesMode == 0 ? getString(R.string.w_main_memory) : getString(R.string.p_cpuusage));
        mLCPUTotal.setTag(Constants.bcpuTotal);
        mLCPUMY.setTag(Constants.bcpuMy);
        mLMemUsed.setTag(Constants.bMemUsed);
//        mTVCPUAMP
    }

    private void setTextLabelCPU(TextView absolute, TextView percent, List<Float> values, @SuppressWarnings("unchecked") List<Integer>... valuesInteger) {
        if (valuesInteger.length == 1) {
            percent.setText(mFormatPercent.format(valuesInteger[0].get(0) * 100 / (float) mRservice.getMemTotal()) + Constants.percent);
            mTVMemoryMy.setVisibility(View.VISIBLE);
            mTVMemoryMy.setText(mFormat.format(valuesInteger[0].get(0)) + Constants.kB);
        } else if (!values.isEmpty()) {
            float w = values.get(0);
            percent.setText(mFormatPercent.format(values.get(0)) + Constants.percent);
            mTVMemoryMy.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this,ReaderService.class),mServiceVonnection, 0);
    }
    private ServiceConnection mServiceVonnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRservice = ((ReaderService.ReaderServiceBinder)service).getService();
            mTVMemTotal.setText(mFormat.format(mRservice.getMemTotal())+Constants.kB);
            mHandler.post(drawRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRservice = null;
        }
    };
    private Runnable drawRunnable = new Runnable() {

        @Override
        public void run() {
            mHandler.postDelayed(this,intervalUpdate);
            if(mRservice !=null){
                setTextLabelCPU(null,mTVCPUTotalP,mRservice.getCPUTotalP());
                if (processesMode==Constants.processesModeShowCPU){
                    setTextLabelCPU(null,mTVCPUAMP,mRservice.getCPUMYP());
                }else setTextLabelCPU(null, mTVCPUAMP, null, mRservice.getMemoryMy());
                setTextLabelMemory(mTVMemUsed,mTVMemUsedP,mRservice.getMemUsed());
                setTextLabelMemory(mTVMemAvailable, mTVMemAvailableP, mRservice.getMemAvailable());
                setTextLabelMemory(mTVMemFree, mTVMemFreeP, mRservice.getMemFree());
                setTextLabelMemory(mTVCached, mTVCachedP, mRservice.getCached());
                setTextLabelMemory(mTVThreshold, mTVThresholdP, mRservice.getThreshold());
            }

        }
    };
    private void setTextLabelMemory(TextView absolute, TextView percent, List<String> values) {
        if (!values.isEmpty()) {
            absolute.setText(mFormat.format(Integer.parseInt(values.get(0))) + Constants.kB);
            percent.setText(mFormatPercent.format(Integer.parseInt(values.get(0)) * 100 / (float) mRservice.getMemTotal()) + Constants.percent);
        }
    }

    @OnClick({R.id.BMemory,R.id.LCPUTotal,R.id.LCPUMY,R.id.LMemUsed,R.id.LMemAvailable,R.id.LMemFree,
                R.id.LCached,R.id.LThreshold,R.id.BChooseProcess})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BMemory:
                processesMode = processesMode==Constants.processesModeShowCPU?
                        Constants.processesModeShowMemory:Constants.processesModeShowCPU;
                sprefUtil.putInt(Constants.processesMode,processesMode);
                mBtnMemory.setText(processesMode==0?getString(R.string.w_main_memory):getString(R.string.p_cpuusage));
                break;
            case R.id.LCPUTotal:
                switchParameter(bCpuTotal = !bCpuTotal,mLCPUTotal);
                break;
            case R.id.LCPUMY:
                switchParameter(bcpuMy = !bcpuMy, mLCPUMY);
                break;
            case R.id.LMemUsed:
                switchParameter(bmemUsed=!bmemUsed,mLMemUsed);
                break;
            case R.id.LMemAvailable:

                break;
            case R.id.LMemFree:

                break;
            case R.id.LCached:

                break;
            case R.id.LThreshold:

                break;
            case R.id.BChooseProcess:
                Intent i = new Intent(MainActivity.this, ActivityProcesses.class);
                i.putExtra(Constants.listSelected, (Serializable) mListSelected);
                startActivityForResult(i, 1);
                break;

        }

    }

    private void switchParameter(boolean draw,LinearLayout lableRow){
        if(mRservice == null) return;
        sprefUtil.putBoolean(Constants.bcpuTotal,bCpuTotal);
        sprefUtil.putBoolean(Constants.bcpuMy,bcpuMy);
        sprefUtil.putBoolean(Constants.bMemUsed, bmemUsed);
        sprefUtil.putBoolean(Constants.bMemAvailable, bmemAvailable);
        sprefUtil.putBoolean(Constants.bMemFree, bmemFree);
        sprefUtil.putBoolean(Constants.bCached, bCached);
        sprefUtil.putBoolean(Constants.bThreshold, bThreshold);
        ImageView icon = (ImageView)lableRow.getChildAt(0);
        if(draw){
            icon.setImageResource(R.drawable.icon_play);
        }else icon.setImageResource(R.drawable.icon_pause);


    }
}
