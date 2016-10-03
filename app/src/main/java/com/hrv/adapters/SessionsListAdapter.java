package com.hrv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hrv.models.SessionTemplate;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by manishautomatic on 03/10/16.
 */

public class SessionsListAdapter extends BaseAdapter {


    private Context parentReference;
    private LayoutInflater inflater;
    private List<SessionTemplate> data;

    private SessionsListAdapter(Context context, List<SessionTemplate> sessions){

        parentReference=context;
        

    }

    @Override
    public int getCount() {
        return 0;
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
        return null;
    }



    private class ViewHolder{
        private TextView mTxtVwSessionDate;
        private TextView mTxtVwSessionDuration;
        private TextView mTxtVwSessionSDNN;
        private TextView mTxtVwSessionRMS;
        private TextView mTxtVwSessionLnRMS;

    }
}
