package com.hrv.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.hrv.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by manishautomatic on 22/09/16.
 */

public class DeviceSelectorAdapter extends BaseAdapter implements SpinnerAdapter{

    private Context parentReference;
    private LayoutInflater mInflater;
    private ArrayList<BluetoothDevice>bleDevices  = new ArrayList<BluetoothDevice>();

    public DeviceSelectorAdapter(Context context, ArrayList<BluetoothDevice> devices){
            parentReference=context;
        mInflater=LayoutInflater.from(parentReference);
        bleDevices.clear();;
        bleDevices=devices;
    }

    @Override
    public int getCount() {
        return bleDevices.size();
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
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView txt = new TextView(parentReference);
        txt.setTextSize(16);
        txt.setPadding(5,5,5,5);
        txt.setText(bleDevices.get(position).getName());

        return  txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {
        TextView txt = new TextView(parentReference);
        txt.setText(bleDevices.get(i).getName());
        txt.setTextSize(16);
        txt.setPadding(5,5,5,5);
        return  txt;
    }
}
