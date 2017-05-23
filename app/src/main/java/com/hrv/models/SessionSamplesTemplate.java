package com.hrv.models;

/**
 * Created by manishautomatic on 20/05/17.
 */
public class SessionSamplesTemplate {

    private String value="";
    private String time="";

    public String getHr_value() {
        return value;
    }

    public void setHr_value(String rr_value) {
        this.value = rr_value;
    }

    public String getSample_timestamp() {
        return time;
    }

    public void setSample_timestamp(String sample_timestamp) {
        this.time = sample_timestamp;
    }
}
