package com.hrv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hrv.R;
import com.hrv.models.SessionTemplate;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by manishautomatic on 03/10/16.
 */

public class SessionsListAdapter extends BaseAdapter {


    private Context parentReference;
    private LayoutInflater inflater;
    private ArrayList<SessionTemplate> data = new ArrayList<>();

    public SessionsListAdapter(Context context, List<SessionTemplate> sessions){

        parentReference=context;
        inflater = LayoutInflater.from(context);
        data.clear();
        data.addAll(sessions);


    }

    @Override
    public int getCount() {
        return data.size();
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
            view = inflater.inflate(R.layout.layout_sessions_list_row,null);
            holder.mTxtVwSessionDate=(TextView)view.findViewById(R.id.txtvwSessionDate);
            holder.mTxtVwSessionDuration=(TextView)view.findViewById(R.id.txtvwSessionDuration);
            holder.mTxtVwSessionSDNN=(TextView)view.findViewById(R.id.txtvwSessionSDNN);
            holder.mTxtVwSessionRMS=(TextView)view.findViewById(R.id.txtvwSessionRMSSD);
            holder.mTxtVwSessionLnRMS=(TextView)view.findViewById(R.id.txtvwSessionLnRSDD);
            view.setTag(holder);
        }else{
            holder =(ViewHolder)view.getTag();
        }

        Double SDNN= data.get(i).getSdNN();
        Double RMSSD = data.get(i).getRms();
        Double LnRMS = data.get(i).getLnRMS();
        Long sessionDuration = data.get(i).getTimeElapsed();
        Long sessionStartDate = data.get(i).getStartTime();

        Date date = new Date(sessionStartDate);
        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
        String sessionDate = dateformat.format(date);

        DecimalFormat df = new DecimalFormat("#.##");
        String strSDNN = df.format(SDNN);
        String strRMSSD = df.format(RMSSD);
        String strLnRMSSD = df.format(LnRMS);

        holder.mTxtVwSessionDate.setText("Date: "+sessionDate);
        double sessionDurationInMins = ((double)sessionDuration/1000)/60;
        String strDurationMinutes = df.format(sessionDurationInMins);
        holder.mTxtVwSessionDuration.setText("Duration ( mins): "+strDurationMinutes);
        holder.mTxtVwSessionSDNN.setText("SDNN: "+strSDNN);
        holder.mTxtVwSessionRMS.setText("RMSSD: "+strRMSSD);
        holder.mTxtVwSessionLnRMS.setText("LnRMSSD: "+strLnRMSSD);


        return view;
    }



    private class ViewHolder{
        private TextView mTxtVwSessionDate;
        private TextView mTxtVwSessionDuration;
        private TextView mTxtVwSessionSDNN;
        private TextView mTxtVwSessionRMS;
        private TextView mTxtVwSessionLnRMS;

    }
}
