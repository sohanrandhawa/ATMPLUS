package com.hrv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.hrv.adapters.SessionsListAdapter;
import com.hrv.models.SessionTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by manishautomatic on 19/09/16.
 */

public class ActivitySessionsHistory extends AppCompatActivity {


    private ListView mLstVwSessions;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_sessions_history);
        initUI();
        populateSessions();
    }



    private void initUI(){
        mLstVwSessions=(ListView)findViewById(R.id.lstVwSessions);
    }


    private void populateSessions(){

        List<SessionTemplate> sessionsHistory = SessionTemplate.listAll(SessionTemplate.class);
        sessionsHistory.size();
        Collections.reverse(sessionsHistory);

        SessionsListAdapter sessionsAdapter = new SessionsListAdapter(ActivitySessionsHistory.this,sessionsHistory);
        mLstVwSessions.setAdapter(sessionsAdapter);
    }
}
