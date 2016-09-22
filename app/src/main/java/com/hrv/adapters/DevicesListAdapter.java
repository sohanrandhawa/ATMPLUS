package com.hrv.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hrv.R;

import java.util.ArrayList;

/**
 * Created by manishautomatic on 22/09/16.
 */

public class DevicesListAdapter extends BaseAdapter {

    private Context parentReference;
    private LayoutInflater mInflater;
    private ArrayList<BluetoothDevice> _bleDevices= new ArrayList<>();

    public DevicesListAdapter(Context context, ArrayList<BluetoothDevice>devices){
        parentReference=context;
        mInflater =LayoutInflater.from(context);
        _bleDevices=devices;

    }

    @Override
    public int getCount() {
        return _bleDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;
        if(view==null){
            holder=new ViewHolder();
            view = mInflater.inflate(R.layout.layout_device_selector,null);
            holder.mTxtVwDeviceName=(TextView)view.findViewById(R.id.txtvwDeviceName);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        holder.mTxtVwDeviceName.setText(_bleDevices.get(i).getName());
        return view;
    }



    private class ViewHolder{
        TextView mTxtVwDeviceName;

    }
}
