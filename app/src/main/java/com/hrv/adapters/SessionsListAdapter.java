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
import java.util.TimeZone;

/**
 * Created by manishautomatic on 03/10/16.
 */

public class SessionsListAdapter extends BaseAdapter {


    private LayoutInflater inflater;
    private ArrayList<SessionTemplate> data = new ArrayList<>();

    public SessionsListAdapter(Context context, List<SessionTemplate> sessions){

        Context parentReference = context;
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
            holder.mTxtVwSessionHRV=(TextView)view.findViewById(R.id.txtvwSessionHRV);
            holder.mTxtVwSessionHR=(TextView)view.findViewById(R.id.txtvwMeanHR);
            holder.mTxtVwSessionType=(TextView)view.findViewById(R.id.txtvwSessionType);
            view.setTag(holder);
        }else{
            holder =(ViewHolder)view.getTag();
        }

        Double SDNN= data.get(i).getSdNN();
        Double RMSSD = data.get(i).getRms();
        Double LnRMS = data.get(i).getLnRMS();
        Double meanHeartRate = data.get(i).getAvgHeartRate();
        Long sessionDuration = data.get(i).getTimeElapsed();
        Long sessionStartDate = data.get(i).getStartTime();


        Date date = new Date(sessionStartDate);
        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
        String sessionDate = dateformat.format(date);

        DecimalFormat df = new DecimalFormat("#.##");
        String strSDNN = df.format(SDNN);
        String strRMSSD = df.format(RMSSD);
        String strLnRMSSD = df.format(LnRMS);
        String strHeartRate = df.format(meanHeartRate);
        holder.mTxtVwSessionDate.setText("Date: "+sessionDate);

        holder.mTxtVwSessionHR.setText("Mean Heart Rate: "+strHeartRate);
        holder.mTxtVwSessionDuration.setText("Duration: "+millisToMinutes(data.get(i).getTimeElapsed()));
        holder.mTxtVwSessionSDNN.setText("SDNN: "+strSDNN);
        holder.mTxtVwSessionRMS.setText("RMSSD: "+strRMSSD);
        holder.mTxtVwSessionLnRMS.setText("LnRMSSD: "+strLnRMSSD);
        holder.mTxtVwSessionHRV.setText("HRV: "+df.format(data.get(i).getHrvValue()));
        String sessionType="";
        if(data.get(i).getSessionType()==1){
            sessionType="HEART RATE";
        }if(data.get(i).getSessionType()==2){
            sessionType="BREATHING EXCERCISE";
        }
        holder.mTxtVwSessionType.setText(sessionType);


        return view;
    }


    private String millisToMinutes(long millis){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        return df.format(new Date(millis));

    }

    private class ViewHolder{
        private TextView mTxtVwSessionDate;
        private TextView mTxtVwSessionDuration;
        private TextView mTxtVwSessionSDNN;
        private TextView mTxtVwSessionRMS;
        private TextView mTxtVwSessionLnRMS;
        private TextView mTxtVwSessionHRV;
        private TextView mTxtVwSessionHR;
        private TextView mTxtVwSessionType;

    }
}
