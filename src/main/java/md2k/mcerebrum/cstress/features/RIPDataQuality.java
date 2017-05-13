package md2k.mcerebrum.cstress.features;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center 
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.library.Time;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for RipQualityCalculation
 */
public class RIPDataQuality {

    /**
     * Constructor
     * @param datastreams Global datastream object
     * @param qualityThreshold Input quality threshold
     */
    public RIPDataQuality(DataStreams datastreams, double qualityThreshold) {
        this(datastreams,
                qualityThreshold,
                AUTOSENSE.QUALITY_acceptableOutlierPercent,
                AUTOSENSE.QUALITY_outlierThresholdHigh,
                AUTOSENSE.QUALITY_outlierThresholdLow,
                AUTOSENSE.QUALITY_ripThresholdBandLoose,
                AUTOSENSE.QUALITY_bufferLength,
                AUTOSENSE.QUALITY_windowSizeRip);
    }
    public RIPDataQuality(DataStreams datastreams,
                          double qualityThreshold,
                          double acceptableOutlierPercent,
                          double outlierThresholdHigh,
                          double outlierThresholdLow,
                          double ripThresholdBandLoose,
                          int bufferLength,
                          int windowSizeRip){

        DataPointStream rip = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP);
        DataPointStream ripQuality = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_QUALITY);

        DataPointStream ripRange = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_RANGE);
        ripRange.setHistoricalBufferSize(bufferLength);
        List<DataPoint> quality = computeQuality(rip.data,
                ripRange,
                windowSizeRip,
                acceptableOutlierPercent,
                outlierThresholdHigh,
                outlierThresholdLow,
                ripThresholdBandLoose); //0.67

        double count = 0;
        for (DataPoint dp : quality) {
            ripQuality.add(dp);
            if (dp.value == AUTOSENSE.QUALITY_GOOD) {
                count++;
            }
        }

        DataPointStream ripWindowQuality = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_WINDOW_QUALITY);

        if ((count / quality.size()) > qualityThreshold)
            ripWindowQuality.add(new DataPoint(quality.get(0).timestamp, AUTOSENSE.QUALITY_GOOD));
        else
            ripWindowQuality.add(new DataPoint(quality.get(0).timestamp, AUTOSENSE.QUALITY_BAD));


    }

    private List<DataPoint> computeQuality(List<DataPoint> rip,
                                           DataPointStream ripRange,
                                           int windowSizeRip,
                                           double acceptableOutlierPercent,
                                           double outlierThresholdHigh,
                                           double outlierThresholdLow,
                                           double ripThresholdBandLoose){

        List<DataPoint[]> windowedRIP = Time.window(rip, windowSizeRip);
        List<DataPoint> result = new ArrayList<>();

        for (DataPoint[] dpA : windowedRIP) {
            if (dpA.length > 0) {
                result.add(new DataPoint(dpA[0].timestamp, currentQuality(dpA,ripRange,acceptableOutlierPercent,outlierThresholdHigh,outlierThresholdLow,ripThresholdBandLoose)));
            }
        }
        return result;

    }

    private int currentQuality(DataPoint[] data,
                               DataPointStream ripRange,
                               double acceptableOutlierPercent,
                               double outlierThresholdHigh,
                               double outlierThresholdLow,
                               double ripThresholdBandLoose) {

        int[] outlierCounts = classifyDataPoints(data,outlierThresholdHigh,outlierThresholdLow);
        int segmentClass = classifySegment(data,outlierCounts,acceptableOutlierPercent);
        ripRange.add(new DataPoint(data[0].timestamp,(double)(outlierCounts[1]-outlierCounts[2])));

        List<DataPoint> rangeValues = ripRange.getHistoricalNValues(3);
        int amplitudeSmall = classifyBuffer(rangeValues,ripThresholdBandLoose);

        if (segmentClass == AUTOSENSE.SEGMENT_BAD) {
            return AUTOSENSE.QUALITY_BAND_OFF;
        } else if (2 * amplitudeSmall > AUTOSENSE.QUALITY_bufferLength) {
            return AUTOSENSE.QUALITY_BAND_LOOSE;
        }else if((outlierCounts[1]-outlierCounts[2]) <= (int)(ripThresholdBandLoose*AUTOSENSE.ADC_range)) {
            return AUTOSENSE.QUALITY_BAND_LOOSE;
        }
        return AUTOSENSE.QUALITY_GOOD;
    }

    private int classifyBuffer(List<DataPoint> rangeValues, double ripThresholdBandLoose) {
        int amplitudeSmall =0;
        if (rangeValues.size() < 3){
            amplitudeSmall = 0;
            return amplitudeSmall;
        }else {
            for (int i = 0; i < rangeValues.size(); i++) {
                if ( rangeValues.get(i).value < ripThresholdBandLoose*AUTOSENSE.ADC_range) {
                    amplitudeSmall++;
                }
            }
            return amplitudeSmall;
        }
    }

    private int classifySegment(DataPoint[] data, int[] outlierCounts,double acceptableOutlierPercent) {
        int segmentClass;
        if (outlierCounts[0] > (int)(acceptableOutlierPercent * data.length)) {
            segmentClass = AUTOSENSE.SEGMENT_BAD;
        } else {
            segmentClass = AUTOSENSE.SEGMENT_GOOD;
        }
        return segmentClass;
    }

    private int[] classifyDataPoints(DataPoint[] data,
                                     double outlierThresholdHigh,
                                     double outlierThresholdLow){

        int[] outlierCounts = new int[3];// index 0 = outlier sum, index 1 = max value,index 2 = min value

        outlierCounts[1]=(int)data[0].value;
        outlierCounts[2]=(int)data[0].value;
        for(int i=0;i<data.length;i++){
            int im=((i==0)?(data.length-1):(i-1));
            int ip=((i==data.length-1)?(0):(i+1));
            boolean stuck=((data[i].value==data[im].value)&&(data[i].value==data[ip].value));
            boolean flip=((Math.abs(data[i].value-data[im].value)>((int)(outlierThresholdHigh*AUTOSENSE.ADC_range)))||(Math.abs(data[i].value-data[ip].value)>((int)(outlierThresholdHigh*AUTOSENSE.ADC_range))));
            boolean disc=((Math.abs(data[i].value-data[im].value)>((int)(AUTOSENSE.QUALITY_ripSlope*AUTOSENSE.ADC_range)))|| (Math.abs(data[i].value-data[ip].value)>((int)(AUTOSENSE.QUALITY_ripSlope*AUTOSENSE.ADC_range))));
            if(disc) outlierCounts[0]++;
            else if(stuck) outlierCounts[0]++;
            else if(flip) outlierCounts[0]++;
            else if(data[i].value >= outlierThresholdHigh*AUTOSENSE.ADC_range){
                outlierCounts[0]++;
            }else if(data[i].value <= outlierThresholdLow*AUTOSENSE.ADC_range){
                outlierCounts[0]++;
            }else{
                if(data[i].value > outlierCounts[1]) outlierCounts[1]=(int)data[i].value;
                if(data[i].value < outlierCounts[2]) outlierCounts[2]=(int)data[i].value;
            }
        }
        return outlierCounts;
    }

}
