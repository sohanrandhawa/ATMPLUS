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
            sum=sum+(double)currentReading/1000;
        }
        // evaluate the ke arithmetic mean of the available sample.
        average = sum/temp.size();
        // now we compute the deviations of the samples from the mean
        ArrayList<Double> sampleDeviations = new ArrayList<>();
        sampleDeviations.clear();
        for(int currentSample :temp){
            sampleDeviations.add((((double)currentSample/1000)-average)*((((double)currentSample/1000)-average)));
        }

        // now we compute the sum of the variance..

        double varianceSum=0;
        for(double currentVariance :sampleDeviations){
            varianceSum= varianceSum+currentVariance;
        }

        double computedStandardDeviation = Math.sqrt(varianceSum);
        return computedStandardDeviation;

    }


    public synchronized  double computeRMS(ArrayList<Integer> rrSamples){
        ArrayList<Integer>temp = new ArrayList<>();
        temp.clear();;
        temp.addAll(rrSamples);
        //compute the squares of the values in the sample space.
        ArrayList<Double>squaredSampleSpace = new ArrayList<>();
        squaredSampleSpace.clear();;
            for(int index=0;index<temp.size();index++){
                double currentValue = (double)temp.get(index)/1000;
                squaredSampleSpace.add(currentValue*currentValue);
                //temp.set(index,(((double)temp.get(index))*temp.get(index)));
            }
        // compute the mean of the squared values..
        double squaredMean = 0;
        double sum=0;
            for(double currentSample : squaredSampleSpace){
                sum=sum+currentSample;
            }
        squaredMean = sum/squaredSampleSpace.size();
        double rmsValue = Math.sqrt(squaredMean);
        return rmsValue;

    }

}
