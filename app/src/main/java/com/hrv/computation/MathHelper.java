package com.hrv.computation;

import java.util.ArrayList;

/**
 * Created by manishautomatic on 29/09/16.
 */

public class MathHelper {


  // calculate the standard deviation of the available sample
    public synchronized double  computeSDNN(ArrayList<Integer> rrSamples){

        ArrayList<Integer>temp = new ArrayList<>();
        temp.clear();;
        temp.addAll(rrSamples);
        double average =0;
        double sum=0;
        for(int currentReading :temp){
            sum=sum+(double)currentReading;
        }
        // evaluate the ke arithmetic mean of the available sample.
        average = sum/temp.size();
        // now we compute the deviations of the samples from the mean
        ArrayList<Double> deviations = new ArrayList<>();
        deviations.clear();
        for(int currentSample :temp){
            deviations.add((((double)currentSample)-average)*((((double)currentSample)-average)));
        }

        // now we compute the sum of the variance..

        double varianceSum=0;
        for(double currentVariance :deviations){
            varianceSum= varianceSum+currentVariance;
        }

        double meanVariance = varianceSum/deviations.size();

        double computedStandardDeviation = Math.sqrt(meanVariance);
        return computedStandardDeviation;

    }


    public synchronized  double computeRMS(ArrayList<Integer> rrSamples){
        ArrayList<Integer>temp = new ArrayList<>();
        temp.clear();;
        temp.addAll(rrSamples);
        //compute the squares of the values in the sample space.
        ArrayList<Double>successiveDiffSquared = new ArrayList<>();
        successiveDiffSquared.clear();;
            for(int index=1;index<temp.size();index++){
                double currentDifference = (double)(temp.get(index)-temp.get(index-1));
                successiveDiffSquared.add(Math.pow(currentDifference,2));
            }


        // compute the mean of the squared differences ..
        double squaredMean = 0;
        double sum=0;
            for(double currentSample : successiveDiffSquared){
                sum=sum+currentSample;
            }
        squaredMean = sum/successiveDiffSquared.size();
        double rmsValue = Math.sqrt(squaredMean);
        return rmsValue;

    }



    public synchronized double computeHRV(double lnRMSSD){
        return (double)((100*lnRMSSD)/6.5);
    }
}
