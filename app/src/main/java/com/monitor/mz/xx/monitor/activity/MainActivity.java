package com.monitor.mz.xx.monitor.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;
import com.monitor.mz.xx.monitor.Services.ReaderService;
import com.monitor.mz.xx.monitor.View.MainViewInterface;
import com.mz.annotation.ContentViewInject;
import com.mz.annotation.ViewInject;

import java.text.DecimalFormat;
import java.util.List;

@ContentViewInject(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainViewInterface{
    private DecimalFormat mFormat = new DecimalFormat("##,###,##0"), mFormatPercent = new DecimalFormat("##0.0"),
            mFormatTime = new DecimalFormat("0.#");

    @ViewInject(R.id.TVCPUTotalP)
    TextView mTVCPUTotalP;
    @ViewInject(R.id.TVCPUAMP)
    TextView mTVCPUAMP;

    @ViewInject(R.id.TVMemoryAM)
    TextView mTVMemoryAM;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ReaderService.class));

    }

    private void setTextLabelCPU(TextView absolute, TextView percent, List<Float> values, @SuppressWarnings("unchecked") List<Integer>... valuesInteger) {
        if (valuesInteger.length == 1) {
            percent.setText(mFormatPercent.format(valuesInteger[0].get(0) * 100 / (float) mSR.getMemTotal()) + Constants.percent);
            mTVMemoryAM.setVisibility(View.VISIBLE);
            mTVMemoryAM.setText(mFormat.format(valuesInteger[0].get(0)) + Constants.kB);
        } else if (!values.isEmpty()) {
            percent.setText(mFormatPercent.format(values.get(0)) + Constants.percent);
            mTVMemoryAM.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this,ReaderService.class),mServiceVonnection,0);
    }
}
