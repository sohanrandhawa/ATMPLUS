package com.hrv.models;

import com.orm.SugarRecord;

/**
 * Created by manishautomatic on 03/10/16.
 */

public class SessionTemplate extends SugarRecord {

    private long timeElapsed;
    private long startTime;
    private Double sdNN=0.0;
    private Double rms=0.0;
    private Double lnRMS=0.0;
    private Double hrvValue=0.0;
    private String rrValuesDump="";
    private Double avgHeartRate=0.0;


    public Double getHrvValue() {
        return hrvValue;
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
}
