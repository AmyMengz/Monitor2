package com.monitor.mz.xx.monitor.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.monitor.mz.xx.monitor.Constants;
import com.monitor.mz.xx.monitor.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/13.
 */

public class SimpleAdapterCustomised extends SimpleAdapter {
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     * Maps contain the data for each row, and should include all the entries specified in
     * "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     * item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     * item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     * TextViews. The first N views in this list are given the values of the first N columns
     */
    Activity context;
    private List<Map<String, Object>> mListProcesses;
    private int navigationBarHeight;

    public SimpleAdapterCustomised(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
//        this.context = context;
    }

    public SimpleAdapterCustomised(Activity context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, int navigationBarHeight) {
        this(context, data, resource, from, to);
        this.context = context;
        this.mListProcesses = (List<Map<String, Object>>) data;
        this.navigationBarHeight = navigationBarHeight;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Tag tag = null;
        if (convertView == null) {
            view = context.getLayoutInflater().inflate(R.layout.activity_processes_entry, parent, false);
            tag = new Tag();
            tag.l = (LinearLayout) view.findViewById(R.id.LpBG);
            tag.iv = (ImageView) view.findViewById(R.id.IVpIconBig);
            tag.tvPAppName = (TextView) view.findViewById(R.id.TVpAppName);
            tag.tvPName = (TextView) view.findViewById(R.id.TVpName);
            view.setTag(tag);
        } else tag = (Tag) view.getTag();
        if (position == mListProcesses.size() - 1)
            view.setPadding(0, 0, 0, navigationBarHeight);
        else view.setPadding(0, 0, 0, 0);

        if ((Boolean) mListProcesses.get(position).get(Constants.pSelected))
            tag.l.setBackgroundColor(context.getResources().getColor(R.color.bgProcessessSelected));
        else tag.l.setBackgroundColor(Color.TRANSPARENT);
        try {
            if (mListProcesses.get(position).get(Constants.pAppName).equals(mListProcesses.get(position).get(Constants.pName))){
//                tag.iv.setImageDrawable(context.getDrawable(R.drawable.transparent_pixel));
            }
            else
                tag.iv.setImageDrawable(context.getPackageManager().getApplicationIcon((String) mListProcesses.get(position).get(Constants.pPackage)));
        } catch (PackageManager.NameNotFoundException e) {
        }
        tag.tvPAppName.setText((String) mListProcesses.get(position).get(Constants.pAppName));
        tag.tvPName.setText(mListProcesses.get(position).get(Constants.pName) + " - Pid: " + mListProcesses.get(position).get(Constants.pId));

        return view;
    }

    class Tag {
        boolean selected;
        LinearLayout l;
        ImageView iv;
        TextView tvPName, tvPAppName;
    }
}
