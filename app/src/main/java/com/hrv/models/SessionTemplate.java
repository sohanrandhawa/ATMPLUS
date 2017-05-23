package com.hrv.models;

import com.orm.SugarRecord;

import java.util.ArrayList;

/**
 * Created by manishautomatic on 03/10/16.
 */

public class SessionTemplate extends SugarRecord {

    private long timeElapsed;
    private long startTime;
    private String endTime;

    private Double sdNN=0.0;
    private Double rms=0.0;
    private Double lnRMS=0.0;
    private Double hrvValue=0.0;
    private String rrValuesDump="";
    private String rmsValuesDump="";
    private Double avgHeartRate=0.0;
    private int sessionType=-1; // -1 = not defined | 1= Heart Rate | 2= Breathing Training
    private String sessionid="";
    private ArrayList<SessionSamplesTemplate> samples = new ArrayList<SessionSamplesTemplate>();



    public Double getHrvValue() {
        return hrvValue;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getAvgHeartRate() {
        return avgHeartRate;
    }

    public void setAvgHeartRate(Double avgHeartRate) {
        this.avgHeartRate = avgHeartRate;
    }

    public void setHrvValue(Double hrvValue) {
        this.hrvValue = hrvValue;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Double getSdNN() {
        return sdNN;
    }

    public void setSdNN(Double sdNN) {
        this.sdNN = sdNN;
    }

    public Double getRms() {
        return rms;
    }

    public void setRms(Double rms) {
        this.rms = rms;
    }

    public Double getLnRMS() {
        return lnRMS;
    }

    public void setLnRMS(Double lnRMS) {
        this.lnRMS = lnRMS;
    }

    public String getRrValuesDump() {
        return rrValuesDump;
    }

    public void setRrValuesDump(String rrValuesDump) {
        this.rrValuesDump = rrValuesDump;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public String getRmsValuesDump() {
        return rmsValuesDump;
    }

    public void setRmsValuesDump(String rmsValuesDump) {
        this.rmsValuesDump = rmsValuesDump;
    }


    public ArrayList<SessionSamplesTemplate> getSamples() {
        return samples;
    }

    public void setSamples(ArrayList<SessionSamplesTemplate> samples) {
        this.samples = samples;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public int getSessionType() {
        return sessionType;
    }
    public int setSessionType() {
        return sessionType;
    }
}
